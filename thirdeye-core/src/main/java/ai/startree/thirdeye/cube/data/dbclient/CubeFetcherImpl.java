/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbclient;

import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import ai.startree.thirdeye.cube.data.dbrow.Row;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

  private final DataSourceCache dataSourceCache;
  private final CubeMetric<R> cubeMetric;

  /**
   * Constructs a Cube client.
   *
   */
  public CubeFetcherImpl(DataSourceCache dataSourceCache, CubeMetric<R> cubeMetric) {
    this.dataSourceCache = Preconditions.checkNotNull(dataSourceCache);
    this.cubeMetric = cubeMetric;
  }

  /**
   * Construct bulks ThirdEye requests.
   *
   * @param datasetConfigDTO dataset config.
   * @param cubeSpecs the spec to retrieve the metrics.
   * @param groupBy groupBy for database.
   * @param filterSets the data filter.
   * @return a list of ThirdEye requests.
   */
  protected Map<CubeTag, ThirdEyeRequest> constructBulkRequests(
      DatasetConfigDTO datasetConfigDTO,
      List<CubeSpec> cubeSpecs, List<String> groupBy, Multimap<String, String> filterSets) {

    Map<CubeTag, ThirdEyeRequest> requests = new HashMap<>();

    for (CubeSpec cubeSpec : cubeSpecs) {
      // Set dataset and metric
      MetricConfigDTO metricConfigDTO = cubeSpec.getMetric();
      MetricFunction  metricFunction = new MetricFunction(metricConfigDTO, datasetConfigDTO);

      ThirdEyeRequest.ThirdEyeRequestBuilder builder = ThirdEyeRequest.newBuilder();

      builder.setMetricFunction(metricFunction);
      builder.setDataSource(datasetConfigDTO.getDataSource());

      // Set start and end time
      builder.setStartTimeInclusive(cubeSpec.getInterval().getStart());
      builder.setEndTimeExclusive(cubeSpec.getInterval().getEnd());

      // Set groupBy and filter
      builder.setGroupBy(groupBy);
      builder.setFilterSet(filterSets);

      requests.put(cubeSpec.getTag(), builder.build(cubeSpec.getTag().toString()));
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
  protected void buildMetricFunctionOrExpressionsRows(Dimensions dimensions, ThirdEyeResponse response,
      Map<List<String>, R> rowTable, CubeTag tag) {
    for (int rowIdx = 0; rowIdx < response.getNumRows(); ++rowIdx) {
      // If the metric expression is a single metric function, then we get the value immediately
      double value = response.getRow(rowIdx).getMetrics().get(0);
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
      List<Map<CubeTag, ThirdEyeRequest>> bulkRequests) throws Exception {

    List<ThirdEyeRequest> allRequests = new ArrayList<>();
    for (Map<CubeTag, ThirdEyeRequest> bulkRequest : bulkRequests) {
      for (Map.Entry<CubeTag, ThirdEyeRequest> entry : bulkRequest.entrySet()) {
        ThirdEyeRequest thirdEyeRequest = entry.getValue();
        allRequests.add(thirdEyeRequest);
      }
    }

    Map<ThirdEyeRequest, Future<ThirdEyeResponse>> queryResponses = dataSourceCache
        .getQueryResultsAsync(allRequests);

    List<List<R>> res = new ArrayList<>();
    int level = 0;
    for (Map<CubeTag, ThirdEyeRequest> bulkRequest : bulkRequests) {
      Map<List<String>, R> rowOfSameLevel = new HashMap<>();

      for (Map.Entry<CubeTag, ThirdEyeRequest> entry : bulkRequest.entrySet()) {
        CubeTag tag = entry.getKey();
        ThirdEyeRequest thirdEyeRequest = entry.getValue();
        ThirdEyeResponse thirdEyeResponse = queryResponses.get(thirdEyeRequest)
            .get(TIME_OUT_VALUE, TIME_OUT_UNIT);
        if (thirdEyeResponse.getNumRows() == 0) {
          LOG.warn("Get 0 rows from the request(s): {}", thirdEyeRequest);
        }
        buildMetricFunctionOrExpressionsRows(dimensions, thirdEyeResponse, rowOfSameLevel, tag);
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
    List<Map<CubeTag, ThirdEyeRequest>> bulkRequests = Collections.singletonList(
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
    List<Map<CubeTag, ThirdEyeRequest>> bulkRequests = new ArrayList<>();
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
    List<Map<CubeTag, ThirdEyeRequest>> bulkRequests = new ArrayList<>();
    for (int level = 0; level < dimensions.size() + 1; ++level) {
      List<String> groupBy = Lists.newArrayList(dimensions.namesToDepth(level));
      bulkRequests.add(
          constructBulkRequests(cubeMetric.getDataset(), cubeMetric.getCubeSpecs(), groupBy, filterSets));
    }
    return constructAggregatedValues(dimensions, bulkRequests);
  }
}
