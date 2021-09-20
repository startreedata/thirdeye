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

package org.apache.pinot.thirdeye.detection.anomalydetection.context;

import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.pinot.thirdeye.spi.detection.MetricTimeSeries;
import org.joda.time.Interval;

/**
 * Time series for anomaly detection, which contains the pairs of timestamp and data values from
 * one time series.
 */
public class TimeSeries implements MetricTimeSeries {

  /**
   * Timestamps to values. Timestamps are sorted.
   */
  private final NavigableMap<Long, Double> timeSeries = new TreeMap<>();

  /**
   * The max (exclusive) and min (inclusive) timestamp of this time series.
   */
  private Interval timeSeriesInterval = new Interval(0L, 0L);

  public TimeSeries() {
  }

  /**
   * Construct time series with list of timestamps and its corresponding values
   */
  public TimeSeries(List<Long> timeStamps, List<Double> values)
      throws Exception {
    if (CollectionUtils.isNotEmpty(timeStamps) && CollectionUtils.isNotEmpty(values)) {
      if (timeStamps.size() != values.size()) {
        throw new IOException("time stamps list and value list need to match in size!!");
      }

      for (int i = 0; i < timeStamps.size(); i++) {
        timeSeries.put(timeStamps.get(i), values.get(i));
      }

      long minTimestamp = timeSeries.firstEntry().getKey();
      long maxTimestamp = timeSeries.lastEntry().getKey() + 1; // end time exclusive

      timeSeriesInterval = new Interval(minTimestamp, maxTimestamp);
    }
  }

  /**
   * Returns data value of the specified timestamp
   *
   * @param timestamp the specified timestamp
   * @return data value of the specified timestamp
   */
  public Double get(long timestamp) {
    return timeSeries.get(timestamp);
  }

  /**
   * Sets the data value of the specified timestamp.
   *
   * @param timestamp the specified timestamp.
   * @param value the data value of the specified timestamp.
   */
  public void set(long timestamp, double value) {
    timeSeries.put(timestamp, value);
  }

  /**
   * Removes data value of the specified timestamp
   */
  @Override
  public void remove(long timeStamp) {
    timeSeries.remove(timeStamp);
  }

  /**
   * Returns true if the specified timestamp exists
   *
   * @param timestamp the specified timestamp
   * @return true if the specified timestamp exists
   */
  @Override
  public boolean hasTimestamp(long timestamp) {
    return timeSeries.containsKey(timestamp);
  }

  /**
   * Returns a sorted timestamp set
   */
  @Override
  public SortedSet<Long> timestampSet() {
    return timeSeries.navigableKeySet();
  }

  /**
   * Returns the number of timestamps in this time series
   */
  @Override
  public int size() {
    return timeSeries.size();
  }

  /**
   * Returns the interval of the time series, which provides the max and min timestamps (inclusive).
   */
  @Override
  public Interval getTimeSeriesInterval() {
    return timeSeriesInterval;
  }

  /**
   * Sets the interval of the time series, which provides the max and min timestamps.
   */
  public void setTimeSeriesInterval(Interval timeSeriesInterval) {
    this.timeSeriesInterval = timeSeriesInterval;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TimeSeries that = (TimeSeries) o;
    return Objects.equals(timeSeries, that.timeSeries) && Objects
        .equals(getTimeSeriesInterval(), that.getTimeSeriesInterval());
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeSeries, getTimeSeriesInterval());
  }
}
