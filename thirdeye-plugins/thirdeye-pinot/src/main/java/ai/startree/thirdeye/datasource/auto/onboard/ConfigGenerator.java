/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.auto.onboard;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.detection.metric.MetricType;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.spi.data.DateTimeFieldSpec;
import org.apache.pinot.spi.data.DateTimeFieldSpec.TimeFormat;
import org.apache.pinot.spi.data.DateTimeFormatSpec;
import org.apache.pinot.spi.data.MetricFieldSpec;
import org.apache.pinot.spi.data.Schema;

public class ConfigGenerator {

  private static final String PDT_TIMEZONE = "US/Pacific";
  private static final String BYTES_STRING = "BYTES";
  private static final String NON_ADDITIVE = "non_additive";
  private static final String PINOT_PRE_AGGREGATED_KEYWORD = "*";

  public static void setDateTimeSpecs(DatasetConfigDTO datasetConfigDTO,
      DateTimeFieldSpec dateTimeFieldSpec) {
    Preconditions.checkNotNull(dateTimeFieldSpec);
    DateTimeFormatSpec formatSpec = new DateTimeFormatSpec(dateTimeFieldSpec.getFormat());
    String timeFormatStr = formatSpec.getTimeFormat().equals(TimeFormat.SIMPLE_DATE_FORMAT) ? String
        .format("%s:%s", TimeFormat.SIMPLE_DATE_FORMAT.toString(), formatSpec.getSDFPattern())
        : TimeFormat.EPOCH.toString();
    setDateTimeSpecs(datasetConfigDTO, dateTimeFieldSpec.getName(), timeFormatStr,
        formatSpec.getColumnSize(),
        formatSpec.getColumnUnit());
  }

  public static void setDateTimeSpecs(DatasetConfigDTO datasetConfigDTO, String timeColumnName,
      String timeFormatStr,
      int columnSize, TimeUnit columnUnit) {
    datasetConfigDTO.setTimeColumn(timeColumnName);
    datasetConfigDTO.setTimeDuration(columnSize);
    datasetConfigDTO.setTimeUnit(columnUnit);
    datasetConfigDTO.setTimeFormat(timeFormatStr);
    datasetConfigDTO.setExpectedDelay(getExpectedDelayFromTimeunit(columnUnit));
    datasetConfigDTO.setTimezone(PDT_TIMEZONE);
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
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(dataset);
    datasetConfigDTO.setDimensions(dimensions);
    setDateTimeSpecs(datasetConfigDTO, dateTimeFieldSpec);
    datasetConfigDTO.setDataSource(dataSourceName);
    datasetConfigDTO.setProperties(customConfigs);
    checkNonAdditive(datasetConfigDTO);
    return datasetConfigDTO;
  }

  private static TimeGranularity getExpectedDelayFromTimeunit(TimeUnit timeUnit) {
    TimeGranularity expectedDelay = null;
    switch (timeUnit) {
      case HOURS:
      case MILLISECONDS:
      case MINUTES:
      case SECONDS:
        expectedDelay = DatasetConfigDTO.DEFAULT_HOURLY_EXPECTED_DELAY;
        break;
      case DAYS:
      default:
        expectedDelay = DatasetConfigDTO.DEFAULT_DAILY_EXPECTED_DELAY;
        break;
    }
    return expectedDelay;
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

    String dataTypeStr = metricFieldSpec.getDataType().toString();
    if (BYTES_STRING.equals(dataTypeStr)) {
      // Assume if the column is BYTES type, use the default TDigest function and set the return data type to double
      metricConfigDTO.setDefaultAggFunction(MetricConfigDTO.DEFAULT_TDIGEST_AGG_FUNCTION);
      metricConfigDTO.setDatatype(MetricType.DOUBLE);
    } else {
      metricConfigDTO.setDatatype(MetricType.valueOf(dataTypeStr));
    }

    return metricConfigDTO;
  }
}
