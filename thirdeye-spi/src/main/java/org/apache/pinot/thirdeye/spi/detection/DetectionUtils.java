/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.spi.detection;

import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.common.collect.Multimap;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.dimension.DimensionMap;
import org.apache.pinot.thirdeye.spi.detection.model.InputData;
import org.apache.pinot.thirdeye.spi.detection.model.InputDataSpec;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.Baseline;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.PeriodType;

public class DetectionUtils {

  // TODO anomaly should support multimap
  public static DimensionMap toFilterMap(final Multimap<String, String> filters) {
    final DimensionMap map = new DimensionMap();
    for (final Map.Entry<String, Collection<String>> entry : filters.asMap().entrySet()) {
      map.put(entry.getKey(), String.join(", ", entry.getValue()));
    }
    return map;
  }

  // Check if a string is a component reference
  public static boolean isReferenceName(final String key) {
    return key.startsWith("$");
  }

  // Extracts the component key from the reference key
  // e.g., "$myRule:ALGORITHM" -> "myRule:ALGORITHM"
  public static String getComponentKey(final String componentRefKey) {
    if (isReferenceName(componentRefKey)) {
      return componentRefKey.substring(1);
    } else {
      throw new IllegalArgumentException("not a component reference key. should starts with $");
    }
  }

  // Extracts the component type from the component key
  // e.g., "myRule:ALGORITHM" -> "ALGORITHM"
  public static String getComponentType(final String componentKey) {
    if (componentKey != null && componentKey.contains(":")) {
      return componentKey.substring(componentKey.lastIndexOf(":") + 1);
    }
    throw new IllegalArgumentException(
        "componentKey is invalid; must be of type componentName:type");
  }

  // get the spec class name for a component class
  public static String getSpecClassName(final Class<BaseComponent> componentClass) {
    final ParameterizedType genericSuperclass = (ParameterizedType) componentClass
        .getGenericInterfaces()[0];
    return (genericSuperclass.getActualTypeArguments()[0].getTypeName());
  }

  /**
   * Helper for creating a list of anomalies from a boolean series.
   *
   * @param slice metric slice
   * @param df time series with COL_TIME and at least one boolean value series
   * @param seriesName name of the value series
   * @param monitoringGranularityPeriod the monitoring granularity period
   * @param dataset dataset config for the metric
   * @return list of anomalies
   */
  @Deprecated
  public static List<MergedAnomalyResultDTO> makeAnomalies(final MetricSlice slice,
      final DataFrame df,
      final String seriesName,
      final Period monitoringGranularityPeriod,
      final DatasetConfigDTO dataset) {
    return buildAnomalies(slice,
        df,
        seriesName,
        optional(dataset).map(DatasetConfigDTO::getTimezone).orElse(null),
        monitoringGranularityPeriod
    );
  }

  @Deprecated
  public static List<MergedAnomalyResultDTO> makeAnomalies(final MetricSlice slice,
      final DataFrame df,
      final String seriesName) {
    return makeAnomalies(slice, df, seriesName, null, null);
  }

  public static List<MergedAnomalyResultDTO> buildAnomalies(final MetricSlice slice,
      final DataFrame df,
      final String seriesName,
      final String datasetTimezone,
      final Period monitoringGranularityPeriod) {
    if (df.isEmpty()) {
      return Collections.emptyList();
    }

    final List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    final LongSeries sTime = df.getLongs(DataFrame.COL_TIME);
    final BooleanSeries isAnomalySeries = df.getBooleans(seriesName);
    final DoubleSeries currentSeries = df.contains(DataFrame.COL_CURRENT)
        ? df.getDoubles(DataFrame.COL_CURRENT)
        : null;
    final DoubleSeries baselineSeries = df.contains(DataFrame.COL_VALUE)
        ? df.getDoubles(DataFrame.COL_VALUE)
        : null;

    int lastStart = -1;
    double currSum = 0;
    int currCount = 0;
    double baselineSum = 0;
    int baselineCount = 0;

    for (int i = 0; i < df.size(); i++) {
      if (isAnomalySeries.isNull(i) || !BooleanSeries.booleanValueOf(isAnomalySeries.get(i))) {
        // end of a run
        if (lastStart >= 0) {
          long start = sTime.get(lastStart);
          long end = sTime.get(i);
          final MergedAnomalyResultDTO anomaly = makeAnomaly(slice.withStart(start).withEnd(end));
          if (currCount > 0) {
            anomaly.setAvgCurrentVal(currSum / currCount);
          }
          if (baselineCount > 0) {
            anomaly.setAvgBaselineVal(baselineSum / baselineCount);
          }
          anomalies.add(anomaly);

          // reset variables for next anomaly
          currSum = 0;
          currCount = 0;
          baselineSum = 0;
          baselineCount = 0;
        }
        lastStart = -1;
      } else {
        // start of a run
        if (lastStart < 0) {
          lastStart = i;
        }

        if (currentSeries != null && !currentSeries.isNull(i)) {
          final double currValue = currentSeries.getDouble(i);
          currSum += currValue;
          ++currCount;
        }
        if (baselineSeries != null && !baselineSeries.isNull(i)) {
          final double baselineValue = baselineSeries.getDouble(i);
          baselineSum += baselineValue;
          ++baselineCount;
        }
      }
    }

    // end of current run
    if (lastStart >= 0) {
      long start = sTime.get(lastStart);
      long end = start + 1;

      // guess-timate of next time series timestamp
      if (datasetTimezone != null) {
        final DateTimeZone timezone = DateTimeZone.forID(datasetTimezone);
        final long lastTimestamp = sTime.getLong(sTime.size() - 1);

        end = new DateTime(lastTimestamp, timezone)
            .plus(monitoringGranularityPeriod)
            .getMillis();
      }
      final MergedAnomalyResultDTO anomaly = makeAnomaly(slice.withStart(start).withEnd(end));
      if (currCount > 0) {
        anomaly.setAvgCurrentVal(currSum / currCount);
      }
      if (baselineCount > 0) {
        anomaly.setAvgBaselineVal(baselineSum / baselineCount);
      }
      anomalies.add(anomaly);
    }

    return anomalies;
  }

  /**
   * Helper for creating an anomaly for a given metric slice. Injects properties such as
   * metric name, filter dimensions, etc.
   *
   * @param slice metric slice
   * @return anomaly template
   */
  public static MergedAnomalyResultDTO makeAnomaly(final MetricSlice slice) {
    return makeAnomaly(slice.getStart(), slice.getEnd());
  }

  public static MergedAnomalyResultDTO makeAnomaly(final long start, final long end) {
    final MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(start);
    anomaly.setEndTime(end);
    return anomaly;
  }

  public static MergedAnomalyResultDTO makeAnomaly(final long start, final long end,
      final long configId) {
    final MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(start);
    anomaly.setEndTime(end);
    anomaly.setDetectionConfigId(configId);

    return anomaly;
  }

  public static void setEntityChildMapping(final MergedAnomalyResultDTO parent,
      final MergedAnomalyResultDTO child1) {
    if (child1 != null) {
      parent.getChildren().add(child1);
      child1.setChild(true);
    }

    parent.setChild(false);
  }

  public static MergedAnomalyResultDTO makeEntityAnomaly() {
    final MergedAnomalyResultDTO entityAnomaly = new MergedAnomalyResultDTO();
    // TODO: define anomaly type
    //entityAnomaly.setType();
    entityAnomaly.setChild(false);

    return entityAnomaly;
  }

  public static MergedAnomalyResultDTO makeParentEntityAnomaly(
      final MergedAnomalyResultDTO childAnomaly) {
    final MergedAnomalyResultDTO newEntityAnomaly = makeEntityAnomaly();
    newEntityAnomaly.setStartTime(childAnomaly.getStartTime());
    newEntityAnomaly.setEndTime(childAnomaly.getEndTime());
    setEntityChildMapping(newEntityAnomaly, childAnomaly);
    return newEntityAnomaly;
  }

  public static List<MergedAnomalyResultDTO> mergeAndSortAnomalies(
      final List<MergedAnomalyResultDTO> anomalyListA,
      final List<MergedAnomalyResultDTO> anomalyListB) {
    final List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    if (anomalyListA != null) {
      anomalies.addAll(anomalyListA);
    }
    if (anomalyListB != null) {
      anomalies.addAll(anomalyListB);
    }

    // Sort by increasing order of anomaly start time
    anomalies.sort(Comparator.comparingLong(MergedAnomalyResultDTO::getStartTime));
    return anomalies;
  }

  /**
   * Helper for consolidate last time stamps in all nested detection pipelines
   *
   * @param nestedLastTimeStamps all nested last time stamps
   * @return the last time stamp
   */
  public static long consolidateNestedLastTimeStamps(final Collection<Long> nestedLastTimeStamps) {
    if (nestedLastTimeStamps.isEmpty()) {
      return -1L;
    }
    return Collections.max(nestedLastTimeStamps);
  }

  /**
   * Get the joda period for a monitoring granularity
   */
  public static Period getMonitoringGranularityPeriod(final String monitoringGranularity,
      final DatasetConfigDTO datasetConfigDTO) {
    if (monitoringGranularity
        .equals(MetricSlice.NATIVE_GRANULARITY.toAggregationGranularityString())) {
      return datasetConfigDTO.bucketTimeGranularity().toPeriod();
    }
    final String[] split = monitoringGranularity.split("_");
    if (split[1].equals("MONTHS")) {
      return new Period(0, Integer.parseInt(split[0]), 0, 0, 0, 0, 0, 0, PeriodType.months());
    }
    if (split[1].equals("WEEKS")) {
      return new Period(0, 0, Integer.parseInt(split[0]), 0, 0, 0, 0, 0, PeriodType.weeks());
    }
    return TimeGranularity.fromString(monitoringGranularity).toPeriod();
  }

  public static Period periodFromTimeUnit(final int size, final TimeUnit unit) {
    switch (unit) {
      case DAYS:
        return Period.days(size);
      case HOURS:
        return Period.hours(size);
      case MINUTES:
        return Period.minutes(size);
      case SECONDS:
        return Period.seconds(size);
      case MILLISECONDS:
        return Period.millis(size);
      default:
        return new Period(TimeUnit.MILLISECONDS.convert(size, unit));
    }
  }

  /**
   * Aggregate the time series data frame's value to specified granularity
   *
   * @param df the data frame
   * @param origin the aggregation origin time stamp
   * @param granularityPeriod the aggregation granularity in period
   * @param aggregationFunction the metric's aggregation function
   * @return the aggregated time series data frame
   */
  public static DataFrame aggregateByPeriod(final DataFrame df, final DateTime origin,
      final Period granularityPeriod,
      final MetricAggFunction aggregationFunction) {
    switch (aggregationFunction) {
      case SUM:
        return df.groupByPeriod(df.getLongs(DataFrame.COL_TIME), origin, granularityPeriod).sum(
            DataFrame.COL_TIME, DataFrame.COL_VALUE);
      case AVG:
        return df.groupByPeriod(df.getLongs(DataFrame.COL_TIME), origin, granularityPeriod).mean(
            DataFrame.COL_TIME, DataFrame.COL_VALUE);
      default:
        throw new UnsupportedOperationException(String
            .format("The aggregate by period for %s is not supported in DataFrame.",
                aggregationFunction));
    }
  }

  /**
   * Check if the aggregation result is complete or not, if not, remove it from the aggregated
   * result.
   *
   * For example, say the weekStart is Monday and current data is available through Jan 8,
   * Wednesday.
   * the latest data time stamp will be Jan 8. The latest aggregation start time stamp should be Jan
   * 6, Monday.
   * In such case, the latest data point is incomplete and should be filtered. If the latest data
   * time stamp is
   * Jan 12, Sunday instead, the data is complete and good to use because the week's data is
   * complete.
   *
   * @param df the aggregated data frame to check
   * @param latestDataTimeStamp the latest data time stamp
   * @param bucketTimeGranularity the metric's original granularity
   * @param aggregationGranularityPeriod the granularity after aggregation
   * @return the filtered data frame
   */
  public static DataFrame filterIncompleteAggregation(DataFrame df,
      final long latestDataTimeStamp,
      final TimeGranularity bucketTimeGranularity,
      final Period aggregationGranularityPeriod) {
    final long latestAggregationStartTimeStamp = df.getLong(DataFrame.COL_TIME, df.size() - 1);
    if (latestDataTimeStamp + bucketTimeGranularity.toMillis()
        < latestAggregationStartTimeStamp + aggregationGranularityPeriod.toStandardDuration()
        .getMillis()) {
      df = df.filter(df.getLongs(DataFrame.COL_TIME).neq(latestAggregationStartTimeStamp))
          .dropNull();
    }
    return df;
  }

  /**
   * Verify if this detection has data quality checks enabled
   */
  public static boolean isDataQualityCheckEnabled(final AlertDTO detectionConfig) {
    return detectionConfig.getDataQualityProperties() != null
        && !detectionConfig.getDataQualityProperties().isEmpty();
  }

  public static Predicate AND(final Collection<Predicate> predicates) {
    return Predicate.AND(predicates.toArray(new Predicate[predicates.size()]));
  }

  public static List<Predicate> buildPredicatesOnTime(final long start, final long end) {
    final List<Predicate> predicates = new ArrayList<>();
    if (end >= 0) {
      predicates.add(Predicate.LT("startTime", end));
    }
    if (start >= 0) {
      predicates.add(Predicate.GT("endTime", start));
    }

    return predicates;
  }

  /**
   * Renotify the anomaly by creating or updating the record in the subscription group notification
   * table
   *
   * @param anomaly the anomaly to be notified.
   */
  public static void renotifyAnomaly(final MergedAnomalyResultDTO anomaly,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager) {
    final List<AnomalySubscriptionGroupNotificationDTO> subscriptionGroupNotificationDTOs =
        anomalySubscriptionGroupNotificationManager
            .findByPredicate(Predicate.EQ("anomalyId", anomaly.getId()));
    final AnomalySubscriptionGroupNotificationDTO anomalyNotificationDTO;
    if (subscriptionGroupNotificationDTOs.isEmpty()) {
      // create a new record if it is not existed yet.
      anomalyNotificationDTO = new AnomalySubscriptionGroupNotificationDTO();
      new AnomalySubscriptionGroupNotificationDTO();
      anomalyNotificationDTO.setAnomalyId(anomaly.getId());
      anomalyNotificationDTO.setDetectionConfigId(anomaly.getDetectionConfigId());
    } else {
      // update the existing record if the anomaly needs to be re-notified
      anomalyNotificationDTO = subscriptionGroupNotificationDTOs.get(0);
      anomalyNotificationDTO.setNotifiedSubscriptionGroupIds(Collections.emptyList());
    }
    anomalySubscriptionGroupNotificationManager.save(anomalyNotificationDTO);
  }

  public static DataFrame buildBaselines(final MetricSlice slice, final Baseline baseline,
      final InputDataFetcher dataFetcher) {
    final List<MetricSlice> slices = new ArrayList<>(baseline.scatter(slice));
    final InputData data = dataFetcher.fetchData(new InputDataSpec().withTimeseriesSlices(slices));
    return baseline.gather(slice, data.getTimeseries());
  }

  public static Map<String, DataTable> getTimeSeriesMap(
      final Map<String, DetectionPipelineResult> inputMap) {
    final Map<String, DataTable> timeSeriesMap = new HashMap<>();
    for (final String key : inputMap.keySet()) {
      final DetectionPipelineResult input = inputMap.get(key);
      if (input instanceof DataTable) {
        timeSeriesMap.put(key, (DataTable) input);
      }
    }
    return timeSeriesMap;
  }
}
