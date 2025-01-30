/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.datasource.pinot.restclient;

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSource.HTTPS_SCHEME;
import static ai.startree.thirdeye.spi.Constants.TWO_DIGITS_FORMATTER;
import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotOauthUtils;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfig;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.data.Schema;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotControllerRestClient {

  private static final Logger LOG = LoggerFactory.getLogger(PinotControllerRestClient.class);
  private static final String PINOT_TABLES_ENDPOINT = "/tables/";
  private static final String PINOT_MY_TABLES_ENDPOINT = "/mytables";
  private static final String PINOT_TABLES_ENDPOINT_TEMPLATE = "/tables/%s";
  private static final String PINOT_SCHEMA_ENDPOINT_TEMPLATE = "/schemas/%s";
  private static final String PINOT_TABLE_CONFIG_ENDPOINT_TEMPLATE = "/tables/%s/schema";

  private static final String TABLE_CONFIG_QUOTA_KEY = "quota";
  private static final String TABLE_CONFIG_QUOTA_MAX_QPS_KEY = "maxQueriesPerSecond";

  private static final ExecutorService CONNECTION_CLOSER_EXECUTOR = Executors.newSingleThreadExecutor(
      new ThreadFactoryBuilder().setNameFormat("pinot-controller-client-connection-closer").build());

  private final HttpHost pinotControllerHost;

  private final PinotThirdEyeDataSourceConfig config;
  private CloseableHttpClient pinotControllerClient = null;
  private String currentToken = null;
  
  public PinotControllerRestClient(final PinotThirdEyeDataSourceConfig config) {
    this.config = config;
    this.pinotControllerHost = new HttpHost(config.getControllerHost(),
        config.getControllerPort(),
        config.getControllerConnectionScheme());
  }

  // FIXME ASAP - pinotControllerClient being closed is not managed - add this logic - consider using org.asynchttpclient for consistency with Pinot and remove httpcomponents dependency
  private CloseableHttpClient getHttpClient() {
    if (config.isOAuthEnabled()) {
      // fixme cyril - at every call this reads a file 
      final String newToken = requireNonNull(PinotOauthUtils.getOauthToken(config.getOauth()), "token supplied is null");
      if (pinotControllerClient == null || !Objects.equals(currentToken, newToken)) {
        // need to update the authorization token 
        // closing old connection is a lower priority - do it async
        closeClientAsync(pinotControllerClient);

        currentToken = newToken;
        final Map<String, String> additionalHeaders = Map.of(HttpHeaders.AUTHORIZATION, currentToken);
        pinotControllerClient = createHttpClient(config, additionalHeaders);
      }
    } else {
      if (pinotControllerClient == null) {
        pinotControllerClient = createHttpClient(config, Map.of());
      }
    }

    return pinotControllerClient;
  }

  private static CloseableHttpClient createHttpClient(
      final PinotThirdEyeDataSourceConfig config, final @NonNull Map<String, String> additionalHeaders) {
    final HttpClientBuilder builder = HttpClients.custom();

    // set headers
    final Map<String, String> mergedHeaders = new HashMap<>();
    optional(config.getHeaders()).ifPresent(mergedHeaders::putAll);
    mergedHeaders.putAll(additionalHeaders);
    builder.setDefaultHeaders(mergedHeaders.entrySet()
        .stream()
        .map(e -> new BasicHeader(e.getKey(), e.getValue()))
        .collect(Collectors.toList()));

    // set ssl context if necessary
    if (HTTPS_SCHEME.equals(config.getControllerConnectionScheme())) {
      builder.setSSLContext(httpsSslContext()).setSSLHostnameVerifier(new NoopHostnameVerifier());
    }

    return builder.build();
  }

  private void closeClientAsync(final CloseableHttpClient httpClient) {
    if (httpClient != null) {
      CONNECTION_CLOSER_EXECUTOR.submit(() -> this.closeClient(httpClient));
    }
  }

  private void closeClient(final CloseableHttpClient httpClient) {
    try {
      final long startTime = System.nanoTime();
      if (httpClient != null) {
        httpClient.close();
        LOG.info("Successfully closed pinot controller client. took {}ms",
            TWO_DIGITS_FORMATTER.format((System.nanoTime() - startTime) / 1e6));
      }
    } catch (final IOException e) {
      LOG.warn("Failed to close pinot controller client", e);
    } catch (final Exception e) {
      LOG.error("Failed to close pinot controller client", e);
    }
  }

  public List<String> getAllTablesFromPinot() throws IOException {
    final String endpoint = getPinotAllTablesEndpoint();
    final HttpGet tablesReq = new HttpGet(endpoint);
    LOG.debug("Retrieving datasets: {}", tablesReq);
    CloseableHttpResponse tablesRes = null;
    try {
      tablesRes =  getHttpClient().execute(pinotControllerHost, tablesReq);
      if (tablesRes.getStatusLine().getStatusCode() != 200) {
        throw new IllegalStateException(tablesRes.getStatusLine().toString());
      }
      final InputStream tablesContent = tablesRes.getEntity().getContent();
      final GetTablesResponseApi api = VANILLA_OBJECT_MAPPER.readValue(tablesContent,
          GetTablesResponseApi.class);
      return optional(api)
          .map(GetTablesResponseApi::getTables)
          .orElse(Collections.emptyList());
    } finally {
      HttpUtils.safeClose(tablesRes);
    }
  }

  /**
   * Checks if /mytables endpoint is available
   * else falls back to /tables endpoint
   */
  private String getPinotAllTablesEndpoint() throws IOException {
    final HttpHead mytablesReq = new HttpHead(PINOT_MY_TABLES_ENDPOINT);
    CloseableHttpResponse mytablesRes = null;
    try {
      mytablesRes =  getHttpClient()
          .execute(pinotControllerHost, mytablesReq);
      if (mytablesRes.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
        return PINOT_MY_TABLES_ENDPOINT;
      }
      return PINOT_TABLES_ENDPOINT;
    } finally {
      HttpUtils.safeClose(mytablesRes);
    }
  }

  /**
   * Fetches schema from pinot, from the tables endpoint or schema endpoint
   */
  public Schema getSchemaFromPinot(final String dataset) throws IOException {
    Schema schema = getSchemaFromPinotEndpoint(PINOT_TABLE_CONFIG_ENDPOINT_TEMPLATE, dataset);
    if (schema == null) {
      schema = getSchemaFromPinotEndpoint(PINOT_SCHEMA_ENDPOINT_TEMPLATE, dataset);
    }
    if (schema == null) {
      schema = getSchemaFromPinotEndpoint(PINOT_SCHEMA_ENDPOINT_TEMPLATE, dataset + "_OFFLINE");
    }
    return schema;
  }

  private Schema getSchemaFromPinotEndpoint(final String endpointTemplate, final String dataset)
      throws IOException {
    Schema schema = null;
    final HttpGet schemaReq = new HttpGet(
        String.format(endpointTemplate, URLEncoder.encode(dataset, StandardCharsets.UTF_8)));
    CloseableHttpResponse schemaRes = null;
    try {
      schemaRes =  getHttpClient().execute(pinotControllerHost, schemaReq);
      if (schemaRes.getStatusLine().getStatusCode() != 200) {
        LOG.error("Schema {} not found, {}", dataset, schemaRes.getStatusLine().toString());
      } else {
        final InputStream schemaContent = schemaRes.getEntity().getContent();
        schema = VANILLA_OBJECT_MAPPER.readValue(schemaContent, Schema.class);
      }
    } catch (final Exception e) {
      LOG.error("Exception in retrieving schema collections, skipping {}", dataset, e);
    } finally {
      HttpUtils.safeClose(schemaRes);
    }
    return schema;
  }

  public JsonNode getTableConfigFromPinotEndpoint(final String dataset) throws IOException {
    final HttpGet request = new HttpGet(String.format(PINOT_TABLES_ENDPOINT_TEMPLATE, dataset));
    CloseableHttpResponse response = null;
    // Retrieve table config
    JsonNode tables = null;
    try {
      response =  getHttpClient().execute(pinotControllerHost, request);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new IllegalStateException(response.getStatusLine().toString());
      }
      final InputStream tablesContent = response.getEntity().getContent();
      tables = VANILLA_OBJECT_MAPPER.readTree(tablesContent);
    } catch (final Exception e) {
      LOG.error("Exception in loading dataset {}", dataset, e);
    } finally {
      HttpUtils.safeClose(response);
    }

    JsonNode tableJson = null;
    if (tables != null) {
      tableJson = tables.get("REALTIME");
      if (tableJson == null || tableJson.isNull()) {
        tableJson = tables.get("OFFLINE");
      }
    }
    return tableJson;
  }

  public void updateTableMaxQPSQuota(final String dataset, final JsonNode tableJson, 
      final @Nullable Integer maxQpsQuota) throws IOException {
    if (maxQpsQuota == null || maxQpsQuota <= 0) {
      return;
    }

    // update quota if it exists
    final JsonNode quotaJson = tableJson.get(TABLE_CONFIG_QUOTA_KEY);
    if (quotaJson != null) {
      ((ObjectNode) quotaJson).put(TABLE_CONFIG_QUOTA_MAX_QPS_KEY, Integer.toString(maxQpsQuota));
    } else {
      LOG.error("quota not configured for dataset {} while onboarding. skipping max qps override", dataset);
      return;
    }

    // update table config with updated quota
    final HttpPut request = new HttpPut(String.format(PINOT_TABLES_ENDPOINT_TEMPLATE, dataset));
    request.setEntity(new StringEntity(tableJson.toString()));

    CloseableHttpResponse response = null;
    try {
      response =  getHttpClient().execute(pinotControllerHost, request);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new IllegalStateException(response.getStatusLine().toString());
      }
    } catch (final Exception e) {
      LOG.error("Exception in updating table config of dataset {}", dataset, e);
      throw e;
    } finally {
      HttpUtils.safeClose(response);
    }

    return ;
  }

  // schema name should always be equal to table name without the type
  public @NonNull String postSchema(final Schema schema, final boolean override,
      final boolean force) throws IOException {
    final HttpPost request = new HttpPost(
        "/schemas?override=%s&force=%s".formatted(override, force));
    request.setEntity(MultipartEntityBuilder.create()
        .addTextBody("file", schema.toJsonObject().toString(), ContentType.DEFAULT_TEXT)
        .build());

    CloseableHttpResponse response = null;
    try {
      response =  getHttpClient().execute(pinotControllerHost, request);
      if (response.getStatusLine().getStatusCode() != 200) {
        switch (response.getStatusLine().getStatusCode()) {
          case 409:
            throw new ThirdEyeException(
                ThirdEyeStatus.ERR_DATASOURCE_DEMO_TABLE_CREATION_CONFLICT_ERROR,
                schema.getSchemaName(),
                "Failed to create %s schema in Pinot because of a conflict. Schema %s already exists?"
                    .formatted(schema.getSchemaName(), schema.getSchemaName()));
          default:
            throw new ThirdEyeException(ThirdEyeStatus.ERR_DATASOURCE_DEMO_TABLE_CREATION_UNKNOWN_ERROR,
                "Failed to create %s schema in Pinot: %s".formatted(schema.getSchemaName(),
                    response.getStatusLine().toString()));
        }
      }
    } catch (final Exception e) {
      LOG.error("Exception in creating schema {}.", schema, e);
      throw e;
    } finally {
      HttpUtils.safeClose(response);
    }
    return schema.getSchemaName();
  }

  // returns the table name with type. Eg pageviews_OFFLINE
  public @NonNull String postTable(final TableConfig tableConfig) throws IOException {
    final HttpPost request = new HttpPost(PINOT_TABLES_ENDPOINT);
    request.setEntity(new StringEntity(tableConfig.toJsonString()));

    CloseableHttpResponse response = null;
    try {
      response =  getHttpClient().execute(pinotControllerHost, request);
      if (response.getStatusLine().getStatusCode() != 200) {
        switch (response.getStatusLine().getStatusCode()) {
          case 409:
            throw new ThirdEyeException(
                ThirdEyeStatus.ERR_DATASOURCE_DEMO_TABLE_CREATION_CONFLICT_ERROR,
                tableConfig.getTableName(),
                "Failed to create %s table in Pinot because of a conflict. Table %s already exists?"
                    .formatted(tableConfig.getTableName(), tableConfig.getTableName()));
          default:
            throw new ThirdEyeException(ThirdEyeStatus.ERR_DATASOURCE_DEMO_TABLE_CREATION_UNKNOWN_ERROR,
                "Failed to create %s table in Pinot: %s".formatted(tableConfig.getTableName(),
                    response.getStatusLine().toString()));
        }
      }
    } catch (final Exception e) {
      LOG.error("Exception in creating table {}.", tableConfig, e);
      throw e;
    } finally {
      HttpUtils.safeClose(response);
    }
    return tableConfig.getTableName();
  }
  
  // note: this is slow because this is downloading the file before sending it to Pinot
  // see discussion here https://startreedata.slack.com/archives/C019DPR16JW/p1736590160817849?thread_ts=1736537370.189139&cid=C019DPR16JW
  // don't want to introduce a caching on disk strategy, I think the issue should be fixed on Pinot side
  public void postIngestFromFile(final @NonNull String tableNameWithType,
      final @NonNull String batchConfigMapStr, final @NonNull String sourceURIStr)
      throws IOException {
    final HttpPost request = new HttpPost(
        "/ingestFromFile?tableNameWithType=%s&batchConfigMapStr=%s"
            .formatted(tableNameWithType,
                URLEncoder.encode(batchConfigMapStr, StandardCharsets.UTF_8)));
    request.setEntity(MultipartEntityBuilder.create()
            .addBinaryBody("file", URI.create(sourceURIStr).toURL().openStream())
        .build());

    CloseableHttpResponse response = null;
    try {
      response =  getHttpClient().execute(pinotControllerHost, request);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new RuntimeException("Failed to load data to table %s : %s"
            .formatted(tableNameWithType, response.getStatusLine().toString()));
      }
    } catch (final Exception e) {
      LOG.error("Exception in loading data to table {}.", tableNameWithType, e);
      throw e;
    } finally {
      HttpUtils.safeClose(response);
    }
  }

  public void close() {
    if (pinotControllerClient != null) {
      try {
        pinotControllerClient.close();
      } catch (IOException e) {
        LOG.error("Exception closing pinotControllerClient", e);
      }
    }
  }

  // SSL context that accepts all SSL certificate.
  private static SSLContext httpsSslContext() {
    try {
      return new SSLContextBuilder()
          .loadTrustMaterial(null, new AcceptAllTrustStrategy())
          .build();
    } catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      // This section shouldn't happen because we use Accept All Strategy
      LOG.error("Failed to generate SSL context for Pinot in https.", e);
      throw new RuntimeException(e);
    }
  }


  /**
   * This class accepts (i.e., ignores) all SSL certificate.
   */
  private static class AcceptAllTrustStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(final X509Certificate[] x509Certificates, final String s)
        throws CertificateException {
      return true;
    }
  }
}
