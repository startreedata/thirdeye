/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbclient;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import ai.startree.thirdeye.cube.additive.AdditiveCubeMetric;
import ai.startree.thirdeye.cube.additive.AdditiveRow;
import ai.startree.thirdeye.cube.data.dbrow.DimensionValues;
import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.calcite.CalciteRequest;
import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.datasource.calcite.QueryProjection;
import ai.startree.thirdeye.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.DimensionType;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
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
public class CubeFetcherImpl implements CubeFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(CubeFetcherImpl.class);
  private final static int TIME_OUT_VALUE = 1200;
  private final static TimeUnit TIME_OUT_UNIT = TimeUnit.SECONDS;
  public static final int QUERY_LIMIT = 100000;

  // todo this is temporary for step by step refactoring
  // dataSourceCache is legacy in cube fetcher - prefer using aggregationLoader - add methods to aggregationLoader if required
  private final DataSourceCache dataSourceCache;
  private final AdditiveCubeMetric cubeMetric;

  // todo this is new
  private final AggregationLoader aggregationLoader;
  private final MetricSlice currentSlice;
  private final MetricSlice baselineSlice;

  /**
   * Constructs a Cube client.
   */
  public CubeFetcherImpl(final DataSourceCache dataSourceCache,
      final AggregationLoader aggregationLoader,
      final MetricSlice currentSlice,
      final MetricSlice baselineSlice,
      @Deprecated final AdditiveCubeMetric cubeMetric) {
    this.dataSourceCache = Preconditions.checkNotNull(dataSourceCache);
    this.aggregationLoader = aggregationLoader;
    this.currentSlice = currentSlice;
    this.baselineSlice = baselineSlice;
    this.cubeMetric = cubeMetric;
  }

  @Override
  public MetricSlice getCurrentSlice() {
    return currentSlice;
  }

  @Override
  public MetricSlice getBaselineSlice() {
    return baselineSlice;
  }

  /**
   * Construct bulks ThirdEye requests.
   *
   * @param datasetConfigDTO dataset config.
   * @param cubeSpecs the spec to retrieve the metrics.
   * @param groupBy groupBy for database.
   * @param predicates the filter predicates.
   * @return a list of ThirdEye requests.
   */
  protected Map<CubeTag, CalciteRequest> constructBulkRequests(DatasetConfigDTO datasetConfigDTO,
      List<CubeSpec> cubeSpecs, List<String> groupBy, List<Predicate> predicates) {

    Map<CubeTag, CalciteRequest> requests = new HashMap<>();

    for (CubeSpec cubeSpec : cubeSpecs) {
      final CalciteRequest calciteRequest = constructRequest(datasetConfigDTO,
          groupBy,
          predicates,
          cubeSpec);
      requests.put(cubeSpec.getTag(), calciteRequest);
    }

    return requests;
  }

  private CalciteRequest constructRequest(final DatasetConfigDTO datasetConfigDTO,
      final List<String> groupBy, final List<Predicate> predicates, final CubeSpec cubeSpec) {
    final MetricConfigDTO metricConfigDTO = cubeSpec.getMetric();
    final CalciteRequest.Builder builder = CalciteRequest.newBuilder(datasetConfigDTO.getDataset())
        .withTimeFilter(cubeSpec.getInterval(),
            datasetConfigDTO.getTimeColumn(),
            datasetConfigDTO.getTimeFormat(),
            datasetConfigDTO.getTimeUnit().name())
        .addSelectProjection(QueryProjection.fromMetricConfig(metricConfigDTO)
            .withAlias(Constants.COL_VALUE))
        // notice this limit has a significant impact on the algorithm. When dicing by many dimensions, num of rows grows exponentially.
        // Because there is no sorting, there is no insurance we will get the relevant rows, the ones with the biggest impact
        // the query limit is high but does not solve the problem - a warning is raised if the limit is reached
        .withLimit(QUERY_LIMIT);
    if (isNotBlank(metricConfigDTO.getWhere())) {
      builder.addPredicate(metricConfigDTO.getWhere());
    }
    for (Predicate predicate : predicates) {
      builder.addPredicate(QueryPredicate.of(predicate, DimensionType.STRING));
    }
    for (String groupByColumn : groupBy) {
      QueryProjection groupByProjection = QueryProjection.of(groupByColumn);
      builder.addSelectProjection(groupByProjection);
      builder.addGroupByProjection(groupByProjection);
    }

    return builder.build();
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
  protected void fillValueToRowTable(Map<List<String>, AdditiveRow> rowTable, Dimensions dimensions,
      List<String> dimensionValues, double value, CubeTag tag) {
    if (Double.compare(0d, value) >= 0) {
      LOG.warn("Value not added to rowTable: it is too small. Value: {}. Tag: {}", value, tag);
      return;
    }
    if (Double.isInfinite(value)) {
      LOG.warn("Value not added to rowTable: it is infinite. Value: {}. Tag: {}", value, tag);
      return;
    }
    AdditiveRow row = rowTable.get(dimensionValues);
    if (row == null) {
      row = new AdditiveRow(dimensions, new DimensionValues(dimensionValues));
      rowTable.put(dimensionValues, row);
    }
    switch (tag) {
      case Baseline:
        row.setBaselineValue(value);
        break;
      case Current:
        row.setCurrentValue(value);
        break;
      default:
        throw new IllegalArgumentException("Unsupported CubeTag: " + tag.name());
    }
  }

  protected void fillValueToRowTable2(Map<List<String>, AdditiveRow> rowTable, Dimensions dimensions,
      List<String> dimensionValues, double value, CubeTag tag) {
    if (Double.compare(0d, value) >= 0) {
      LOG.warn("Value not added to rowTable: it is too small. Value: {}. Tag: {}", value, tag);
      return;
    }
    if (Double.isInfinite(value)) {
      LOG.warn("Value not added to rowTable: it is infinite. Value: {}. Tag: {}", value, tag);
      return;
    }
    AdditiveRow row = rowTable.get(dimensionValues);
    if (row == null) {
      row = new AdditiveRow(dimensions, new DimensionValues(dimensionValues));
      rowTable.put(dimensionValues, row);
    }
    switch (tag) {
      case Baseline:
        row.setBaselineValue(value);
        break;
      case Current:
        row.setCurrentValue(value);
        break;
      default:
        throw new IllegalArgumentException("Unsupported CubeTag: " + tag.name());
    }
  }

  /**
   * Returns a list of rows. The value of each row is evaluated and no further processing is needed.
   *
   * @param dimensions dimensions of the response
   * @param dataFrame the response dataFrame from backend database
   * @param rowTable the storage for rows
   * @param tag true if the response is for baseline values
   */
  protected void buildMetricFunctionOrExpressionsRows(Dimensions dimensions, DataFrame dataFrame,
      Map<List<String>, AdditiveRow> rowTable, CubeTag tag) {
    for (int rowIdx = 0; rowIdx < dataFrame.size(); ++rowIdx) {
      // If the metric expression is a single metric function, then we get the value immediately
      double value = dataFrame.getDouble(Constants.COL_VALUE, rowIdx);
      final int finalRowIdx = rowIdx;
      List<String> dimensionValues = dataFrame.getSeriesNames()
          .stream()
          .filter(name -> !name.equals(Constants.COL_VALUE))
          .map(dimensionName -> dataFrame.getString(dimensionName, finalRowIdx))
          .collect(Collectors.toList());
      fillValueToRowTable(rowTable, dimensions, dimensionValues, value, tag);
    }
  }

  protected void buildMetricFunctionOrExpressionsRows2(List<String> dimensions, DataFrame dataFrame,
      Map<List<String>, AdditiveRow> rowTable, CubeTag tag) {
    for (int rowIdx = 0; rowIdx < dataFrame.size(); ++rowIdx) {
      // If the metric expression is a single metric function, then we get the value immediately
      double value = dataFrame.getDouble(Constants.COL_VALUE, rowIdx);
      final int finalRowIdx = rowIdx;
      List<String> dimensionValues = dataFrame.getSeriesNames()
          .stream()
          .filter(name -> !name.equals(Constants.COL_VALUE))
          .map(dimensionName -> dataFrame.getString(dimensionName, finalRowIdx))
          .collect(Collectors.toList());
      fillValueToRowTable2(rowTable, new Dimensions(dimensions), dimensionValues, value, tag);
    }
  }

  /**
   * Converts Pinot results to Cube Rows.
   *
   * @param dimensions the dimension of the Pinot results.
   * @param bulkRequests the original requests of those results.
   * @return Cube rows.
   */
  protected List<List<AdditiveRow>> constructAggregatedValues(Dimensions dimensions,
      List<Map<CubeTag, CalciteRequest>> bulkRequests) throws Exception {

    List<CalciteRequest> allRequests = new ArrayList<>();
    bulkRequests.forEach(bulkRequest -> allRequests.addAll(bulkRequest.values()));

    Map<CalciteRequest, Future<DataFrame>> queryResponses = dataSourceCache.getQueryResultsAsync(
        allRequests,
        baselineSlice.getDatasetConfigDTO().getDataSource());

    List<List<AdditiveRow>> res = new ArrayList<>();
    int level = 0;
    for (Map<CubeTag, CalciteRequest> bulkRequest : bulkRequests) {
      Map<List<String>, AdditiveRow> rowOfSameLevel = new HashMap<>();

      for (Map.Entry<CubeTag, CalciteRequest> entry : bulkRequest.entrySet()) {
        CubeTag tag = entry.getKey();
        CalciteRequest calciteRequest = entry.getValue();
        DataFrame df = queryResponses.get(calciteRequest).get(TIME_OUT_VALUE, TIME_OUT_UNIT);
        if (df.size() == 0) {
          LOG.warn("Get 0 rows from the request: {}", calciteRequest);
        }
        if (df.size() == QUERY_LIMIT) {
          LOG.warn(
              "Got {} rows from the request. This corresponds to the LIMIT clause. "
                  + "Rows are randomly chosen, dimension analysis algorithm may not return the best results. Request: {}",
              calciteRequest);
        }
        buildMetricFunctionOrExpressionsRows(dimensions, df, rowOfSameLevel, tag);
      }
      if (rowOfSameLevel.size() == 0) {
        LOG.warn("Failed to retrieve non-zero results for requests of level {}. BulkRequest: {}",
            level,
            bulkRequest);
      }
      List<AdditiveRow> rows = new ArrayList<>(rowOfSameLevel.values());
      res.add(rows);
      ++level;
    }

    return res;
  }

  @Override
  public List<List<AdditiveRow>> getAggregatedValuesOfDimension(Dimensions dimensions,
      List<Predicate> predicates) throws Exception {

    List<List<String>> dimensionsLists = new ArrayList<>();
    List<Future<DataFrame>> baselineResults = new ArrayList<>();
    List<Future<DataFrame>> currentResults = new ArrayList<>();
    for (int i = 0; i < dimensions.size(); ++i) {
      dimensionsLists.add(List.of(dimensions.get(i)));
      baselineResults.add(aggregationLoader.loadAggregateAsync(baselineSlice,
          List.of(dimensions.get(i)),
          QUERY_LIMIT));
      currentResults.add(aggregationLoader.loadAggregateAsync(currentSlice,
          List.of(dimensions.get(i)),
          QUERY_LIMIT));
    }

    return constructRows(dimensionsLists, baselineResults, currentResults);
  }

  @NonNull
  private List<List<AdditiveRow>> constructRows(List<List<String>> dimensionsLists,
      final List<Future<DataFrame>> baselineResults,
      final List<Future<DataFrame>> currentResults)
      throws InterruptedException, ExecutionException, TimeoutException {
    List<List<AdditiveRow>> res = new ArrayList<>();
    for (int i = 0; i < dimensionsLists.size(); i++) {
      Map<List<String>, AdditiveRow> rowOfSameLevel = new HashMap<>();
      final List<String> dimensions = dimensionsLists.get(i);
      addRow(rowOfSameLevel, dimensions, CubeTag.Baseline, baselineResults.get(i));
      addRow(rowOfSameLevel, dimensions, CubeTag.Current, currentResults.get(i));
      if (rowOfSameLevel.size() == 0) {
        LOG.warn("Failed to retrieve non-zero results for dimensions {}.", dimensions);
      }
      List<AdditiveRow> rows = new ArrayList<>(rowOfSameLevel.values());
      res.add(rows);
    }

    return res;
  }

  private void addRow(final Map<List<String>, AdditiveRow> rowOfSameLevel,
      final List<String> dimensions, final CubeTag tag, final Future<DataFrame> future)
      throws InterruptedException, ExecutionException, TimeoutException {
    // fixme build a function and call 2 times rather than looping on map
    final DataFrame df = future.get(TIME_OUT_VALUE, TIME_OUT_UNIT);
    if (df.size() == 0) {
      LOG.warn("Got 0 rows for dimensions: {} for {} timeframe", dimensions, tag);
    }
    if (df.size() == QUERY_LIMIT) {
      LOG.warn(
          "Got {} rows for dimensions: {} for {} timeframe. This corresponds to the LIMIT clause. "
              + "Rows are randomly chosen, dimension analysis algorithm may not return the best results.",
          QUERY_LIMIT,
          dimensions,
          tag);
    }
    buildMetricFunctionOrExpressionsRows2(dimensions, df, rowOfSameLevel, tag);
  }

  @Override
  public List<List<AdditiveRow>> getAggregatedValuesOfLevels(Dimensions dimensions,
      List<Predicate> predicates) throws Exception {
    List<Map<CubeTag, CalciteRequest>> bulkRequests = new ArrayList<>();
    for (int level = 0; level < dimensions.size() + 1; ++level) {
      final Map<CubeTag, CalciteRequest> requests = constructBulkRequests(cubeMetric.getDataset(),
          cubeMetric.getCubeSpecs(),
          dimensions.namesToDepth(level),
          predicates);
      bulkRequests.add(requests);
    }
    return constructAggregatedValues(dimensions, bulkRequests);
  }
}
