/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbclient;

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.cube.additive.AdditiveRow;
import ai.startree.thirdeye.cube.data.dbrow.DimensionValues;
import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import ai.startree.thirdeye.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class retrieves the data for the cube algorithm.
 */
public class CubeFetcher {

  private static final Logger LOG = LoggerFactory.getLogger(CubeFetcher.class);
  private final static int TIME_OUT_VALUE = 1200;
  private final static TimeUnit TIME_OUT_UNIT = TimeUnit.SECONDS;
  public static final int QUERY_LIMIT = 100000;

  private final AggregationLoader aggregationLoader;
  private final MetricSlice currentSlice;
  private final MetricSlice baselineSlice;

  /**
   * Constructs a Cube client.
   */
  public CubeFetcher(final AggregationLoader aggregationLoader, final MetricSlice currentSlice,
      final MetricSlice baselineSlice) {
    this.aggregationLoader = aggregationLoader;
    this.currentSlice = currentSlice;
    this.baselineSlice = baselineSlice;
  }

  public double getBaselineTotal() throws Exception {
    return getTotal(baselineSlice, CubeTag.Baseline.name());
  }

  public double getCurrentTotal() throws Exception {
    return getTotal(currentSlice, CubeTag.Current.name());
  }

  private double getTotal(final MetricSlice slice, final String sliceName) throws Exception {
    final DataFrame df = aggregationLoader.loadAggregate(slice, List.of(), 2);
    checkArgument(!df.isEmpty(),
        String.format("No data found in %s timeframe. Cannot perform dimension analysis.",
            sliceName));

    return df.getDouble(Constants.COL_VALUE, 0);
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

  /**
   * Returns a list of rows. The value of each row is evaluated and no further processing is needed.
   *
   * @param dimensions dimensions of the response
   * @param dataFrame the response dataFrame from backend database
   * @param rowTable the storage for rows
   * @param tag true if the response is for baseline values
   */
  protected void buildMetricFunctionOrExpressionsRows(List<String> dimensions, DataFrame dataFrame,
      Map<List<String>, AdditiveRow> rowTable, CubeTag tag, final Dimensions baseDimensions) {
    for (int rowIdx = 0; rowIdx < dataFrame.size(); ++rowIdx) {
      // If the metric expression is a single metric function, then we get the value immediately
      double value = dataFrame.getDouble(Constants.COL_VALUE, rowIdx);
      // fixme cyril - investigate why null values happen - in the mean time mitigate by replacing by "null"
      List<@NonNull String> dimensionValues = new ArrayList<>();
      for (String dimensionName: dimensions) {
        String dimensionValue = dataFrame.getString(dimensionName, rowIdx);
        if (dimensionValue == null) {
          LOG.warn("Encountered null dimension value for dimension: {}. Should not happen. Mitigating - replacing by \"null\".", dimensionName);
          dimensionValue = "null";
        }
        dimensionValues.add(dimensionValue);
      }
      fillValueToRowTable(rowTable, baseDimensions, dimensionValues, value, tag);
    }
  }

  /**
     * Returns the baseline and current value for nodes at each dimension from the given list.
     * For instance, if the list has ["country", "page name"], then it returns nodes of ["US", "IN",
     * "JP", ...,
     * "linkedin.com", "google.com", ...]
     *
     * @param dimensions the list of dimensions.
     * @return the baseline and current value for nodes at each dimension from the given list.
     */
  public List<List<AdditiveRow>> getAggregatedValuesOfDimension(Dimensions dimensions) throws Exception {

    List<List<String>> dimensionsLists = new ArrayList<>();
    List<Future<DataFrame>> baselineResults = new ArrayList<>();
    List<Future<DataFrame>> currentResults = new ArrayList<>();
    for (int i = 0; i < dimensions.size(); ++i) {
      final List<String> groupByDimensions = List.of(dimensions.get(i));
      dimensionsLists.add(groupByDimensions);
      baselineResults.add(aggregationLoader.loadAggregateAsync(baselineSlice,
          groupByDimensions,
          QUERY_LIMIT));
      currentResults.add(aggregationLoader.loadAggregateAsync(currentSlice,
          groupByDimensions,
          QUERY_LIMIT));
    }

    return constructRows(dimensionsLists, baselineResults, currentResults, dimensions);
  }

  @NonNull
  private List<List<AdditiveRow>> constructRows(List<List<String>> dimensionsLists,
      final List<Future<DataFrame>> baselineResults, final List<Future<DataFrame>> currentResults,
      final Dimensions baseDimensions)
      throws InterruptedException, ExecutionException, TimeoutException {
    List<List<AdditiveRow>> res = new ArrayList<>();
    for (int i = 0; i < dimensionsLists.size(); i++) {
      Map<List<String>, AdditiveRow> rowOfSameLevel = new HashMap<>();
      final List<String> dimensions = dimensionsLists.get(i);
      addRow(rowOfSameLevel, dimensions, CubeTag.Baseline, baselineResults.get(i), baseDimensions);
      addRow(rowOfSameLevel, dimensions, CubeTag.Current, currentResults.get(i), baseDimensions);
      if (rowOfSameLevel.size() == 0) {
        LOG.warn("Failed to retrieve non-zero results for dimensions {}.", dimensions);
      }
      List<AdditiveRow> rows = new ArrayList<>(rowOfSameLevel.values());
      res.add(rows);
    }

    return res;
  }

  private void addRow(final Map<List<String>, AdditiveRow> rowOfSameLevel,
      final List<String> dimensions, final CubeTag tag, final Future<DataFrame> future,
      final Dimensions baseDimensions)
      throws InterruptedException, ExecutionException, TimeoutException {
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
    buildMetricFunctionOrExpressionsRows(dimensions, df, rowOfSameLevel, tag, baseDimensions);
  }

  /**
     * Returns the baseline and current value for nodes for each dimension combination.
     * For instance, if the list has ["country", "page name"], then it returns nodes of
     * [
     * ["US", "IN", "JP", ...,],
     * ["US, linkedin.com", "US, google.com", "IN, linkedin.com", "IN, google.com", "JP,
     * linkedin.com", "JP, google.com", ...]
     * ]
     *
     * @param dimensions the dimensions to be drilled down.
     * @return the baseline and current value for nodes for each dimension combination.
     */
  public List<List<AdditiveRow>> getAggregatedValuesOfLevels(Dimensions dimensions) throws Exception {

    List<List<String>> dimensionsLists = new ArrayList<>();
    List<Future<DataFrame>> baselineResults = new ArrayList<>();
    List<Future<DataFrame>> currentResults = new ArrayList<>();
    for (int level = 0; level < dimensions.size() + 1; ++level) {
      final List<String> groupByDimensions = dimensions.namesToDepth(level);
      dimensionsLists.add(groupByDimensions);
      baselineResults.add(aggregationLoader.loadAggregateAsync(baselineSlice,
          groupByDimensions,
          QUERY_LIMIT));
      currentResults.add(aggregationLoader.loadAggregateAsync(currentSlice,
          groupByDimensions,
          QUERY_LIMIT));
    }

    return constructRows(dimensionsLists, baselineResults, currentResults, dimensions);
  }
}
