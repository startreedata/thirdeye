/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.timeseries;

import ai.startree.thirdeye.datasource.ResponseParserUtils;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponseRow;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
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
