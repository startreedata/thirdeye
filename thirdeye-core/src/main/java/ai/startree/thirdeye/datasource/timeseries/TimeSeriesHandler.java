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

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.timeseries.TimeSeriesRow.TimeSeriesMetric;
import ai.startree.thirdeye.detection.anomaly.utils.AnomalyUtils;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest.ThirdEyeRequestBuilder;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import ai.startree.thirdeye.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSeriesHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesHandler.class);
  private final TimeSeriesResponseParser defaultTimeseriesResponseParser;
  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;

  private final DataSourceCache dataSourceCache;
  private ExecutorService executorService;

  public TimeSeriesHandler(DataSourceCache dataSourceCache,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    this.dataSourceCache = dataSourceCache;
    defaultTimeseriesResponseParser = new UITimeSeriesResponseParser(dataSourceCache);
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
  }

  /**
   * Handles the given time series request using the default time series parser (i.e., {@link
   * UITimeSeriesResponseParser}.)
   *
   * @param timeSeriesRequest the request to retrieve time series.
   * @return the time series for the given request.
   * @throws Exception Any exception that is thrown during the retrieval.
   */
  public TimeSeriesResponse handle(TimeSeriesRequest timeSeriesRequest) throws Exception {
    return handle(timeSeriesRequest, defaultTimeseriesResponseParser);
  }

  /**
   * Handles the given time series request using the given time series parser.
   *
   * @param timeSeriesRequest the request to retrieve time series.
   * @return the time series for the given request.
   * @throws Exception Any exception that is thrown during the retrieval.
   */
  public TimeSeriesResponse handle(TimeSeriesRequest timeSeriesRequest,
      TimeSeriesResponseParser timeSeriesResponseParser) throws Exception {
    // Time ranges for creating ThirdEye request
    DateTime start = timeSeriesRequest.getStart();
    DateTime end = timeSeriesRequest.getEnd();
    if (timeSeriesRequest.isEndDateInclusive()) {
      // ThirdEyeRequest is exclusive endpoint, so increment by one bucket
      TimeGranularity aggregationTimeGranularity = timeSeriesRequest
          .getAggregationTimeGranularity();
      end = end.plus(aggregationTimeGranularity.toMillis());
    }
    // Create request
    ThirdEyeRequest request = createThirdEyeRequest("timeseries", timeSeriesRequest, start, end);
    Future<ThirdEyeResponse> responseFuture = dataSourceCache.getQueryResultAsync(request);
    // 5 minutes timeout
    ThirdEyeResponse response = responseFuture.get(5, TimeUnit.MINUTES);
    List<TimeSeriesRow> rows = timeSeriesResponseParser.parseResponse(response);
    // compute the derived metrics
    computeDerivedMetrics(timeSeriesRequest, rows);
    return new TimeSeriesResponse(rows);
  }

  private void computeDerivedMetrics(TimeSeriesRequest timeSeriesRequest, List<TimeSeriesRow> rows)
      throws Exception {
    // compute list of derived expressions
    List<MetricFunction> metricFunctionsFromExpressions =
        Utils.computeMetricFunctionsFromExpressions(timeSeriesRequest.getMetricExpressions(),
            thirdEyeCacheRegistry);
    Set<String> metricNameSet = new HashSet<>();
    for (MetricFunction function : metricFunctionsFromExpressions) {
      metricNameSet.add(function.getMetricName());
    }
    List<MetricExpression> derivedMetricExpressions = new ArrayList<>();
    for (MetricExpression expression : timeSeriesRequest.getMetricExpressions()) {
      if (!metricNameSet.contains(expression.getExpressionName())) {
        derivedMetricExpressions.add(expression);
      }
    }

    // add metric expressions
    if (derivedMetricExpressions.size() > 0) {
      Map<String, Double> valueContext = new HashMap<>();
      for (TimeSeriesRow row : rows) {
        valueContext.clear();
        List<TimeSeriesMetric> metrics = row.getMetrics();
        // baseline value
        for (TimeSeriesMetric metric : metrics) {
          valueContext.put(metric.getMetricName(), metric.getValue());
        }
        for (MetricExpression expression : derivedMetricExpressions) {
          String derivedMetricExpression = expression.getExpression();
          double derivedMetricValue =
              MetricExpression.evaluateExpression(derivedMetricExpression, valueContext);
          if (Double.isInfinite(derivedMetricValue) || Double.isNaN(derivedMetricValue)) {
            derivedMetricValue = 0;
          }

          row.getMetrics().add(
              new TimeSeriesMetric(expression.getExpressionName(), derivedMetricValue));
        }
      }
    }
  }

  /**
   * An asynchrous method for handling the time series request. This method initializes executor
   * service (if necessary)
   * and invokes the synchronous method -- handle() -- in the backend. After invoking this method,
   * users could invoke
   * shutdownAsyncHandler() to shutdown the executor service if it is no longer needed.
   *
   * @param timeSeriesRequest the request to retrieve time series.
   * @param timeSeriesResponseParser the parser to be used to parse the result from data source
   * @return a future object of time series response for the give request. Returns null if it fails
   *     to handle the
   *     request.
   */
  public Future<TimeSeriesResponse> asyncHandle(final TimeSeriesRequest timeSeriesRequest,
      final TimeSeriesResponseParser timeSeriesResponseParser) {
    // For optimizing concurrency performance by reducing the access to the synchronized method
    if (executorService == null) {
      startAsyncHandler();
    }

    Future<TimeSeriesResponse> responseFuture = executorService
        .submit(() -> {
          try {
            return TimeSeriesHandler.this.handle(timeSeriesRequest, timeSeriesResponseParser);
          } catch (Exception e) {
            LOG.warn("Failed to retrieve time series of the request: {}", timeSeriesRequest);
          }
          return null;
        });

    return responseFuture;
  }

  /**
   * Initializes executor service if it is null. This method is thread-safe.
   */
  private synchronized void startAsyncHandler() {
    if (executorService == null) {
      executorService = Executors.newFixedThreadPool(10);
    }
  }

  /**
   * Shutdown the executor service of this TimeSeriesHandler safely.
   */
  public void shutdownAsyncHandler() {
    AnomalyUtils.safelyShutdownExecutionService(executorService, this.getClass());
  }

  private ThirdEyeRequest createThirdEyeRequest(String requestReference,
      TimeSeriesRequest timeSeriesRequest, DateTime start, DateTime end) {
    ThirdEyeRequestBuilder requestBuilder = ThirdEyeRequest.newBuilder();
    requestBuilder.setStartTimeInclusive(start);
    requestBuilder.setEndTimeExclusive(end);
    requestBuilder.setFilterSet(timeSeriesRequest.getFilterSet());
    requestBuilder.addGroupBy(timeSeriesRequest.getGroupByDimensions());
    requestBuilder.setGroupByTimeGranularity(timeSeriesRequest.getAggregationTimeGranularity());
    List<MetricExpression> metricExpressions = timeSeriesRequest.getMetricExpressions();
    List<MetricFunction> metricFunctionsFromExpressions =
        Utils.computeMetricFunctionsFromExpressions(metricExpressions, thirdEyeCacheRegistry);
    requestBuilder.setMetricFunctions(metricFunctionsFromExpressions);
    requestBuilder.setDataSource(
        ThirdEyeUtils.getDataSourceFromMetricFunctions(metricFunctionsFromExpressions));
    return requestBuilder.build(requestReference);
  }
}
