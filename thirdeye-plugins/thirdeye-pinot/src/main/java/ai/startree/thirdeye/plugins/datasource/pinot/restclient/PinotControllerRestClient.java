/*
 * Copyright 2022 StarTree Inc
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

import static ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceUtils.buildConfig;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSourceConfig;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.pinot.spi.data.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotControllerRestClient {

  private static final Logger LOG = LoggerFactory.getLogger(PinotControllerRestClient.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final org.codehaus.jackson.map.ObjectMapper CODEHAUS_OBJECT_MAPPER =
      new org.codehaus.jackson.map.ObjectMapper();

  private static final String PINOT_TABLES_ENDPOINT = "/tables/";
  private static final String PINOT_TABLES_ENDPOINT_TEMPLATE = "/tables/%s";
  private static final String PINOT_SCHEMA_ENDPOINT_TEMPLATE = "/schemas/%s";
  private static final String PINOT_TABLE_CONFIG_ENDPOINT_TEMPLATE = "/tables/%s/schema";

  private final HttpHost pinotControllerHost;
  private final PinotControllerRestClientSupplier pinotControllerRestClientSupplier;

  @Inject
  public PinotControllerRestClient(final PinotThirdEyeDataSourceConfig config,
      final PinotControllerRestClientSupplier pinotControllerRestClientSupplier) {

    pinotControllerHost = new HttpHost(config.getControllerHost(),
        config.getControllerPort(),
        config.getControllerConnectionScheme());
    this.pinotControllerRestClientSupplier = pinotControllerRestClientSupplier;
  }

  /**
   * TODO shounak refactor constructor
   */
  @Deprecated
  public PinotControllerRestClient(final DataSourceMetaBean dataSourceMeta,
      final String dataSourceType) {
    final Map<String, Object> properties = dataSourceMeta.getProperties();
    Preconditions.checkArgument(dataSourceType.equals("pinot-sql"),
        "This constructor is only called from pinot-sql connector");
    final PinotThirdEyeDataSourceConfig config = buildConfig(properties);
    pinotControllerHost = new HttpHost(config.getControllerHost(),
        config.getControllerPort(),
        config.getControllerConnectionScheme());

    pinotControllerRestClientSupplier = null;
  }

  public List<String> getAllTablesFromPinot() throws IOException {
    final HttpGet tablesReq = new HttpGet(PINOT_TABLES_ENDPOINT);
    LOG.info("Retrieving datasets: {}", tablesReq);
    final CloseableHttpResponse tablesRes = pinotControllerRestClientSupplier.get()
        .execute(pinotControllerHost, tablesReq);
    if (tablesRes.getStatusLine().getStatusCode() != 200) {
      throw new IllegalStateException(tablesRes.getStatusLine().toString());
    }
    final InputStream tablesContent = tablesRes.getEntity().getContent();
    final GetTablesResponseApi api = OBJECT_MAPPER.readValue(tablesContent,
        GetTablesResponseApi.class);
    return optional(api)
        .map(GetTablesResponseApi::getTables)
        .orElse(Collections.emptyList());
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
    LOG.info("Retrieving schema: {}", schemaReq);
    final CloseableHttpResponse schemaRes = pinotControllerRestClientSupplier.get()
        .execute(pinotControllerHost, schemaReq);
    try {
      if (schemaRes.getStatusLine().getStatusCode() != 200) {
        LOG.error("Schema {} not found, {}", dataset, schemaRes.getStatusLine().toString());
      } else {
        final InputStream schemaContent = schemaRes.getEntity().getContent();
        schema = CODEHAUS_OBJECT_MAPPER.readValue(schemaContent, Schema.class);
      }
    } catch (final Exception e) {
      LOG.error("Exception in retrieving schema collections, skipping {}", dataset);
    } finally {
      if (schemaRes.getEntity() != null) {
        EntityUtils.consume(schemaRes.getEntity());
      }
      schemaRes.close();
    }
    return schema;
  }

  /**
   * Verify schema name and presence of field spec for time column
   */
  public boolean verifySchemaCorrectness(final Schema schema,
      @Nullable final String timeColumnName) {
    return !StringUtils.isBlank(schema.getSchemaName())
        && timeColumnName != null
        && schema.getSpecForTimeColumn(timeColumnName) != null;
  }

  public JsonNode getTableConfigFromPinotEndpoint(final String dataset) throws IOException {
    final HttpGet request = new HttpGet(String.format(PINOT_TABLES_ENDPOINT_TEMPLATE, dataset));
    final CloseableHttpResponse response = pinotControllerRestClientSupplier.get()
        .execute(pinotControllerHost, request);
    LOG.debug("Retrieving dataset's custom config: {}", request);

    // Retrieve table config
    JsonNode tables = null;
    try {
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new IllegalStateException(response.getStatusLine().toString());
      }
      final InputStream tablesContent = response.getEntity().getContent();
      tables = OBJECT_MAPPER.readTree(tablesContent);
    } catch (final Exception e) {
      LOG.error("Exception in loading dataset {}", dataset, e);
    } finally {
      if (response.getEntity() != null) {
        EntityUtils.consume(response.getEntity());
      }
      response.close();
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

  /**
   * Returns the map of custom configs of the given dataset from the Pinot table config json.
   */
  public Map<String, String> extractCustomConfigsFromPinotTable(final JsonNode tableConfigJson) {

    Map<String, String> customConfigs = Collections.emptyMap();
    try {
      final JsonNode jsonNode = tableConfigJson.get("metadata").get("customConfigs");
      customConfigs = OBJECT_MAPPER.convertValue(jsonNode, new TypeReference<>() {});
    } catch (final Exception e) {
      LOG.warn("Failed to get custom config from table: {}. Reason: {}", tableConfigJson, e);
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
