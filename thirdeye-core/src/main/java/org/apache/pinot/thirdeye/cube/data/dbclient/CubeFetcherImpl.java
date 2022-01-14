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

package org.apache.pinot.thirdeye.cube.data.dbclient;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.apache.pinot.thirdeye.cube.data.dbrow.Row;
import org.apache.pinot.thirdeye.datasource.MetricExpression;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.datasource.MetricFunction;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.apache.pinot.thirdeye.spi.detection.MetricAggFunction;
import org.apache.pinot.thirdeye.util.ThirdEyeUtils;
import org.apache.pinot.thirdeye.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class generates query requests to the backend database and retrieve the data for summary
 * algorithm.
 *
 * The generated requests are organized the following tree structure:
 * Root level by GroupBy dimensions.
 * Mid  level by "baseline" or "current"; The "baseline" request is ordered before the "current"
 * request.
 * Leaf level by metric functions; This level is handled by the request itself, i.e., a request can
 * gather multiple
 * metric functions at the same time.
 * The generated requests are store in a List. Because of the tree structure, the requests belong to
 * the same
 * timeline (baseline or current) are located together. Then, the requests belong to the same
 * GroupBy dimension are
 * located together.
 */
public class CubeFetcherImpl<R extends Row> implements CubeFetcher<R> {

  private static final Logger LOG = LoggerFactory.getLogger(CubeFetcherImpl.class);
  private final static int TIME_OUT_VALUE = 1200;
  private final static TimeUnit TIME_OUT_UNIT = TimeUnit.SECONDS;

  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final DataSourceCache dataSourceCache;
  private final CubeMetric<R> cubeMetric;

  /**
   * Constructs a Cube client.
   *
   */
  public CubeFetcherImpl(DataSourceCache dataSourceCache,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry, CubeMetric<R> cubeMetric) {
    this.dataSourceCache = Preconditions.checkNotNull(dataSourceCache);
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.cubeMetric = cubeMetric;
  }

  /**
   * Construct bulks ThirdEye requests.
   *
   * @param dataset the data set to be queries.
   * @param cubeSpecs the spec to retrieve the metrics.
   * @param groupBy groupBy for database.
   * @param filterSets the data filter.
   * @return a list of ThirdEye requests.
   */
  protected Map<CubeTag, ThirdEyeRequestMetricExpressions> constructBulkRequests(
      String dataset,
      List<CubeSpec> cubeSpecs, List<String> groupBy, Multimap<String, String> filterSets)
      throws ExecutionException {

    Map<CubeTag, ThirdEyeRequestMetricExpressions> requests = new HashMap<>();

    for (CubeSpec cubeSpec : cubeSpecs) {
      // Set dataset and metric
      List<MetricExpression> metricExpressions =
          Utils.convertToMetricExpressions(cubeSpec.getMetric(), MetricAggFunction.SUM, dataset,
              thirdEyeCacheRegistry);
      List<MetricFunction> metricFunctions = metricExpressions.get(0).computeMetricFunctions(
          thirdEyeCacheRegistry);

      ThirdEyeRequest.ThirdEyeRequestBuilder builder = ThirdEyeRequest.newBuilder();

      builder.setMetricFunctions(metricFunctions);
      builder.setDataSource(ThirdEyeUtils.getDataSourceFromMetricFunctions(metricFunctions));

      // Set start and end time
      builder.setStartTimeInclusive(cubeSpec.getInterval().getStartMillis());
      builder.setEndTimeExclusive(cubeSpec.getInterval().getEndMillis());

      // Set groupBy and filter
      builder.setGroupBy(groupBy);
      builder.setFilterSet(filterSets);

      requests.put(cubeSpec.getTag(),
          new ThirdEyeRequestMetricExpressions(builder.build(cubeSpec.getTag().toString()),
              metricExpressions));
    }

    return requests;
  }

  /**
   * Fills in multiple Pinot results to one Cube row.
   *
   * @param rowTable the table from dimension values to cube row; the return of this method.
   * @param dimensions the dimension names of the row.
   * @param dimensionValues the dimension values of the row.
   * @param value the value to be filled in to the row.
   * @param tag The field of the row where the value is filled in.
   */
  protected void fillValueToRowTable(Map<List<String>, R> rowTable, Dimensions dimensions,
      List<String> dimensionValues, double value, CubeTag tag) {
    cubeMetric.fillValueToRowTable(rowTable, dimensions, dimensionValues, value, tag);
  }

  /**
   * Returns a list of rows. The value of each row is evaluated and no further processing is needed.
   *
   * @param dimensions dimensions of the response
   * @param response the response from backend database
   * @param rowTable the storage for rows
   * @param tag true if the response is for baseline values
   */
  protected void buildMetricFunctionOrExpressionsRows(Dimensions dimensions,
      List<MetricExpression> metricExpressions,
      List<MetricFunction> metricFunctions, ThirdEyeResponse response,
      Map<List<String>, R> rowTable, CubeTag tag) {
    Map<String, Double> context = new HashMap<>();
    for (int rowIdx = 0; rowIdx < response.getNumRows(); ++rowIdx) {
      double value = 0d;
      // If the metric expression is a single metric function, then we get the value immediately
      if (metricFunctions.size() <= 1) {
        value = response.getRow(rowIdx).getMetrics().get(0);
      } else { // Otherwise, we need to evaluate the expression
        for (int metricFuncIdx = 0; metricFuncIdx < metricFunctions.size(); ++metricFuncIdx) {
          double contextValue = response.getRow(rowIdx).getMetrics().get(metricFuncIdx);
          context.put(metricFunctions.get(metricFuncIdx).getMetricName(), contextValue);
        }
        try {
          value = MetricExpression.evaluateExpression(metricExpressions.get(0), context);
        } catch (Exception e) {
          LOG.warn(e.getMessage());
        }
      }
      List<String> dimensionValues = response.getRow(rowIdx).getDimensions();
      fillValueToRowTable(rowTable, dimensions, dimensionValues, value, tag);
    }
  }

  /**
   * Converts Pinot results to Cube Rows.
   *
   * @param dimensions the dimension of the Pinot results.
   * @param bulkRequests the original requests of those results.
   * @return Cube rows.
   */
  protected List<List<R>> constructAggregatedValues(Dimensions dimensions,
      List<Map<CubeTag, ThirdEyeRequestMetricExpressions>> bulkRequests) throws Exception {

    List<ThirdEyeRequest> allRequests = new ArrayList<>();
    for (Map<CubeTag, ThirdEyeRequestMetricExpressions> bulkRequest : bulkRequests) {
      for (Map.Entry<CubeTag, ThirdEyeRequestMetricExpressions> entry : bulkRequest.entrySet()) {
        ThirdEyeRequest thirdEyeRequest = entry.getValue().getThirdEyeRequest();
        allRequests.add(thirdEyeRequest);
      }
    }

    Map<ThirdEyeRequest, Future<ThirdEyeResponse>> queryResponses = dataSourceCache
        .getQueryResultsAsync(allRequests);

    List<List<R>> res = new ArrayList<>();
    int level = 0;
    for (Map<CubeTag, ThirdEyeRequestMetricExpressions> bulkRequest : bulkRequests) {
      Map<List<String>, R> rowOfSameLevel = new HashMap<>();

      for (Map.Entry<CubeTag, ThirdEyeRequestMetricExpressions> entry : bulkRequest.entrySet()) {
        CubeTag tag = entry.getKey();
        ThirdEyeRequest thirdEyeRequest = entry.getValue().getThirdEyeRequest();
        ThirdEyeResponse thirdEyeResponse = queryResponses.get(thirdEyeRequest)
            .get(TIME_OUT_VALUE, TIME_OUT_UNIT);
        if (thirdEyeResponse.getNumRows() == 0) {
          LOG.warn("Get 0 rows from the request(s): {}", thirdEyeRequest);
        }
        List<MetricExpression> metricExpressions = entry.getValue().getMetricExpressions();
        buildMetricFunctionOrExpressionsRows(dimensions, metricExpressions,
            thirdEyeRequest.getMetricFunctions(),
            thirdEyeResponse, rowOfSameLevel, tag);
      }
      if (rowOfSameLevel.size() == 0) {
        LOG.warn("Failed to retrieve non-zero results for requests of level {}. BulkRequest: {}",
            level, bulkRequest);
      }
      List<R> rows = new ArrayList<>(rowOfSameLevel.values());
      res.add(rows);
      ++level;
    }

    return res;
  }

  @Override
  public R getTopAggregatedValues(Multimap<String, String> filterSets) throws Exception {
    List<String> groupBy = Collections.emptyList();
    List<Map<CubeTag, ThirdEyeRequestMetricExpressions>> bulkRequests = Collections.singletonList(
        constructBulkRequests(cubeMetric.getDataset(), cubeMetric.getCubeSpecs(), groupBy, filterSets));
    // quickfix - redundant local variables for better IndexOutOfBoundsException logging
    List<List<R>> aggregatedValues = constructAggregatedValues(new Dimensions(), bulkRequests);
    List<R> aggregatedValue = aggregatedValues.get(0);
    R topValue = aggregatedValue.get(0);
    return topValue;
  }

  @Override
  public List<List<R>> getAggregatedValuesOfDimension(Dimensions dimensions,
      Multimap<String, String> filterSets)
      throws Exception {
    List<Map<CubeTag, ThirdEyeRequestMetricExpressions>> bulkRequests = new ArrayList<>();
    for (int level = 0; level < dimensions.size(); ++level) {
      List<String> groupBy = Lists.newArrayList(dimensions.get(level));
      bulkRequests.add(
          constructBulkRequests(cubeMetric.getDataset(), cubeMetric.getCubeSpecs(), groupBy, filterSets));
    }
    return constructAggregatedValues(dimensions, bulkRequests);
  }

  @Override
  public List<List<R>> getAggregatedValuesOfLevels(Dimensions dimensions,
      Multimap<String, String> filterSets)
      throws Exception {
    List<Map<CubeTag, ThirdEyeRequestMetricExpressions>> bulkRequests = new ArrayList<>();
    for (int level = 0; level < dimensions.size() + 1; ++level) {
      List<String> groupBy = Lists.newArrayList(dimensions.namesToDepth(level));
      bulkRequests.add(
          constructBulkRequests(cubeMetric.getDataset(), cubeMetric.getCubeSpecs(), groupBy, filterSets));
    }
    return constructAggregatedValues(dimensions, bulkRequests);
  }
}
