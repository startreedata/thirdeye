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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_CHRONOLOGY;
import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.datasource.pinot.DemoConfigs.DemoDatasetConfig;
import ai.startree.thirdeye.plugins.datasource.pinot.restclient.PinotControllerRestClient;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.spi.metric.MetricType;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.spi.data.DateTimeFieldSpec;
import org.apache.pinot.spi.data.MetricFieldSpec;
import org.apache.pinot.spi.data.Schema;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotDatasetReader {

  public static final MetricAggFunction DEFAULT_AGG_FUNCTION = MetricAggFunction.SUM;
  public static final MetricAggFunction DEFAULT_TDIGEST_AGG_FUNCTION = MetricAggFunction.PCT90;
  private static final Logger LOG = LoggerFactory.getLogger(PinotDatasetReader.class);
  
  private static final String BYTES_STRING = "BYTES";

  private final PinotControllerRestClient pinotControllerRestClient;

  @Inject
  public PinotDatasetReader(final PinotThirdEyeDataSourceConfig config, final ThirdEyeDataSourceContext context) {
    this.pinotControllerRestClient = new PinotControllerRestClient(config, context);
  }

  public List<String> getAllTableNames() throws IOException {
    return pinotControllerRestClient.getAllTablesFromPinot();
  }

  public List<DatasetConfigDTO> getAll(final String dataSourceName) throws IOException {
    final List<String> allTables = getAllTableNames();

    final List<DatasetConfigDTO> onboarded = new ArrayList<>();
    for (final String tableName : allTables) {
      try {
        final DatasetConfigDTO datasetConfigDTO = getTable(tableName, dataSourceName);
        onboarded.add(requireNonNull(datasetConfigDTO, "Dataset config is null"));
      } catch (final Exception e) {
        // Catch the exception and continue to onboard other tables
        LOG.error("Failed to onboard table: " + tableName, e);
      }
    }
    return onboarded;
  }

  public DatasetConfigDTO getTable(final String tableName, final String dataSourceName)
      throws IOException {
    final Schema schema = pinotControllerRestClient.getSchemaFromPinot(tableName);
    requireNonNull(schema, "Onboarding Error: schema is null for pinot table: " + tableName);
    checkArgument(!StringUtils.isBlank(schema.getSchemaName()),
        "Onboarding Error: schema name is blank for pinot table: " + tableName);

    final JsonNode tableConfigJson = pinotControllerRestClient
        .getTableConfigFromPinotEndpoint(tableName);
    checkArgument(tableConfigJson != null && !tableConfigJson.isNull(),
        "Onboarding Error: table config is null for pinot table: " + tableName);

    final String timeColumnName = timeColumnFromTableConfig(tableConfigJson);
    // rewrite above if to throw exception instead of returning null
    checkArgument(timeColumnName != null,
        "Onboarding Error: time column is null for pinot table: " + tableName);
    checkArgument(schema.getSpecForTimeColumn(timeColumnName) != null,
        "Onboarding Error: unable to get time column spec in schema for pinot table: " + tableName);

    final Map<String, String> pinotCustomProperties = customConfigsFromTableConfig(tableConfigJson);

    return toDatasetConfigDTO(tableName,
        schema,
        timeColumnName,
        pinotCustomProperties,
        dataSourceName);
  }

  public void prepareDatasetForOnboarding(final String datasetName)
      throws IOException {
    final JsonNode tableConfigJson = pinotControllerRestClient
        .getTableConfigFromPinotEndpoint(datasetName);
    checkArgument(tableConfigJson != null && !tableConfigJson.isNull(),
        "Onboarding Preparation Error: table config is null for pinot table: " + datasetName);

    pinotControllerRestClient.updateTableMaxQPSQuota(datasetName, tableConfigJson);
  }

  public void close() {
    pinotControllerRestClient.close();
  }

  private static DatasetConfigDTO toDatasetConfigDTO(final String dataset,
      final Schema schema,
      final String timeColumnName,
      final Map<String, String> customConfigs,
      final String dataSourceName) {
    final List<MetricFieldSpec> metricSpecs = schema.getMetricFieldSpecs();

    // Create DatasetConfig
    final DatasetConfigDTO datasetConfigDTO = generateDatasetConfig(dataset,
        schema,
        timeColumnName,
        customConfigs,
        dataSourceName);

    // Create MetricConfig
    final List<MetricConfigDTO> metrics = metricSpecs.stream()
        .map(metricFieldSpec -> generateMetricConfig(metricFieldSpec, dataset))
        .collect(Collectors.toList());

    datasetConfigDTO.setMetrics(metrics);
    return datasetConfigDTO;
  }

  private static DatasetConfigDTO generateDatasetConfig(final String dataset, final Schema schema,
      final String timeColumnName,
      final Map<String, String> customConfigs, final String dataSourceName) {
    final List<String> dimensions = schema.getDimensionNames();
    final DateTimeFieldSpec dateTimeFieldSpec = schema.getSpecForTimeColumn(timeColumnName);
    Preconditions.checkNotNull(dateTimeFieldSpec);
    // Create DatasetConfig
    return new DatasetConfigDTO()
        .setDataset(dataset)
        .setDimensions(Templatable.of(dimensions))
        .setDataSource(dataSourceName)
        .setProperties(customConfigs)
        .setActive(Boolean.TRUE)
        .setTimeColumn(dateTimeFieldSpec.getName())
        .setTimeFormat(dateTimeFieldSpec.getFormat())
        .setTimezone(DEFAULT_CHRONOLOGY.getZone().toString());
  }

  private static MetricConfigDTO generateMetricConfig(final MetricFieldSpec metricFieldSpec,
      final String dataset) {
    final MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    final String metric = metricFieldSpec.getName();
    metricConfigDTO.setName(metric);
    metricConfigDTO.setAlias(SpiUtils.constructMetricAlias(dataset, metric));
    metricConfigDTO.setDataset(dataset);
    metricConfigDTO.setActive(Boolean.TRUE);

    final String dataTypeStr = metricFieldSpec.getDataType().toString();
    if (BYTES_STRING.equals(dataTypeStr)) {
      // Assume if the column is BYTES type, use the default TDigest function and set the return data type to double
      metricConfigDTO.setDefaultAggFunction(DEFAULT_TDIGEST_AGG_FUNCTION.toString());
      metricConfigDTO.setDatatype(MetricType.DOUBLE);
    } else {
      metricConfigDTO.setDefaultAggFunction(DEFAULT_AGG_FUNCTION.toString());
      metricConfigDTO.setDatatype(MetricType.valueOf(dataTypeStr));
    }

    return metricConfigDTO;
  }

  public @NonNull String createDemoDataset(final DemoDatasetConfig demoDatasetConfig) throws IOException {
    final String tableName = pinotControllerRestClient.postSchema(demoDatasetConfig.schema(), false, false);
    final String tableNameWithType = pinotControllerRestClient.postTable(demoDatasetConfig.tableConfig());
    pinotControllerRestClient.postIngestFromFile(
        tableNameWithType,
        demoDatasetConfig.batchConfigMapStr(),
        demoDatasetConfig.s3SourceUri()
    );
    
    return tableName;
  }

  private static String timeColumnFromTableConfig(final JsonNode tableConfigJson) {
    final JsonNode timeColumnNode = tableConfigJson.get("segmentsConfig").get("timeColumnName");
    return (timeColumnNode != null && !timeColumnNode.isNull()) ? timeColumnNode.asText() : null;
  }

  /**
   * Returns the map of custom configs of the given dataset from the Pinot table config json.
   */
  private static Map<String, String> customConfigsFromTableConfig(final JsonNode tableConfigJson) {

    Map<String, String> customConfigs = Collections.emptyMap();
    try {
      final JsonNode jsonNode = tableConfigJson.get("metadata").get("customConfigs");
      customConfigs = VANILLA_OBJECT_MAPPER.convertValue(jsonNode, new TypeReference<>() {});
    } catch (final Exception e) {
      LOG.warn("Failed to get custom config from table: {}. Exception:", tableConfigJson, e);
    }
    return customConfigs;
  }
}
