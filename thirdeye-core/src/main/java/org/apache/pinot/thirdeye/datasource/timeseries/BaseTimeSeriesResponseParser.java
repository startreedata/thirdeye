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

package org.apache.pinot.thirdeye.datasource.timeseries;

import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pinot.thirdeye.datasource.ResponseParserUtils;
import org.apache.pinot.thirdeye.spi.datasource.MetricFunction;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponseRow;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.util.SpiUtils;
import org.joda.time.DateTime;

public abstract class BaseTimeSeriesResponseParser implements TimeSeriesResponseParser {

  /**
   * Parse the given ThirdEye response for UI, in which the missing data might be inserted with
   * zeros and an OTHER
   * dimension is created in order to roll up entries with small contributions for alleviating
   * congested UI.
   * This method is thread safe.
   *
   * @param response the ThirdEye response to be parsed.
   * @return The parsed time series rows.
   */
  public List<TimeSeriesRow> parseResponse(ThirdEyeResponse response) {
    if (response == null) {
      return Collections.emptyList();
    }

    ThirdEyeRequest request = response.getRequest();
    if (CollectionUtils.isNotEmpty(request.getGroupBy())) {
      return parseGroupByTimeDimensionResponse(response);
    } else {
      return parseGroupByTimeResponse(response);
    }
  }

  protected List<TimeSeriesRow> parseGroupByTimeResponse(ThirdEyeResponse response) {
    Map<String, ThirdEyeResponseRow> responseMap = ResponseParserUtils
        .createResponseMapByTime(response);
    List<Range<DateTime>> ranges = getTimeRanges(response.getRequest());
    int numTimeBuckets = ranges.size();
    List<MetricFunction> metricFunctions = response.getMetricFunctions();
    List<TimeSeriesRow> rows = new ArrayList<>();

    for (int timeBucketId = 0; timeBucketId < numTimeBuckets; timeBucketId++) {
      Range<DateTime> timeRange = ranges.get(timeBucketId);
      ThirdEyeResponseRow responseRow = responseMap.get(String.valueOf(timeBucketId));

      if (responseRow != null) {
        TimeSeriesRow.Builder builder = new TimeSeriesRow.Builder();
        builder.setStart(timeRange.lowerEndpoint());
        builder.setEnd(timeRange.upperEndpoint());

        addMetric(metricFunctions, responseRow, builder);
        TimeSeriesRow row = builder.build();
        rows.add(row);
      }
    }

    return rows;
  }

  protected abstract List<TimeSeriesRow> parseGroupByTimeDimensionResponse(
      ThirdEyeResponse response);

  /* Helper functions */

  protected List<TimeSeriesRow> buildTimeSeriesRows(Map<String, ThirdEyeResponseRow> responseMap,
      List<Range<DateTime>> ranges, int numTimeBuckets, List<String> dimensionNames,
      List<String> dimensionValues,
      List<MetricFunction> metricFunctions) {

    List<TimeSeriesRow> thresholdRows = new ArrayList<>();

    for (int timeBucketId = 0; timeBucketId < numTimeBuckets; timeBucketId++) {
      Range<DateTime> timeRange = ranges.get(timeBucketId);

      // compute the time|dimension key
      String timeDimensionValue = ResponseParserUtils
          .computeTimeDimensionValues(timeBucketId, dimensionValues);

      ThirdEyeResponseRow responseRow = responseMap.get(timeDimensionValue);

      if (responseRow != null) {
        TimeSeriesRow.Builder builder = new TimeSeriesRow.Builder();
        builder.setStart(timeRange.lowerEndpoint());
        builder.setEnd(timeRange.upperEndpoint());
        builder.setDimensionNames(dimensionNames);
        builder.setDimensionValues(dimensionValues);
        addMetric(metricFunctions, responseRow, builder);

        TimeSeriesRow row = builder.build();
        thresholdRows.add(row);
      }
    }

    return thresholdRows;
  }

  protected static void addMetric(List<MetricFunction> metricFunctions, ThirdEyeResponseRow row,
      TimeSeriesRow.Builder builder) {
    for (int i = 0; i < metricFunctions.size(); i++) {
      MetricFunction metricFunction = metricFunctions.get(i);
      double value = 0;
      if (row != null) {
        value = row.getMetrics().get(i);
      }
      builder.addMetric(metricFunction.getMetricName(), value);
    }
  }

  protected static List<Range<DateTime>> getTimeRanges(ThirdEyeRequest request) {
    DateTime start = request.getStartTimeInclusive();
    DateTime end = request.getEndTimeExclusive();
    TimeGranularity timeGranularity = request.getGroupByTimeGranularity();
    return SpiUtils.computeTimeRanges(timeGranularity, start, end);
  }
}
