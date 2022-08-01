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
package ai.startree.thirdeye.plugins.datasource.auto.onboard;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.spi.metric.MetricType;
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

  // This is the expected delay for the hourly/daily data source.
  // 1 hour delay means we always expect to have 1 hour's before's data.
  public static final TimeGranularity DEFAULT_HOURLY_EXPECTED_DELAY = new TimeGranularity(1,
      TimeUnit.HOURS);
  public static final TimeGranularity DEFAULT_DAILY_EXPECTED_DELAY = new TimeGranularity(24,
      TimeUnit.HOURS);
  public static final MetricAggFunction DEFAULT_AGG_FUNCTION = MetricAggFunction.SUM;
  public static final MetricAggFunction DEFAULT_TDIGEST_AGG_FUNCTION = MetricAggFunction.PCT90;

  private static final String BYTES_STRING = "BYTES";
  private static final String NON_ADDITIVE = "non_additive";
  private static final String PINOT_PRE_AGGREGATED_KEYWORD = "*";

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
    datasetConfigDTO.setTimeColumn(timeColumnName);
    datasetConfigDTO.setTimeDuration(columnSize);
    datasetConfigDTO.setTimeUnit(columnUnit);
    datasetConfigDTO.setTimeFormat(timeFormatStr);
    datasetConfigDTO.setTimezone(Constants.DEFAULT_TIMEZONE_STRING);
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
    datasetConfigDTO.setActive(Boolean.TRUE);
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
}
