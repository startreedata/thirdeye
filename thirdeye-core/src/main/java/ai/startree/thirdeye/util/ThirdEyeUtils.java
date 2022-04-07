/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import static ai.startree.thirdeye.spi.Constants.GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME;
import static ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO.TIME_SERIES_SNAPSHOT_KEY;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.CoreConstants;
import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.MetricDataset;
import ai.startree.thirdeye.detection.anomaly.views.AnomalyTimelinesView;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.rootcause.impl.MetricEntity;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThirdEyeUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeUtils.class);

  /**
   * Returns the time spec of the buckets (data points) in the specified dataset config. For
   * additive dataset, this
   * method returns the same time spec as getTimestampTimeSpecFromDatasetConfig; however, for
   * non-additive dataset,
   * this method return the time spec for buckets (data points) instead of the one for the timestamp
   * in the backend
   * database. For example, the data points of a non-additive dataset could be 5-MINUTES
   * granularity, but timestamp's
   * granularity could be 1-Milliseconds. For additive dataset, the discrepancy is not an issue, but
   * it could be
   * a problem for non-additive dataset.
   *
   * @param datasetConfig the given dataset config
   * @return the time spec of the buckets (data points) in the specified dataset config.
   */
  public static TimeSpec getTimeSpecFromDatasetConfig(DatasetConfigDTO datasetConfig) {
    String timeFormat = SpiUtils.getTimeFormatString(datasetConfig);
    return new TimeSpec(datasetConfig.getTimeColumn(),
        new TimeGranularity(datasetConfig.bucketTimeGranularity()), timeFormat);
  }

  public static MetricExpression getMetricExpressionFromMetricConfig(MetricConfigDTO metricConfig) {
    String expression = optional(metricConfig.getDerivedMetricExpression())
        .orElse(metricConfig.getName());
    return new MetricExpression(metricConfig.getName(),
        expression,
        metricConfig.getDefaultAggFunction(),
        metricConfig.getDataset());
  }

  public static String getDerivedMetricExpression(String metricExpressionName,
      String dataset,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws ExecutionException {
    final MetricDataset metricDataset = new MetricDataset(metricExpressionName, dataset);

    MetricConfigDTO metricConfig = thirdEyeCacheRegistry
        .getMetricConfigCache()
        .get(metricDataset);

    String derivedMetricExpression;
    if (metricConfig != null && metricConfig.getDerivedMetricExpression() != null) {
      derivedMetricExpression = metricConfig.getDerivedMetricExpression();
    } else {
      derivedMetricExpression = metricConfig.getName();
    }
    return derivedMetricExpression;
  }

  public static DatasetConfigDTO getDatasetConfigFromName(String dataset,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    DatasetConfigDTO datasetConfig = null;
    try {
      datasetConfig = thirdEyeCacheRegistry
          .getDatasetConfigCache().get(dataset);
    } catch (ExecutionException e) {
      LOG.error("Exception in getting dataset config {} from cache", dataset, e);
    }
    return datasetConfig;
  }

  public static List<DatasetConfigDTO> getDatasetConfigsFromMetricUrn(String metricUrn,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    MetricEntity me = MetricEntity.fromURN(metricUrn);
    MetricConfigDTO metricConfig = metricConfigManager
        .findById(me.getId());
    if (metricConfig == null) {
      return new ArrayList<>();
    }
    if (metricConfig.getDerivedMetricExpression() == null) {
      return Collections
          .singletonList(datasetConfigManager.findByDataset(metricConfig.getDataset()));
    } else {
      MetricExpression metricExpression = ThirdEyeUtils
          .getMetricExpressionFromMetricConfig(metricConfig);
      List<MetricFunction> functions = metricExpression.computeMetricFunctions(
          thirdEyeCacheRegistry);
      return functions.stream().map(
          f -> datasetConfigManager.findByDataset(f.getDataset())).collect(Collectors.toList());
    }
  }

  /**
   * Get rounded double value, according to the value of the double.
   * Max rounding will be up to 4 decimals
   * For values >= 0.1, use 2 decimals (eg. 123, 2.5, 1.26, 0.5, 0.162)
   * For values < 0.1, use 3 decimals (eg. 0.08, 0.071, 0.0123)
   *
   * @param value any double value
   * @return the rounded double value
   */
  public static Double getRoundedDouble(Double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return Double.NaN;
    }
    if (value >= 0.1) {
      return Math.round(value * (Math.pow(10, 2))) / (Math.pow(10, 2));
    } else {
      return Math.round(value * (Math.pow(10, 3))) / (Math.pow(10, 3));
    }
  }

  /**
   * Get rounded double value, according to the value of the double.
   * Max rounding will be upto 4 decimals
   * For values gte 0.1, use ##.## (eg. 123, 2.5, 1.26, 0.5, 0.162)
   * For values lt 0.1 and gte 0.01, use ##.### (eg. 0.08, 0.071, 0.0123)
   * For values lt 0.01 and gte 0.001, use ##.#### (eg. 0.001, 0.00367)
   * This function ensures we don't prematurely round off double values to a fixed format, and make
   * it 0.00 or lose out information
   */
  public static String getRoundedValue(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      if (Double.isNaN(value)) {
        return Double.toString(Double.NaN);
      }
      if (value > 0) {
        return Double.toString(Double.POSITIVE_INFINITY);
      } else {
        return Double.toString(Double.NEGATIVE_INFINITY);
      }
    }
    StringBuffer decimalFormatBuffer = new StringBuffer(CoreConstants.TWO_DECIMALS_FORMAT);
    double compareValue = 0.1;
    while (value > 0 && value < compareValue && !decimalFormatBuffer.toString().equals(
        CoreConstants.MAX_DECIMALS_FORMAT)) {
      decimalFormatBuffer.append(CoreConstants.DECIMALS_FORMAT_TOKEN);
      compareValue = compareValue * 0.1;
    }
    DecimalFormat decimalFormat = new DecimalFormat(decimalFormatBuffer.toString());

    return decimalFormat.format(value);
  }

  //TODO: currently assuming all metrics in one request are for the same data source
  // It would be better to not assume that, and split the thirdeye request into more requests depending upon the data sources
  public static String getDataSourceFromMetricFunctions(List<MetricFunction> metricFunctions) {
    String dataSource = null;
    for (MetricFunction metricFunction : metricFunctions) {
      String functionDatasetDatasource = metricFunction.getDatasetConfig().getDataSource();
      if (dataSource == null) {
        dataSource = functionDatasetDatasource;
      } else if (!dataSource.equals(functionDatasetDatasource)) {
        throw new IllegalStateException(
            "All metric funcitons of one request must belong to the same data source. "
                + dataSource + " is not equal to" + functionDatasetDatasource);
      }
    }
    return dataSource;
  }

  /**
   * Prints messages and stack traces of the given list of exceptions in a string.
   *
   * @param exceptions the list of exceptions to be printed.
   * @param maxWordCount the length limitation of the string; set to 0 to remove the limitation.
   * @return the string that contains the messages and stack traces of the given exceptions.
   */
  public static String exceptionsToString(List<Exception> exceptions, int maxWordCount) {
    String message = "";
    if (CollectionUtils.isNotEmpty(exceptions)) {
      StringBuilder sb = new StringBuilder();
      for (Exception exception : exceptions) {
        sb.append(ExceptionUtils.getStackTrace(exception));
        if (maxWordCount > 0 && sb.length() > maxWordCount) {
          message = sb.substring(0, maxWordCount) + "\n...";
          break;
        }
      }
      if (message.equals("")) {
        message = sb.toString();
      }
    }
    return message;
  }

  /**
   * Merge child's properties into parent's properties.
   * If the property exists in both then use parent's property.
   * For property = "detectorComponentName", combine the parent and child.
   *
   * @param parent The parent anomaly's properties.
   * @param child The child anomaly's properties.
   */
  public static void mergeAnomalyProperties(Map<String, String> parent, Map<String, String> child) {
    for (String key : child.keySet()) {
      if (!parent.containsKey(key)) {
        parent.put(key, child.get(key));
      } else {
        // combine detectorComponentName
        if (key.equals(GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME)) {
          String component = ThirdEyeUtils.combineComponents(parent.get(
              GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME), child.get(
              GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME));
          parent.put(GROUP_WRAPPER_PROP_DETECTOR_COMPONENT_NAME, component);
        }
        // combine time series snapshot of parent and child anomalies
        if (key.equals(MergedAnomalyResultDTO.TIME_SERIES_SNAPSHOT_KEY)) {
          try {
            AnomalyTimelinesView parentTimeSeries = AnomalyTimelinesView
                .fromJsonString(parent.get(TIME_SERIES_SNAPSHOT_KEY));
            AnomalyTimelinesView childTimeSeries = AnomalyTimelinesView
                .fromJsonString(child.get(TIME_SERIES_SNAPSHOT_KEY));
            parent.put(TIME_SERIES_SNAPSHOT_KEY,
                mergeTimeSeriesSnapshot(parentTimeSeries, childTimeSeries).toJsonString());
          } catch (Exception e) {
            LOG.warn("Unable to merge time series, so skipping...", e);
          }
        }
      }
    }
  }

  /**
   * Combine two components with comma separated.
   * For example, will combine "component1" and "component2" into "component1, component2".
   *
   * @param component1 The first component.
   * @param component2 The second component.
   * @return The combined components.
   */
  private static String combineComponents(String component1, String component2) {
    List<String> components = new ArrayList<>();
    components.addAll(Arrays.asList(component1.split(
        CoreConstants.PROP_DETECTOR_COMPONENT_NAME_DELIMETER)));
    components.addAll(Arrays.asList(component2.split(
        CoreConstants.PROP_DETECTOR_COMPONENT_NAME_DELIMETER)));
    return components.stream().distinct().collect(Collectors.joining(
        CoreConstants.PROP_DETECTOR_COMPONENT_NAME_DELIMETER));
  }

  /**
   * Parse job name to get the detection id
   */
  public static long getDetectionIdFromJobName(String jobName) {
    String[] parts = jobName.split("_");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid job name: " + jobName);
    }
    return Long.parseLong(parts[1]);
  }

  /**
   * A helper function to merge time series snapshot of two anomalies. This function assumes that
   * the time series of
   * both parent and child anomalies are aligned with the metric granularity boundary.
   *
   * @param parent time series snapshot of parent anomaly
   * @param child time series snapshot of parent anaomaly
   * @return merged time series snapshot based on timestamps
   */
  private static AnomalyTimelinesView mergeTimeSeriesSnapshot(AnomalyTimelinesView parent,
      AnomalyTimelinesView child) {
    AnomalyTimelinesView mergedTimeSeriesSnapshot = new AnomalyTimelinesView();
    int i = 0;
    int j = 0;
    while (i < parent.getTimeBuckets().size() && j < child.getTimeBuckets().size()) {
      long parentTime = parent.getTimeBuckets().get(i).getCurrentStart();
      long childTime = child.getTimeBuckets().get(j).getCurrentStart();
      if (parentTime == childTime) {
        // use the values in parent anomalies when the time series overlap
        mergedTimeSeriesSnapshot.addTimeBuckets(parent.getTimeBuckets().get(i));
        mergedTimeSeriesSnapshot.addCurrentValues(parent.getCurrentValues().get(i));
        mergedTimeSeriesSnapshot.addBaselineValues(parent.getBaselineValues().get(i));
        i++;
        j++;
      } else if (parentTime < childTime) {
        mergedTimeSeriesSnapshot.addTimeBuckets(parent.getTimeBuckets().get(i));
        mergedTimeSeriesSnapshot.addCurrentValues(parent.getCurrentValues().get(i));
        mergedTimeSeriesSnapshot.addBaselineValues(parent.getBaselineValues().get(i));
        i++;
      } else {
        mergedTimeSeriesSnapshot.addTimeBuckets(child.getTimeBuckets().get(j));
        mergedTimeSeriesSnapshot.addCurrentValues(child.getCurrentValues().get(j));
        mergedTimeSeriesSnapshot.addBaselineValues(child.getBaselineValues().get(j));
        j++;
      }
    }
    while (i < parent.getTimeBuckets().size()) {
      mergedTimeSeriesSnapshot.addTimeBuckets(parent.getTimeBuckets().get(i));
      mergedTimeSeriesSnapshot.addCurrentValues(parent.getCurrentValues().get(i));
      mergedTimeSeriesSnapshot.addBaselineValues(parent.getBaselineValues().get(i));
      i++;
    }
    while (j < child.getTimeBuckets().size()) {
      mergedTimeSeriesSnapshot.addTimeBuckets(child.getTimeBuckets().get(j));
      mergedTimeSeriesSnapshot.addCurrentValues(child.getCurrentValues().get(j));
      mergedTimeSeriesSnapshot.addBaselineValues(child.getBaselineValues().get(j));
      j++;
    }
    mergedTimeSeriesSnapshot.getSummary().putAll(parent.getSummary());
    for (String key : child.getSummary().keySet()) {
      if (!mergedTimeSeriesSnapshot.getSummary().containsKey(key)) {
        mergedTimeSeriesSnapshot.getSummary().put(key, child.getSummary().get(key));
      }
    }
    return mergedTimeSeriesSnapshot;
  }
}
