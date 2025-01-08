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

import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfig;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.pinot.spi.data.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotControllerRestClient {

  private static final Logger LOG = LoggerFactory.getLogger(PinotControllerRestClient.class);
  private static final String PINOT_TABLES_ENDPOINT = "/tables/";
  private static final String PINOT_MY_TABLES_ENDPOINT = "/mytables";
  private static final String PINOT_TABLES_ENDPOINT_TEMPLATE = "/tables/%s";
  private static final String PINOT_SCHEMA_ENDPOINT_TEMPLATE = "/schemas/%s";
  private static final String PINOT_TABLE_CONFIG_ENDPOINT_TEMPLATE = "/tables/%s/schema";

  private static final String TABLE_CONFIG_QUOTA_KEY = "quota";
  private static final String TABLE_CONFIG_QUOTA_MAX_QPS_KEY = "maxQueriesPerSecond";

  private final HttpHost pinotControllerHost;
  private final PinotControllerHttpClientProvider pinotControllerRestClientSupplier;
  private final ThirdEyeDataSourceContext context;

  @Inject
  public PinotControllerRestClient(final PinotThirdEyeDataSourceConfig config,
      final ThirdEyeDataSourceContext context) {

    pinotControllerHost = new HttpHost(config.getControllerHost(),
        config.getControllerPort(),
        config.getControllerConnectionScheme());
    this.pinotControllerRestClientSupplier = new PinotControllerHttpClientProvider(config);
    this.context = context;
  }

  public List<String> getAllTablesFromPinot() throws IOException {
    final String endpoint = getPinotAllTablesEndpoint();
    final HttpGet tablesReq = new HttpGet(endpoint);
    LOG.debug("Retrieving datasets: {}", tablesReq);
    CloseableHttpResponse tablesRes = null;
    try {
      tablesRes = pinotControllerRestClientSupplier.get().execute(pinotControllerHost, tablesReq);
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
      if (tablesRes != null) {
        if (tablesRes.getEntity() != null) {
          EntityUtils.consume(tablesRes.getEntity());
        }
        tablesRes.close();
      }
    }
  }

  /**
   * Checks if /mytables endpoint is available
   * else falls back to /tables endpoint
   */
  private String getPinotAllTablesEndpoint() throws IOException  {
    final HttpHead mytablesReq = new HttpHead(PINOT_MY_TABLES_ENDPOINT);
    CloseableHttpResponse mytablesRes = null;
    try {
      mytablesRes = pinotControllerRestClientSupplier.get().execute(pinotControllerHost, mytablesReq);
      if (mytablesRes.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
        return PINOT_MY_TABLES_ENDPOINT;
      }
      return PINOT_TABLES_ENDPOINT;
    } finally {
      if (mytablesRes != null) {
        if (mytablesRes.getEntity() != null) {
          EntityUtils.consume(mytablesRes.getEntity());
        }
        mytablesRes.close();
      }
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
      schemaRes = pinotControllerRestClientSupplier.get().execute(pinotControllerHost, schemaReq);
      if (schemaRes.getStatusLine().getStatusCode() != 200) {
        LOG.error("Schema {} not found, {}", dataset, schemaRes.getStatusLine().toString());
      } else {
        final InputStream schemaContent = schemaRes.getEntity().getContent();
        schema = VANILLA_OBJECT_MAPPER.readValue(schemaContent, Schema.class);
      }
    } catch (final Exception e) {
      LOG.error("Exception in retrieving schema collections, skipping {}", dataset, e);
    } finally {
      if (schemaRes != null) {
        if (schemaRes.getEntity() != null) {
          EntityUtils.consume(schemaRes.getEntity());
        }
        schemaRes.close();
      }
    }
    return schema;
  }

  public JsonNode getTableConfigFromPinotEndpoint(final String dataset) throws IOException {
    final HttpGet request = new HttpGet(String.format(PINOT_TABLES_ENDPOINT_TEMPLATE, dataset));
    CloseableHttpResponse response = null;
    // Retrieve table config
    JsonNode tables = null;
    try {
      response = pinotControllerRestClientSupplier.get().execute(pinotControllerHost, request);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new IllegalStateException(response.getStatusLine().toString());
      }
      final InputStream tablesContent = response.getEntity().getContent();
      tables = VANILLA_OBJECT_MAPPER.readTree(tablesContent);
    } catch (final Exception e) {
      LOG.error("Exception in loading dataset {}", dataset, e);
    } finally {
      if (response != null) {
        if (response.getEntity() != null) {
          EntityUtils.consume(response.getEntity());
        }
        response.close();
      }
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

  public void updateTableMaxQPSQuota(final String dataset, final JsonNode tableJson) throws IOException {
    Integer customMaxQPSQuota = context.getEnvironmentContextConfiguration().getPinotMaxQPSQuotaOverride();
    if (customMaxQPSQuota == null || customMaxQPSQuota <= 0) {
      return;
    }

    // update quota if it exists
    JsonNode quotaJson = tableJson.get(TABLE_CONFIG_QUOTA_KEY);
    if (quotaJson != null) {
      ((ObjectNode) quotaJson).put(TABLE_CONFIG_QUOTA_MAX_QPS_KEY, Integer.toString(customMaxQPSQuota));
    } else {
      LOG.warn("quota not configured for dataset {} while onboarding. skipping max qps override", dataset);
      return;
    }

    // update table config with updated quota
    final HttpPut request = new HttpPut(String.format(PINOT_TABLES_ENDPOINT_TEMPLATE, dataset));
    request.setEntity(new StringEntity(tableJson.toString()));

    CloseableHttpResponse response = null;
    try {
      response = pinotControllerRestClientSupplier.get().execute(pinotControllerHost, request);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new IllegalStateException(response.getStatusLine().toString());
      }
    } catch (final Exception e) {
      LOG.error("Exception in updating table config of dataset {}", dataset, e);
      throw e;
    } finally {
      if (response != null) {
        if (response.getEntity() != null) {
          EntityUtils.consume(response.getEntity());
        }
        response.close();
      }
    }

    return ;
  }

  /**
   * Returns the map of custom configs of the given dataset from the Pinot table config json.
   */
  public static Map<String, String> extractCustomConfigsFromPinotTable(final JsonNode tableConfigJson) {

    Map<String, String> customConfigs = Collections.emptyMap();
    try {
      final JsonNode jsonNode = tableConfigJson.get("metadata").get("customConfigs");
      customConfigs = VANILLA_OBJECT_MAPPER.convertValue(jsonNode, new TypeReference<>() {});
    } catch (final Exception e) {
      LOG.warn("Failed to get custom config from table: {}. Exception:", tableConfigJson, e);
    }
    return customConfigs;
  }

  public String extractTimeColumnFromPinotTable(final JsonNode tableConfigJson) {
    final JsonNode timeColumnNode = tableConfigJson.get("segmentsConfig").get("timeColumnName");
    return (timeColumnNode != null && !timeColumnNode.isNull()) ? timeColumnNode.asText() : null;
  }

  public void close() {
    pinotControllerRestClientSupplier.close();
  }
}
