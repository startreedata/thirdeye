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

package ai.startree.thirdeye.detection.anomaly.detection;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.datasource.ResponseParserUtils;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.timeseries.AnomalyDetectionTimeSeriesResponseParser;
import ai.startree.thirdeye.datasource.timeseries.TimeSeriesHandler;
import ai.startree.thirdeye.datasource.timeseries.TimeSeriesRequest;
import ai.startree.thirdeye.datasource.timeseries.TimeSeriesResponse;
import ai.startree.thirdeye.datasource.timeseries.TimeSeriesResponseConverter;
import ai.startree.thirdeye.datasource.timeseries.TimeSeriesRow;
import ai.startree.thirdeye.metric.MetricTimeSeries;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.dimension.DimensionKey;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import ai.startree.thirdeye.spi.util.Pair;
import ai.startree.thirdeye.spi.util.SpiUtils;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import ai.startree.thirdeye.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnomalyDetectionInputContextBuilder {

  private static final Logger LOG = LoggerFactory
      .getLogger(AnomalyDetectionInputContextBuilder.class);

  private final DataSourceCache dataSourceCache;
  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final DatasetConfigManager datasetConfigManager;

  private AnomalyDetectionInputContext anomalyDetectionInputContext;
  private AnomalyFunctionDTO anomalyFunctionSpec;

  public AnomalyDetectionInputContextBuilder(final DataSourceCache dataSourceCache,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final DatasetConfigManager datasetConfigManager) {
    this.dataSourceCache = dataSourceCache;
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.datasetConfigManager = datasetConfigManager;
  }

  /**
   * Get the metric filter setting for an anomaly function
   */
  public static Multimap<String, String> getFiltersForFunction(String filterString) {
    // Get the original filter
    Multimap<String, String> filters;
    if (StringUtils.isNotBlank(filterString)) {
      filters = SpiUtils.getFilterSet(filterString);
    } else {
      filters = HashMultimap.create();
    }
    return filters;
  }

  public AnomalyDetectionInputContextBuilder setFunction(AnomalyFunctionDTO anomalyFunctionSpec)
      throws Exception {
    return setFunction(anomalyFunctionSpec, new AnomalyDetectionInputContext());
  }

  public AnomalyDetectionInputContextBuilder setFunction(AnomalyFunctionDTO anomalyFunctionSpec,
      AnomalyDetectionInputContext anomalyDetectionInputContext)
      throws Exception {
    this.anomalyFunctionSpec = anomalyFunctionSpec;
    this.anomalyDetectionInputContext = anomalyDetectionInputContext;
    final String dataset = this.anomalyFunctionSpec.getCollection();
    DatasetConfigDTO datasetConfig = datasetConfigManager.findByDataset(dataset);
    if (datasetConfig == null) {
      LOG.error("Dataset [" + dataset + "] is not found");
      throw new IllegalArgumentException(
          "Dataset [" + dataset + "] is not found with function : " + anomalyFunctionSpec
              .toString());
    }
    return this;
  }

  public AnomalyDetectionInputContext build() {
    return this.anomalyDetectionInputContext;
  }

  /**
   * Fetch time series, known merged anomalies, and scaling factor for the specified dimension. Note
   * that scaling
   * factor has no dimension information, so all scaling factor in the specified time range will be
   * retrieved.
   *
   * @param startEndTimeRanges the start and end time range for retrieving the data
   * @param dimensions the dimension of the data
   * @param endTimeInclusive set to true if the end time should be inclusive; mainly used by the
   *     queries from UI
   * @return the builder of the AnomalyDetectionInputContext
   * @throws Exception if it fails to retrieve time series from DB.
   */
  public AnomalyDetectionInputContextBuilder fetchTimeSeriesDataByDimension(
      List<Pair<Long, Long>> startEndTimeRanges,
      DimensionMap dimensions,
      boolean endTimeInclusive,
      final DatasetConfigDTO datasetConfig)
      throws Exception {
    TimeGranularity timeGranularity = new TimeGranularity(anomalyFunctionSpec.getBucketSize(),
        anomalyFunctionSpec.getBucketUnit());

    // Retrieve Time Series
    MetricTimeSeries metricTimeSeries = getTimeSeriesByDimension(anomalyFunctionSpec,
        startEndTimeRanges,
        dimensions,
        timeGranularity,
        endTimeInclusive,
        datasetConfig);
    Map<DimensionMap, MetricTimeSeries> metricTimeSeriesMap = new HashMap<>();
    metricTimeSeriesMap.put(dimensions, metricTimeSeries);
    this.anomalyDetectionInputContext.setDimensionMapMetricTimeSeriesMap(metricTimeSeriesMap);

    return this;
  }

  /**
   * Returns the metric time series that were given to the anomaly function for anomaly detection.
   * If the dimension to
   * retrieve is OTHER, this method retrieves all combinations of dimensions and calculate the
   * metric time series for
   * OTHER dimension on-the-fly.
   *
   * @param anomalyFunctionSpec spec of the anomaly function
   * @param startEndTimeRanges the time ranges to retrieve the data for constructing the time
   *     series
   * @param dimensionMap a dimension map that is used to construct the filter for retrieving the
   *     corresponding data
   *     that was used to detected the anomaly
   * @param timeGranularity time granularity of the time series
   * @param endTimeInclusive set to true if the end time should be inclusive; mainly used by the
   *     query for UI
   * @return the time series in the same format as those used by the given anomaly function for
   *     anomaly detection
   */
  public MetricTimeSeries getTimeSeriesByDimension(AnomalyFunctionDTO anomalyFunctionSpec,
      List<Pair<Long, Long>> startEndTimeRanges,
      DimensionMap dimensionMap,
      TimeGranularity timeGranularity,
      boolean endTimeInclusive,
      final DatasetConfigDTO datasetConfig)
      throws ExecutionException {

    // Get the original filter
    Multimap<String, String> filters = getFiltersForFunction(anomalyFunctionSpec.getFilters());

    // Decorate filters according to dimensionMap
    filters = ThirdEyeUtils.getFilterSetFromDimensionMap(dimensionMap, filters);

    boolean hasOTHERDimensionName = false;
    for (String dimensionValue : dimensionMap.values()) {
      if (dimensionValue.equalsIgnoreCase(ResponseParserUtils.OTHER)) {
        hasOTHERDimensionName = true;
        break;
      }
    }

    // groupByDimensions (i.e., exploreDimensions) is empty by default because the query for getting the time series
    // will have the decorated filters according to anomalies' explore dimensions.
    // However, if there exists any dimension with value "OTHER, then we need to honor the origin groupBy in order to
    // construct the data for OTHER
    List<String> groupByDimensions = Collections.emptyList();
    if (hasOTHERDimensionName && StringUtils
        .isNotBlank(anomalyFunctionSpec.getExploreDimensions().trim())) {
      groupByDimensions = Arrays
          .asList(anomalyFunctionSpec.getExploreDimensions().trim().split(","));
    }

    TimeSeriesResponse response =
        getTimeSeriesResponseImpl(anomalyFunctionSpec, startEndTimeRanges,
            timeGranularity, filters, groupByDimensions, endTimeInclusive);
    try {

      Map<DimensionKey, MetricTimeSeries> metricTimeSeriesMap = TimeSeriesResponseConverter
          .toMap(response, datasetConfig.getDimensions());
      return extractMetricTimeSeriesByDimension(metricTimeSeriesMap);
    } catch (Exception e) {
      LOG.warn("Unable to get schema dimension name for retrieving metric time series: {}",
          e.toString());
      return null;
    }
  }

  /**
   * Extract current and baseline values from the parsed Pinot results. There are two possible time
   * series for presenting
   * the time series after anomaly detection: 1. the time series with a specific dimension and 2.
   * the time series for
   * OTHER dimension.
   *
   * For case 1, the input map should contain only one time series and hence we can just return it.
   * For case 2, the
   * input map would contain all combination of explored dimension and hence we need to filter out
   * the one for OTHER
   * dimension.
   *
   * @return the time series when the anomaly is detected
   */
  private MetricTimeSeries extractMetricTimeSeriesByDimension(
      Map<DimensionKey, MetricTimeSeries> metricTimeSeriesMap) {
    MetricTimeSeries metricTimeSeries = null;
    if (MapUtils.isNotEmpty(metricTimeSeriesMap)) {
      // For most anomalies, there should be only one time series due to its dimensions. The exception is the OTHER
      // dimension, in which time series of different dimensions are returned due to the calculation of OTHER dimension.
      // Therefore, we need to get the time series of OTHER dimension manually.
      if (metricTimeSeriesMap.size() == 1) {
        Iterator<MetricTimeSeries> ite = metricTimeSeriesMap.values().iterator();
        if (ite.hasNext()) {
          metricTimeSeries = ite.next();
        }
      } else { // Retrieve the time series of OTHER dimension
        for (final Map.Entry<DimensionKey, MetricTimeSeries> entry : metricTimeSeriesMap
            .entrySet()) {
          DimensionKey dimensionKey = entry.getKey();
          boolean foundOTHER = false;
          for (String dimensionValue : dimensionKey.getDimensionValues()) {
            if (dimensionValue.equalsIgnoreCase(ResponseParserUtils.OTHER)) {
              metricTimeSeries = entry.getValue();
              foundOTHER = true;
              break;
            }
          }
          if (foundOTHER) {
            break;
          }
        }
      }
    }
    return metricTimeSeries;
  }

  private TimeSeriesResponse getTimeSeriesResponseImpl(AnomalyFunctionDTO anomalyFunctionSpec,
      List<Pair<Long, Long>> startEndTimeRanges, TimeGranularity timeGranularity,
      Multimap<String, String> filters,
      List<String> groupByDimensions, boolean endTimeInclusive)
      throws ExecutionException {
    return getTimeSeriesResponseImpl(anomalyFunctionSpec, anomalyFunctionSpec.getMetrics(),
        startEndTimeRanges,
        timeGranularity, filters, groupByDimensions, endTimeInclusive);
  }

  private TimeSeriesResponse getTimeSeriesResponseImpl(AnomalyFunctionDTO anomalyFunctionSpec,
      List<String> metrics,
      List<Pair<Long, Long>> startEndTimeRanges, TimeGranularity timeGranularity,
      Multimap<String, String> filters,
      List<String> groupByDimensions, boolean endTimeInclusive)
      throws ExecutionException {

    TimeSeriesHandler timeSeriesHandler = new TimeSeriesHandler(dataSourceCache,
        thirdEyeCacheRegistry);

    // Seed request with top-level...
    TimeSeriesRequest seedRequest = new TimeSeriesRequest();
    seedRequest.setCollectionName(anomalyFunctionSpec.getCollection());
    // TODO: Check low level support for multiple metrics retrieval
    String metricsToRetrieve = StringUtils.join(metrics, ",");
    List<MetricExpression> metricExpressions = Utils
        .convertToMetricExpressions(metricsToRetrieve,
            anomalyFunctionSpec.getMetricFunction(), anomalyFunctionSpec.getCollection(),
            thirdEyeCacheRegistry);
    seedRequest.setMetricExpressions(metricExpressions);
    seedRequest.setAggregationTimeGranularity(timeGranularity);
    seedRequest.setEndDateInclusive(false);
    seedRequest.setFilterSet(filters);
    seedRequest.setGroupByDimensions(groupByDimensions);
    seedRequest.setEndDateInclusive(endTimeInclusive);

    LOG.info("Found [{}] time ranges to fetch data for metric(s): {}, with filter: {}",
        startEndTimeRanges.size(), metricsToRetrieve, filters);

    // NOTE: another ThirdEye-esque hack. This code is to be deprecated, so no value in refactoring it.
    DateTimeZone timeZone = Utils.getDateTimeZone(anomalyFunctionSpec.getCollection(),
        thirdEyeCacheRegistry);

    // MultiQuery request
    List<Future<TimeSeriesResponse>> futureResponses = new ArrayList<>();
    List<TimeSeriesRequest> requests = new ArrayList<>();
    Set<TimeSeriesRow> timeSeriesRowSet = new HashSet<>();
    for (Pair<Long, Long> startEndInterval : startEndTimeRanges) {
      TimeSeriesRequest request = new TimeSeriesRequest(seedRequest);
      DateTime startTime = new DateTime(startEndInterval.getFirst(), timeZone);
      DateTime endTime = new DateTime(startEndInterval.getSecond(), timeZone);
      request.setStart(startTime);
      request.setEnd(endTime);

      Future<TimeSeriesResponse> response = timeSeriesHandler
          .asyncHandle(request, new AnomalyDetectionTimeSeriesResponseParser());
      if (response != null) {
        futureResponses.add(response);
        requests.add(request);
        LOG.info(
            "Fetching time series for range: [{} -- {}], metricExpressions: [{}], timeGranularity: [{}]",
            startTime, endTime, metricExpressions, timeGranularity);
      }
    }

    for (int i = 0; i < futureResponses.size(); i++) {
      Future<TimeSeriesResponse> futureResponse = futureResponses.get(i);
      TimeSeriesRequest request = requests.get(i);
      try {
        TimeSeriesResponse response = futureResponse.get();
        timeSeriesRowSet.addAll(response.getRows());
      } catch (InterruptedException e) {
        LOG.warn("Failed to fetch data with request: [{}]", request);
      }
    }

    timeSeriesHandler.shutdownAsyncHandler();

    List<TimeSeriesRow> timeSeriesRows = new ArrayList<>();
    timeSeriesRows.addAll(timeSeriesRowSet);

    return new TimeSeriesResponse(timeSeriesRows);
  }
}
