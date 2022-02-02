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

import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_TIME;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.PeriodType;

public class DetectionUtils {

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

  // todo cyril can be moved to core once v1 is removed
  public static DetectionResult buildDetectionResult(
      final AnomalyDetectorV2Result detectorV2Result) {
    final List<MergedAnomalyResultDTO> anomalies = DetectionUtils.buildAnomaliesFromDetectorDf(
        detectorV2Result.getDataFrame(),
        detectorV2Result.getTimeZone(),
        detectorV2Result.getMonitoringGranularityPeriod());

    return DetectionResult.from(anomalies,
        TimeSeries.fromDataFrame(detectorV2Result.getDataFrame().sortedBy(COL_TIME)));
  }

  // todo cyril can be moved to core once v1 is removed
  public static List<MergedAnomalyResultDTO> buildAnomaliesFromDetectorDf(final DataFrame df,
      final String datasetTimezone,
      final Period monitoringGranularityPeriod) {
    if (df.isEmpty()) {
      return Collections.emptyList();
    }

    final List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    final LongSeries timeMillisSeries = df.getLongs(DataFrame.COL_TIME);
    final BooleanSeries isAnomalySeries = df.getBooleans(DataFrame.COL_ANOMALY);
    final DoubleSeries currentSeries = df.getDoubles(DataFrame.COL_CURRENT);
    final DoubleSeries baselineSeries = df.getDoubles(DataFrame.COL_VALUE);

    long lastStartMillis = -1;
    AnomalyStatsAccumulator anomalyStatsAccumulator = new AnomalyStatsAccumulator();

    for (int i = 0; i < df.size(); i++) {
      if (!isAnomalySeries.isNull(i) && BooleanSeries.booleanValueOf(isAnomalySeries.get(i))) {
        // inside an anomaly range
        if (lastStartMillis < 0) {
          // start of an anomaly range
          lastStartMillis = timeMillisSeries.get(i);
        }
        if (!currentSeries.isNull(i)) {
          anomalyStatsAccumulator.addCurrentValue(currentSeries.getDouble(i));
        }
        if (!baselineSeries.isNull(i)) {
          anomalyStatsAccumulator.addBaselineValue(baselineSeries.getDouble(i));
        }
      } else if (lastStartMillis >= 0) {
        // anomaly range opened - let's close the anomaly
        long endMillis = timeMillisSeries.get(i);
        anomalies.add(anomalyStatsAccumulator.buildAnomaly(lastStartMillis, endMillis));

        // reset variables for next anomaly
        anomalyStatsAccumulator.reset();
        lastStartMillis = -1;
      }
    }

    if (lastStartMillis >= 0) {
      // last anomaly has not been closed - let's close it
      // estimate end time of anomaly range
      final long lastTimestamp = timeMillisSeries.getLong(timeMillisSeries.size() - 1);
      // default: add 1 to lastTimestamp
      long endMillis = lastTimestamp + 1;
      if (datasetTimezone != null && monitoringGranularityPeriod != null) {
        // exact computation of end of period
        final DateTimeZone timezone = DateTimeZone.forID(datasetTimezone);
        endMillis = new DateTime(lastTimestamp, timezone)
            .plus(monitoringGranularityPeriod)
            .getMillis();
      }
      anomalies.add(anomalyStatsAccumulator.buildAnomaly(lastStartMillis, endMillis));
    }

    return anomalies;
  }

  public static MergedAnomalyResultDTO makeAnomaly(final long start, final long end) {
    final MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(start);
    anomaly.setEndTime(end);
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

  private static class AnomalyStatsAccumulator {

    private double currentSum = 0;
    private int currentCount = 0;
    private double baselineSum = 0;
    private int baselineCount = 0;

    public AnomalyStatsAccumulator() {
    }

    public MergedAnomalyResultDTO buildAnomaly(long startMillis, long endMillis) {
      final MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
      anomaly.setStartTime(startMillis);
      anomaly.setEndTime(endMillis);
      if (currentCount > 0) {
        anomaly.setAvgCurrentVal(currentSum / currentCount);
      }
      if (baselineCount > 0) {
        anomaly.setAvgBaselineVal(baselineSum / baselineCount);
      }
      return anomaly;
    }

    public void addCurrentValue(double currentValue) {
      currentSum += currentValue;
      ++currentCount;
    }

    public void addBaselineValue(double baselineValue) {
      baselineSum += baselineValue;
      ++baselineCount;
    }

    public void reset() {
      currentSum = 0;
      currentCount = 0;
      baselineSum = 0;
      baselineCount = 0;
    }
  }
}
