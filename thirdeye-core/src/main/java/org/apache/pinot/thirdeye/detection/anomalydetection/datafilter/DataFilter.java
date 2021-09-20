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

package org.apache.pinot.thirdeye.detection.anomalydetection.datafilter;

import java.util.Map;
import org.apache.pinot.thirdeye.metric.MetricTimeSeries;
import org.apache.pinot.thirdeye.spi.detection.dimension.DimensionMap;

public interface DataFilter {

  /**
   * Sets parameters for this filter.
   *
   * @param props the properties for this filter.
   */
  void setParameters(Map<String, String> props);

  /**
   * Returns if the given data, which is a time series with the given metric name and dimension map,
   * passes the filter.
   *
   * @param metricTimeSeries the context for retrieving the time series.
   * @param dimensionMap the dimension map to retrieve the time series.
   * @return true if the time series passes this filter.
   */
  boolean isQualified(MetricTimeSeries metricTimeSeries, DimensionMap dimensionMap);

  /**
   * Returns if the given time series, which is specified by a metric name, a dimension map, and a
   * pair of
   * start and end timestamps, passes the filter.
   *
   * @param metricTimeSeries the context for retrieving the time series.
   * @param dimensionMap the dimension map of time series.
   * @param windowStart the start timestamp of the target data points, inclusive.
   * @param windowEnd the end timestamp of the target data points, exclusive.
   * @return true if the time series passes this filter.
   */
  boolean isQualified(MetricTimeSeries metricTimeSeries, DimensionMap dimensionMap,
      long windowStart, long windowEnd);
}
