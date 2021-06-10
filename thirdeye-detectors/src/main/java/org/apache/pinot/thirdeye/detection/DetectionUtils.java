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

package org.apache.pinot.thirdeye.detection;

import com.google.common.collect.Multimap;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.pinot.thirdeye.spi.common.dimension.DimensionMap;
import org.apache.pinot.thirdeye.spi.common.time.TimeGranularity;
import org.apache.pinot.thirdeye.spi.constant.MetricAggFunction;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.MergedAnomalyResultBean;
import org.apache.pinot.thirdeye.spi.detection.InputDataFetcher;
import org.apache.pinot.thirdeye.spi.detection.spi.components.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.spi.model.InputData;
import org.apache.pinot.thirdeye.spi.detection.spi.model.InputDataSpec;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.Baseline;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.PeriodType;

public class DetectionUtils {

  // TODO anomaly should support multimap
  public static DimensionMap toFilterMap(Multimap<String, String> filters) {
    DimensionMap map = new DimensionMap();
    for (Map.Entry<String, Collection<String>> entry : filters.asMap().entrySet()) {
      map.put(entry.getKey(), String.join(", ", entry.getValue()));
    }
    return map;
  }

  // Check if a string is a component reference
  public static boolean isReferenceName(String key) {
    return key.startsWith("$");
  }

  // Extracts the component key from the reference key
  // e.g., "$myRule:ALGORITHM" -> "myRule:ALGORITHM"
  public static String getComponentKey(String componentRefKey) {
    if (isReferenceName(componentRefKey)) {
      return componentRefKey.substring(1);
    } else {
      throw new IllegalArgumentException("not a component reference key. should starts with $");
    }
  }

  // Extracts the component type from the component key
  // e.g., "myRule:ALGORITHM" -> "ALGORITHM"
  public static String getComponentType(String componentKey) {
    if (componentKey != null && componentKey.contains(":")) {
      return componentKey.substring(componentKey.lastIndexOf(":") + 1);
    }
    throw new IllegalArgumentException(
        "componentKey is invalid; must be of type componentName:type");
  }

  // get the spec class name for a component class
  public static String getSpecClassName(Class<BaseComponent> componentClass) {
    ParameterizedType genericSuperclass = (ParameterizedType) componentClass
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
  public static List<MergedAnomalyResultDTO> makeAnomalies(MetricSlice slice, DataFrame df,
      String seriesName,
      Period monitoringGranularityPeriod, DatasetConfigDTO dataset) {
    if (df.isEmpty()) {
      return Collections.emptyList();
    }

    List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    LongSeries sTime = df.getLongs(DataFrame.COL_TIME);
    BooleanSeries sVal = df.getBooleans(seriesName);

    int lastStart = -1;
    for (int i = 0; i < df.size(); i++) {
      if (sVal.isNull(i) || !BooleanSeries.booleanValueOf(sVal.get(i))) {
        // end of a run
        if (lastStart >= 0) {
          long start = sTime.get(lastStart);
          long end = sTime.get(i);
          anomalies.add(makeAnomaly(slice.withStart(start).withEnd(end)));
        }
        lastStart = -1;
      } else {
        // start of a run
        if (lastStart < 0) {
          lastStart = i;
        }
      }
    }

    // end of current run
    if (lastStart >= 0) {
      long start = sTime.get(lastStart);
      long end = start + 1;

      // guess-timate of next time series timestamp
      if (dataset != null) {
        DateTimeZone timezone = DateTimeZone.forID(dataset.getTimezone());

        long lastTimestamp = sTime.getLong(sTime.size() - 1);

        end = new DateTime(lastTimestamp, timezone).plus(monitoringGranularityPeriod).getMillis();
      }
      anomalies.add(makeAnomaly(slice.withStart(start).withEnd(end)));
    }

    return anomalies;
  }

  public static List<MergedAnomalyResultDTO> makeAnomalies(MetricSlice slice, DataFrame df,
      String seriesName) {
    if (df.isEmpty()) {
      return Collections.emptyList();
    }

    List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    LongSeries sTime = df.getLongs(DataFrame.COL_TIME);
    BooleanSeries sVal = df.getBooleans(seriesName);

    int lastStart = -1;
    for (int i = 0; i < df.size(); i++) {
      if (sVal.isNull(i) || !BooleanSeries.booleanValueOf(sVal.get(i))) {
        // end of a run
        if (lastStart >= 0) {
          long start = sTime.get(lastStart);
          long end = sTime.get(i);
          anomalies.add(makeAnomaly(slice.withStart(start).withEnd(end)));
        }
        lastStart = -1;
      } else {
        // start of a run
        if (lastStart < 0) {
          lastStart = i;
        }
      }
    }

    // end of current run
    if (lastStart >= 0) {
      long start = sTime.get(lastStart);
      long end = start + 1;
      anomalies.add(makeAnomaly(slice.withStart(start).withEnd(end)));
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
  public static MergedAnomalyResultDTO makeAnomaly(MetricSlice slice) {
    return makeAnomaly(slice.getStart(), slice.getEnd());
  }

  public static MergedAnomalyResultDTO makeAnomaly(long start, long end) {
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(start);
    anomaly.setEndTime(end);
    return anomaly;
  }

  public static MergedAnomalyResultDTO makeAnomaly(long start, long end, long configId) {
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(start);
    anomaly.setEndTime(end);
    anomaly.setDetectionConfigId(configId);

    return anomaly;
  }

  public static void setEntityChildMapping(MergedAnomalyResultDTO parent,
      MergedAnomalyResultDTO child1) {
    if (child1 != null) {
      parent.getChildren().add(child1);
      child1.setChild(true);
    }

    parent.setChild(false);
  }

  public static MergedAnomalyResultDTO makeEntityAnomaly() {
    MergedAnomalyResultDTO entityAnomaly = new MergedAnomalyResultDTO();
    // TODO: define anomaly type
    //entityAnomaly.setType();
    entityAnomaly.setChild(false);

    return entityAnomaly;
  }

  public static MergedAnomalyResultDTO makeParentEntityAnomaly(
      MergedAnomalyResultDTO childAnomaly) {
    MergedAnomalyResultDTO newEntityAnomaly = makeEntityAnomaly();
    newEntityAnomaly.setStartTime(childAnomaly.getStartTime());
    newEntityAnomaly.setEndTime(childAnomaly.getEndTime());
    setEntityChildMapping(newEntityAnomaly, childAnomaly);
    return newEntityAnomaly;
  }

  public static List<MergedAnomalyResultDTO> mergeAndSortAnomalies(
      List<MergedAnomalyResultDTO> anomalyListA, List<MergedAnomalyResultDTO> anomalyListB) {
    List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    if (anomalyListA != null) {
      anomalies.addAll(anomalyListA);
    }
    if (anomalyListB != null) {
      anomalies.addAll(anomalyListB);
    }

    // Sort by increasing order of anomaly start time
    anomalies.sort(Comparator.comparingLong(MergedAnomalyResultBean::getStartTime));
    return anomalies;
  }

  /**
   * Helper for consolidate last time stamps in all nested detection pipelines
   *
   * @param nestedLastTimeStamps all nested last time stamps
   * @return the last time stamp
   */
  public static long consolidateNestedLastTimeStamps(Collection<Long> nestedLastTimeStamps) {
    if (nestedLastTimeStamps.isEmpty()) {
      return -1L;
    }
    return Collections.max(nestedLastTimeStamps);
  }

  /**
   * Get the joda period for a monitoring granularity
   */
  public static Period getMonitoringGranularityPeriod(String monitoringGranularity,
      DatasetConfigDTO datasetConfigDTO) {
    if (monitoringGranularity
        .equals(MetricSlice.NATIVE_GRANULARITY.toAggregationGranularityString())) {
      return datasetConfigDTO.bucketTimeGranularity().toPeriod();
    }
    String[] split = monitoringGranularity.split("_");
    if (split[1].equals("MONTHS")) {
      return new Period(0, Integer.parseInt(split[0]), 0, 0, 0, 0, 0, 0, PeriodType.months());
    }
    if (split[1].equals("WEEKS")) {
      return new Period(0, 0, Integer.parseInt(split[0]), 0, 0, 0, 0, 0, PeriodType.weeks());
    }
    return TimeGranularity.fromString(monitoringGranularity).toPeriod();
  }

  public static Period periodFromTimeUnit(int size, TimeUnit unit) {
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
  public static DataFrame aggregateByPeriod(DataFrame df, DateTime origin, Period granularityPeriod,
      MetricAggFunction aggregationFunction) {
    switch (aggregationFunction) {
      case SUM:
        return df.groupByPeriod(df.getLongs(DataFrame.COL_TIME), origin, granularityPeriod).sum(
            DataFrame.COL_TIME, DataFrame.COL_VALUE);
      case AVG:
        return df.groupByPeriod(df.getLongs(DataFrame.COL_TIME), origin, granularityPeriod).mean(
            DataFrame.COL_TIME, DataFrame.COL_VALUE);
      default:
        throw new NotImplementedException(String
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
  public static DataFrame filterIncompleteAggregation(DataFrame df, long latestDataTimeStamp,
      TimeGranularity bucketTimeGranularity, Period aggregationGranularityPeriod) {
    long latestAggregationStartTimeStamp = df.getLong(DataFrame.COL_TIME, df.size() - 1);
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
  public static boolean isDataQualityCheckEnabled(AlertDTO detectionConfig) {
    return detectionConfig.getDataQualityProperties() != null
        && !detectionConfig.getDataQualityProperties().isEmpty();
  }

  public static long makeTimeout(long deadline) {
    long diff = deadline - System.currentTimeMillis();
    return diff > 0 ? diff : 0;
  }

  public static Predicate AND(Collection<Predicate> predicates) {
    return Predicate.AND(predicates.toArray(new Predicate[predicates.size()]));
  }

  public static List<Predicate> buildPredicatesOnTime(long start, long end) {
    List<Predicate> predicates = new ArrayList<>();
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
  public static void renotifyAnomaly(MergedAnomalyResultDTO anomaly,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager) {
    List<AnomalySubscriptionGroupNotificationDTO> subscriptionGroupNotificationDTOs =
        anomalySubscriptionGroupNotificationManager
            .findByPredicate(Predicate.EQ("anomalyId", anomaly.getId()));
    AnomalySubscriptionGroupNotificationDTO anomalyNotificationDTO;
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

  public static DataFrame buildBaselines(MetricSlice slice, Baseline baseline,
      InputDataFetcher dataFetcher) {
    List<MetricSlice> slices = new ArrayList<>(baseline.scatter(slice));
    InputData data = dataFetcher.fetchData(new InputDataSpec().withTimeseriesSlices(slices));
    return baseline.gather(slice, data.getTimeseries());
  }
}
