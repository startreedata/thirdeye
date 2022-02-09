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

package ai.startree.thirdeye.datasource.timeseries;

import static ai.startree.thirdeye.datasource.ResponseParserUtils.OTHER;

import ai.startree.thirdeye.datasource.ResponseParserUtils;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.timeseries.TimeSeriesRow.Builder;
import ai.startree.thirdeye.datasource.timeseries.TimeSeriesRow.TimeSeriesMetric;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest.ThirdEyeRequestBuilder;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponseRow;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;

//Heavily based off TimeOnTime equivalent
public class UITimeSeriesResponseParser extends BaseTimeSeriesResponseParser {

  private final boolean doRollUp = true; // roll up small metric to OTHER dimensions
  private DataSourceCache dataSourceCache;

  public UITimeSeriesResponseParser(final DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
  }

  private static boolean checkMetricSums(TimeSeriesRow row, List<Double> metricSums,
      Map<String, Double> metricThresholds) {
    List<TimeSeriesMetric> metrics = row.getMetrics();
    for (int i = 0; i < metrics.size(); i++) {
      TimeSeriesMetric metric = metrics.get(i);
      double sum = 0;
      if (metricSums != null) {
        sum = metricSums.get(i);
      }
      if (metric.getValue() > metricThresholds.get(metric.getMetricName()) * sum) {
        return true;
      }
    }
    return false;
  }

  public Map<Integer, List<Double>> getMetricSumsByTime(ThirdEyeResponse response) {

    ThirdEyeRequest request = response.getRequest();
    Map<Integer, List<Double>> metricSums = new HashMap<>();
    ThirdEyeRequestBuilder requestBuilder = ThirdEyeRequest.newBuilder();
    requestBuilder.setStartTimeInclusive(request.getStartTimeInclusive());
    requestBuilder.setEndTimeExclusive(request.getEndTimeExclusive());
    requestBuilder.setFilterSet(request.getFilterSet());
    requestBuilder.setGroupByTimeGranularity(request.getGroupByTimeGranularity());
    requestBuilder.setMetricFunctions(request.getMetricFunctions());
    requestBuilder.setDataSource(
        ThirdEyeUtils.getDataSourceFromMetricFunctions(request.getMetricFunctions()));
    ThirdEyeRequest metricSumsRequest = requestBuilder.build("metricSums");
    ThirdEyeResponse metricSumsResponse = null;
    try {
      metricSumsResponse = dataSourceCache.getQueryResult(metricSumsRequest);
    } catch (Exception e) {
      ResponseParserUtils.LOGGER.error("Caught exception when executing metric sums request", e);
    }

    for (int i = 0; i < metricSumsResponse.getNumRows(); i++) {
      ThirdEyeResponseRow row = metricSumsResponse.getRow(i);
      metricSums.put(row.getTimeBucketId(), row.getMetrics());
    }
    return metricSums;
  }

  /* Helper functions */

  /**
   * Returns the parsed ThirdEye response that has GroupBy in space dimension. In addition, the
   * combinations of space
   * dimension that have small contributions will be rolled up to a new dimension called OTHER.
   *
   * @param response the ThirdEye response from any data source.
   * @return the parsed ThirdEye response to rows of TimeSeriesRow.
   */
  protected List<TimeSeriesRow> parseGroupByTimeDimensionResponse(ThirdEyeResponse response) {
    Map<String, ThirdEyeResponseRow> responseMap = ResponseParserUtils
        .createResponseMapByTimeAndDimension(response);
    List<Range<DateTime>> ranges = getTimeRanges(response.getRequest());
    int numTimeBuckets = ranges.size();
    List<MetricFunction> metricFunctions = response.getMetricFunctions();
    int numMetrics = metricFunctions.size();
    Map<String, Double> metricThresholds = ThirdEyeUtils.getMetricThresholdsMap(metricFunctions);
    List<TimeSeriesRow> rows = new ArrayList<>();

    Map<Integer, List<Double>> metricSums = Collections.emptyMap();
    if (doRollUp) {
      metricSums = getMetricSumsByTime(response);
    }

    // group by time and dimension values
    Set<String> timeDimensionValues = new HashSet<>();
    timeDimensionValues.addAll(responseMap.keySet());
    Set<List<String>> dimensionValuesList = new HashSet<>();
    for (String timeDimensionValue : timeDimensionValues) {
      List<String> dimensionValues = ResponseParserUtils.extractDimensionValues(timeDimensionValue);
      dimensionValuesList.add(dimensionValues);
    }

    // group by dimension names (the 0th dimension, which is the time bucket, is skipped).
    List<String> groupKeyColumns = response.getGroupKeyColumns();
    List<String> dimensionNameList = new ArrayList<>(groupKeyColumns.size() - 1);
    for (int i = 1; i < groupKeyColumns.size(); ++i) {
      dimensionNameList.add(groupKeyColumns.get(i));
    }

    // other row
    List<TimeSeriesRow.Builder> otherBuilders = new ArrayList<>();
    List<double[]> otherMetrics = new ArrayList<>();
    boolean includeOther = false;
    // constructing an OTHER rows, 1 for each time bucket
    for (int timeBucketId = 0; timeBucketId < numTimeBuckets; timeBucketId++) {
      Range<DateTime> timeRange = ranges.get(timeBucketId);

      TimeSeriesRow.Builder builder = new TimeSeriesRow.Builder();
      builder.setStart(timeRange.lowerEndpoint());
      builder.setEnd(timeRange.upperEndpoint());
      builder.setDimensionNames(dimensionNameList);
      List<String> dimensionValues = new ArrayList<>(dimensionNameList.size());
      for (int i = 0; i < dimensionNameList.size(); ++i) {
        dimensionValues.add(OTHER);
      }
      builder.setDimensionValues(dimensionValues);
      otherBuilders.add(builder);
      double[] other = new double[numMetrics];
      Arrays.fill(other, 0);
      otherMetrics.add(other);
    }

    // for every row we construct, we check if any of its time buckets passes metric
    // threshold
    // if it does, we add it to the rows as is
    // else, we add the metric values to the OTHER row
    for (List<String> dimensionValues : dimensionValuesList) {
      List<TimeSeriesRow> thresholdRows =
          buildTimeSeriesRows(responseMap, ranges, numTimeBuckets, dimensionNameList,
              dimensionValues,
              metricFunctions);

      boolean passedThreshold = false;
      if (doRollUp) {
        // check if rows pass threshold
        for (int timeBucketId = 0; timeBucketId < numTimeBuckets; timeBucketId++) {
          if (checkMetricSums(thresholdRows.get(timeBucketId), metricSums.get(timeBucketId),
              metricThresholds)) {
            passedThreshold = true;
            break;
          }
        }
      } else {
        passedThreshold = true;
      }

      // if any of the cells of a contributor row passes threshold, add all those cells
      if (passedThreshold && !dimensionValues.contains(OTHER)) {
        rows.addAll(thresholdRows);
      } else { // else that row of cells goes into OTHER
        includeOther = true;
        for (int timeBucketId = 0; timeBucketId < numTimeBuckets; timeBucketId++) {
          TimeSeriesRow row = thresholdRows.get(timeBucketId);
          List<TimeSeriesMetric> metrics = row.getMetrics();
          for (int i = 0; i < metrics.size(); i++) {
            TimeSeriesMetric metricToAdd = metrics.get(i);
            otherMetrics.get(timeBucketId)[i] += metricToAdd.getValue();
          }
        }
      }
    }

    // create other row using the other sums
    if (includeOther) {
      for (int timeBucketId = 0; timeBucketId < numTimeBuckets; timeBucketId++) {
        Builder otherBuilder = otherBuilders.get(timeBucketId);
        double[] other = otherMetrics.get(timeBucketId);
        for (int i = 0; i < numMetrics; i++) {
          otherBuilder.addMetric(metricFunctions.get(i).getMetricName(), other[i]);
        }
        rows.add(otherBuilder.build());
      }
    }

    return rows;
  }
}
