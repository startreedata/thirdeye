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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.plugins.datasource.pinot.restclient.PinotControllerRestClient;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO.DimensionAsMetricProperties;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.spi.metric.MetricType;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.spi.data.DateTimeFieldSpec;
import org.apache.pinot.spi.data.DateTimeFieldSpec.TimeFormat;
import org.apache.pinot.spi.data.DateTimeFormatSpec;
import org.apache.pinot.spi.data.MetricFieldSpec;
import org.apache.pinot.spi.data.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotDatasetOnboarder {

  public static final MetricAggFunction DEFAULT_AGG_FUNCTION = MetricAggFunction.SUM;
  public static final MetricAggFunction DEFAULT_TDIGEST_AGG_FUNCTION = MetricAggFunction.PCT90;
  private static final Logger LOG = LoggerFactory.getLogger(PinotDatasetOnboarder.class);

  /* Use "ROW_COUNT" as the special token for the count(*) metric for a pinot table */
  private static final String ROW_COUNT = "ROW_COUNT";
  private static final Set<String> DIMENSION_SUFFIX_BLACKLIST = new HashSet<>(
      Arrays.asList("_topk", "_approximate", "_tDigest"));
  private static final String BYTES_STRING = "BYTES";
  private static final String NON_ADDITIVE = "non_additive";
  private static final String PINOT_PRE_AGGREGATED_KEYWORD = "*";

  private final PinotControllerRestClient pinotControllerRestClient;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;

  @Inject
  public PinotDatasetOnboarder(
      final PinotControllerRestClient pinotControllerRestClient,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    this.pinotControllerRestClient = pinotControllerRestClient;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
  }

  /**
   * Returns the metric column name
   *
   * @param metricConfig metric config
   * @return column name
   */
  private static String getColumnName(MetricConfigDTO metricConfig) {
    // In dimensionAsMetric case, the metric name will be used in the METRIC_VALUES_COLUMN property of the metric
    if (metricConfig.isDimensionAsMetric()) {
      Map<String, String> metricProperties = metricConfig.getMetricProperties();
      if (MapUtils.isNotEmpty(metricProperties)) {
        return metricProperties.get(DimensionAsMetricProperties.METRIC_VALUES_COLUMN.toString());
      }
    } else {
      return metricConfig.getName();
    }
    throw new IllegalArgumentException(
        String.format("Could not resolve column name for '%s'", metricConfig));
  }

  public static void setDateTimeSpecs(DatasetConfigDTO datasetConfigDTO,
      DateTimeFieldSpec dateTimeFieldSpec) {
    Preconditions.checkNotNull(dateTimeFieldSpec);
    DateTimeFormatSpec formatSpec = new DateTimeFormatSpec(dateTimeFieldSpec.getFormat());
    String timeFormatStr = formatSpec.getTimeFormat().equals(TimeFormat.SIMPLE_DATE_FORMAT) ? String
        .format("%s:%s", TimeFormat.SIMPLE_DATE_FORMAT, formatSpec.getSDFPattern())
        : TimeFormat.EPOCH.toString();
    setDateTimeSpecs(datasetConfigDTO, dateTimeFieldSpec.getName(), timeFormatStr,
        formatSpec.getColumnSize(),
        formatSpec.getColumnUnit());
  }

  public static void setDateTimeSpecs(DatasetConfigDTO datasetConfigDTO, String timeColumnName,
      String timeFormatStr,
      int columnSize, TimeUnit columnUnit) {
    datasetConfigDTO
        .setTimeColumn(timeColumnName)
        .setTimeDuration(columnSize)
        .setTimeUnit(columnUnit)
        .setTimeFormat(timeFormatStr)
        .setTimezone(Constants.DEFAULT_TIMEZONE_STRING);
    // set the data granularity of epoch timestamp dataset to minute-level
    if (datasetConfigDTO.getTimeFormat().equals(TimeSpec.SINCE_EPOCH_FORMAT) && datasetConfigDTO
        .getTimeUnit()
        .equals(TimeUnit.MILLISECONDS) && (datasetConfigDTO.getNonAdditiveBucketSize() == null
        || datasetConfigDTO.getNonAdditiveBucketUnit() == null)) {
      datasetConfigDTO.setNonAdditiveBucketUnit(TimeUnit.MINUTES);
      datasetConfigDTO.setNonAdditiveBucketSize(5);
    }
  }

  public static DatasetConfigDTO generateDatasetConfig(String dataset, Schema schema,
      String timeColumnName,
      Map<String, String> customConfigs, String dataSourceName) {
    List<String> dimensions = schema.getDimensionNames();
    DateTimeFieldSpec dateTimeFieldSpec = schema.getSpecForTimeColumn(timeColumnName);
    // Create DatasetConfig
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(dataset)
        .setDimensions(Templatable.of(dimensions))
        .setDataSource(dataSourceName)
        .setProperties(customConfigs)
        .setActive(Boolean.TRUE);
    setDateTimeSpecs(datasetConfigDTO, dateTimeFieldSpec);
    checkNonAdditive(datasetConfigDTO);
    return datasetConfigDTO;
  }

  /**
   * Check if the dataset is non-additive. If it is, set the additive flag to false and set the
   * pre-aggregated keyword.
   *
   * @param dataset the dataset DTO to check
   */
  static void checkNonAdditive(DatasetConfigDTO dataset) {
    if (dataset.isAdditive() && dataset.getDataset().endsWith(NON_ADDITIVE)) {
      dataset.setAdditive(false);
      dataset.setPreAggregatedKeyword(PINOT_PRE_AGGREGATED_KEYWORD);
    }
  }

  public static MetricConfigDTO generateMetricConfig(MetricFieldSpec metricFieldSpec,
      String dataset) {
    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    String metric = metricFieldSpec.getName();
    metricConfigDTO.setName(metric);
    metricConfigDTO.setAlias(SpiUtils.constructMetricAlias(dataset, metric));
    metricConfigDTO.setDataset(dataset);
    metricConfigDTO.setActive(Boolean.TRUE);

    String dataTypeStr = metricFieldSpec.getDataType().toString();
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

  public ImmutableList<String> getAllTables() throws IOException {
    return ImmutableList.copyOf(pinotControllerRestClient.getAllTablesFromPinot());
  }

  public List<DatasetConfigDTO> onboardAll(final String dataSourceName) throws IOException {
    final List<String> allTables = getAllTables();
    deactivateDatasets(allTables, dataSourceName);

    List<DatasetConfigDTO> onboarded = new ArrayList<>();
    for (String tableName : allTables) {
      final DatasetConfigDTO datasetConfigDTO = onboardTable(tableName, dataSourceName);
      if (datasetConfigDTO != null) {
        onboarded.add(datasetConfigDTO);
      }
    }
    return onboarded;
  }

  public DatasetConfigDTO onboardTable(final String tableName, final String dataSourceName)
      throws IOException {
    final Schema schema = pinotControllerRestClient.getSchemaFromPinot(tableName);
    if (schema == null) {
      LOG.error("schema not found for pinot table: " + tableName);
      return null;
    }

    final JsonNode tableConfigJson = pinotControllerRestClient
        .getTableConfigFromPinotEndpoint(tableName);
    if (tableConfigJson == null || tableConfigJson.isNull()) {
      LOG.error("table config is null for pinot table: " + tableName);
      return null;
    }

    final String timeColumnName = pinotControllerRestClient
        .extractTimeColumnFromPinotTable(tableConfigJson);
    if (!pinotControllerRestClient.verifySchemaCorrectness(schema, timeColumnName)) {
      LOG.info("Incorrect schema in pinot table: " + tableName);
      return null;
    }

    final Map<String, String> pinotCustomProperties = pinotControllerRestClient
        .extractCustomConfigsFromPinotTable(tableConfigJson);

    final DatasetConfigDTO existingDataset = datasetConfigManager.findByDataset(tableName);
    return addPinotDataset(tableName,
        schema,
        timeColumnName,
        pinotCustomProperties,
        existingDataset,
        dataSourceName);
  }

  public void deactivateDatasets(List<String> allDatasets, final String dataSourceName) {
    requireNonNull(dataSourceName, "data source name is null");

    final Set<String> datasets = new HashSet<>(allDatasets);
    datasetConfigManager.findAll()
        .stream()
        .filter(dataset -> dataSourceName.equals(dataset.getDataSource()))
        .filter(dataset -> shouldDeactivateDataset(dataset, datasets))
        .peek(dataset -> dataset.setActive(Boolean.FALSE))
        .forEach(datasetConfigManager::save);
  }

  /**
   * Adds a dataset to the thirdeye database
   */
  public DatasetConfigDTO addPinotDataset(String dataset,
      Schema schema,
      String timeColumnName,
      Map<String, String> customConfigs,
      DatasetConfigDTO datasetConfig,
      final String dataSourceName) {
    if (datasetConfig == null) {
      LOG.info("Dataset {} is new, adding it to thirdeye", dataset);
      return addNewDataset(dataset,
          schema,
          timeColumnName,
          customConfigs,
          dataSourceName);
    } else {
      LOG.info("Dataset {} already exists, checking for updates", dataset);
      return refreshOldDataset(dataset, schema, timeColumnName, customConfigs, datasetConfig);
    }
  }

  /**
   * Adds a new dataset to the thirdeye database
   */
  private DatasetConfigDTO addNewDataset(String dataset,
      Schema schema,
      String timeColumnName,
      Map<String, String> customConfigs,
      final String dataSourceName) {
    List<MetricFieldSpec> metricSpecs = schema.getMetricFieldSpecs();

    // Create DatasetConfig
    DatasetConfigDTO datasetConfigDTO = generateDatasetConfig(dataset, schema, timeColumnName, customConfigs, dataSourceName);
    LOG.info("Creating dataset for {}", dataset);
    datasetConfigManager.save(datasetConfigDTO);

    // Create MetricConfig
    for (MetricFieldSpec metricFieldSpec : metricSpecs) {
      MetricConfigDTO metricConfigDTO = generateMetricConfig(metricFieldSpec, dataset);
      LOG.info("Creating metric {} for {}", metricConfigDTO.getName(), dataset);
      metricConfigManager.save(metricConfigDTO);
    }
    return datasetConfigDTO;
  }

  /**
   * Refreshes an existing dataset in the thirdeye database
   * with any dimension/metric changes from pinot schema
   */
  private DatasetConfigDTO refreshOldDataset(String dataset,
      Schema schema,
      String timeColumnName,
      Map<String, String> customConfigs,
      DatasetConfigDTO datasetConfig) {
    checkDimensionChanges(dataset, datasetConfig, schema);
    checkMetricChanges(dataset, schema);
    checkTimeFieldChanges(datasetConfig, schema, timeColumnName);
    appendNewCustomConfigs(datasetConfig, customConfigs);
    checkNonAdditive(datasetConfig);
    datasetConfig.setActive(Boolean.TRUE);
    return datasetConfig;
  }

  private void checkDimensionChanges(String dataset, DatasetConfigDTO datasetConfig,
      Schema schema) {
    LOG.info("Checking for dimensions changes in {}", dataset);
    List<String> schemaDimensions = schema.getDimensionNames();
    List<String> datasetDimensions = optional(datasetConfig.getDimensions()).map(Templatable::value)
        .orElse(new ArrayList<>());

    // remove blacklisted dimensions
    Iterator<String> itDimension = schemaDimensions.iterator();
    while (itDimension.hasNext()) {
      String dimName = itDimension.next();
      for (String suffix : DIMENSION_SUFFIX_BLACKLIST) {
        if (dimName.endsWith(suffix)) {
          itDimension.remove();
          break;
        }
      }
    }

    // in dimensionAsMetric case, the dimension name will be used in the METRIC_NAMES_COLUMNS property of the metric
    List<String> dimensionsAsMetrics = new ArrayList<>();
    List<MetricConfigDTO> metricConfigs = metricConfigManager.findByDataset(dataset);
    for (MetricConfigDTO metricConfig : metricConfigs) {
      if (metricConfig.isDimensionAsMetric()) {
        Map<String, String> metricProperties = metricConfig.getMetricProperties();
        if (MapUtils.isNotEmpty(metricProperties)) {
          String metricNames = metricProperties
              .get(DimensionAsMetricProperties.METRIC_NAMES_COLUMNS.toString());
          if (StringUtils.isNotBlank(metricNames)) {
            dimensionsAsMetrics.addAll(Lists
                .newArrayList(metricNames.split(MetricConfigDTO.METRIC_PROPERTIES_SEPARATOR)));
          }
        }
      }
    }

    // create diff
    List<String> dimensionsToAdd = new ArrayList<>();
    List<String> dimensionsToRemove = new ArrayList<>();

    // dimensions which are new in the pinot schema
    for (String dimensionName : schemaDimensions) {
      if (!datasetDimensions.contains(dimensionName) && !dimensionsAsMetrics
          .contains(dimensionName)) {
        dimensionsToAdd.add(dimensionName);
      }
    }

    // dimensions which are removed from pinot schema
    for (String dimensionName : datasetDimensions) {
      if (!schemaDimensions.contains(dimensionName)) {
        dimensionsToRemove.add(dimensionName);
      }
    }

    // apply diff
    if (CollectionUtils.isNotEmpty(dimensionsToAdd) || CollectionUtils
        .isNotEmpty(dimensionsToRemove)) {
      datasetDimensions.addAll(dimensionsToAdd);
      datasetDimensions.removeAll(dimensionsToRemove);
      datasetConfig.setDimensions(Templatable.of(datasetDimensions));

      if (!datasetConfig.isAdditive()
          && CollectionUtils.isNotEmpty(datasetConfig.getDimensionsHaveNoPreAggregation())) {
        List<String> dimensionsHaveNoPreAggregation = datasetConfig
            .getDimensionsHaveNoPreAggregation();
        dimensionsHaveNoPreAggregation.removeAll(dimensionsToRemove);
        datasetConfig.setDimensionsHaveNoPreAggregation(dimensionsHaveNoPreAggregation);
      }
      LOG.info("Added dimensions {}, removed {}", dimensionsToAdd, dimensionsToRemove);
      datasetConfigManager.update(datasetConfig);
    }
  }

  private void checkMetricChanges(String dataset, Schema schema) {
    LOG.info("Checking for metric changes in {}", dataset);

    // Fetch metrics from Thirdeye
    List<MetricConfigDTO> datasetMetricConfigs = metricConfigManager
        .findByDataset(dataset);

    // Fetch metrics from Pinot
    List<MetricFieldSpec> schemaMetricSpecs = schema.getMetricFieldSpecs();

    // Index metric names
    Set<String> datasetMetricNames = new HashSet<>();
    for (MetricConfigDTO metricConfig : datasetMetricConfigs) {
      datasetMetricNames.add(getColumnName(metricConfig));
    }

    Set<String> schemaMetricNames = new HashSet<>();
    for (MetricFieldSpec metricSpec : schemaMetricSpecs) {
      schemaMetricNames.add(metricSpec.getName());
    }

    // add new metrics to ThirdEye
    for (MetricFieldSpec metricSpec : schemaMetricSpecs) {
      if (!datasetMetricNames.contains(metricSpec.getName())) {
        MetricConfigDTO metricConfigDTO = generateMetricConfig(metricSpec, dataset);
        LOG.info("Creating metric {} in {}", metricSpec.getName(), dataset);
        metricConfigManager.save(metricConfigDTO);
      }
    }

    // audit existing metrics in ThirdEye
    for (MetricConfigDTO metricConfig : datasetMetricConfigs) {
      if (!schemaMetricNames.contains(getColumnName(metricConfig))) {
        if (metricConfig.getDerivedMetricExpression() == null && !metricConfig.getName()
            .equals(ROW_COUNT)) {
          // if metric is removed from schema and not a derived/row_count metric, deactivate it
          LOG.info("Deactivating metric {} in {}", metricConfig.getName(), dataset);
          metricConfig.setActive(false);
          metricConfigManager.save(metricConfig);
        }
      } else {
        if (!Boolean.TRUE.equals(metricConfig.getActive())) {
          LOG.info("Activating metric {} in {}", metricConfig.getName(), dataset);
          metricConfig.setActive(true);
          metricConfigManager.save(metricConfig);
        }
      }
    }

    // TODO: write a tool, which given a metric id, erases all traces of that metric from the database
    // This will include:
    // 1) delete the metric from metricConfigs
    // 2) remove any derived metrics which use the deleted metric
    // 3) remove the metric, and derived metrics from all dashboards
    // 4) remove any anomaly functions associated with the metric
    // 5) remove any alerts associated with these anomaly functions

  }

  private boolean shouldDeactivateDataset(DatasetConfigDTO datasetConfigDTO, Set<String> datasets) {
    if (!datasets.contains(datasetConfigDTO.getDataset())) {
      List<MetricConfigDTO> metrics = metricConfigManager.findByDataset(datasetConfigDTO.getDataset());
      int metricCount = metrics.size();
      for (MetricConfigDTO metric : metrics) {
        if (metric.getDerivedMetricExpression() == null && !metric.getName().equals(ROW_COUNT)) {
          metric.setActive(false);
          metricConfigManager.save(metric);
          metricCount--;
        }
      }
      return metricCount == 0;
    } else {
      return false;
    }
  }

  private void checkTimeFieldChanges(DatasetConfigDTO datasetConfig, Schema schema,
      String timeColumnName) {
    DateTimeFieldSpec dateTimeFieldSpec = schema.getSpecForTimeColumn(timeColumnName);
    DateTimeFormatSpec formatSpec = new DateTimeFormatSpec(dateTimeFieldSpec.getFormat());
    String timeFormatStr = formatSpec.getTimeFormat().equals(TimeFormat.SIMPLE_DATE_FORMAT) ? String
        .format("%s:%s", TimeFormat.SIMPLE_DATE_FORMAT, formatSpec.getSDFPattern())
        : TimeFormat.EPOCH.toString();
    if (!datasetConfig.getTimeColumn().equals(timeColumnName)
        || !datasetConfig.getTimeFormat().equals(timeFormatStr)
        || datasetConfig.bucketTimeGranularity().getUnit() != formatSpec.getColumnUnit()
        || datasetConfig.bucketTimeGranularity().getSize() != formatSpec.getColumnSize()) {
      setDateTimeSpecs(datasetConfig, timeColumnName, timeFormatStr,
          formatSpec.getColumnSize(),
          formatSpec.getColumnUnit());
      datasetConfigManager.update(datasetConfig);
      LOG.info("Refreshed time field. name = {}, format = {}, type = {}, unit size = {}.",
          timeColumnName, timeFormatStr, formatSpec.getColumnUnit(), formatSpec.getColumnSize());
    }
  }

  /**
   * This method ensures that the given custom configs exist in the dataset config and their value
   * are the same.
   *
   * @param datasetConfig the current dataset config to be appended with new custom config.
   * @param customConfigs the custom config to be matched with that from dataset config.
   *
   *         TODO: Remove out-of-date Pinot custom config from dataset config.
   */
  private void appendNewCustomConfigs(DatasetConfigDTO datasetConfig,
      Map<String, String> customConfigs) {
    if (MapUtils.isNotEmpty(customConfigs)) {
      Map<String, String> properties = datasetConfig.getProperties();
      boolean hasUpdate = false;
      if (MapUtils.isEmpty(properties)) {
        properties = customConfigs;
        hasUpdate = true;
      } else {
        for (Map.Entry<String, String> customConfig : customConfigs.entrySet()) {
          String configKey = customConfig.getKey();
          String configValue = customConfig.getValue();

          if (!properties.containsKey(configKey)) {
            properties.put(configKey, configValue);
            hasUpdate = true;
          }
        }
      }
      if (hasUpdate) {
        datasetConfig.setProperties(properties);
        datasetConfigManager.update(datasetConfig);
      }
    }
  }
}
