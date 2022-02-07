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

package org.apache.pinot.thirdeye.detection.anomalydetection.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.pinot.thirdeye.detection.anomalydetection.context.TimeSeries;
import org.apache.pinot.thirdeye.metric.MetricTimeSeries;
import org.joda.time.Interval;

public class BackwardAnomalyFunctionUtils {

  private static final Double NULL_DOUBLE = Double.NaN;

  /**
   * Splits a MetricTimeSeries to current (observed) time series and baselines. The list of time
   * series is sorted by the start time of their interval in the reversed natural order. Therefore,
   * the current time series is located at the beginning of the returned list.
   *
   * @param metricTimeSeries the metric time series that contains current and baseline time
   *     series.
   * @param metricName the metric name to retrieve the value from the given metric time series.
   * @param timeSeriesIntervals the intervals of the split time series.
   * @return a list of time series, which are split from the metric time series.
   */
  public static List<TimeSeries> splitSetsOfTimeSeries(MetricTimeSeries metricTimeSeries,
      String metricName, List<Interval> timeSeriesIntervals) {
    List<TimeSeries> timeSeriesList = new ArrayList<>(timeSeriesIntervals.size());
    for (Interval interval : timeSeriesIntervals) {
      TimeSeries timeSeries = new TimeSeries();
      timeSeries.setTimeSeriesInterval(interval);
      timeSeriesList.add(timeSeries);
    }
    // Sort time series by their start time in reversed natural order, i.e., the latest time series
    // is arranged in the front of the list
    Collections.sort(timeSeriesList, Collections.reverseOrder(new TimeSeriesStartTimeComparator()));

    // If a timestamp is contained in the interval of a time series, then the timestamp and its
    // value is inserted into that time series. If the intervals of time series have overlap, then
    // the timestamp and its value could be inserted to multiple time series.
    for (long timestamp : metricTimeSeries.getTimeWindowSet()) {
      for (TimeSeries timeSeries : timeSeriesList) {
        if (timeSeries.getTimeSeriesInterval().contains(timestamp)) {
          double value = metricTimeSeries.getOrDefault(timestamp, metricName, NULL_DOUBLE)
              .doubleValue();
          if (Double.compare(value, NULL_DOUBLE) != 0) {
            timeSeries.set(timestamp, value);
          }
        }
      }
    }
    return timeSeriesList;
  }

  /**
   * Compares Time Series by the start time of their interval.
   */
  private static class TimeSeriesStartTimeComparator implements Comparator<TimeSeries> {

    @Override
    public int compare(TimeSeries ts1, TimeSeries ts2) {
      long startTime1 = ts1.getTimeSeriesInterval().getStartMillis();
      long startTime2 = ts2.getTimeSeriesInterval().getStartMillis();
      return Long.compare(startTime1, startTime2);
    }
  }
}
