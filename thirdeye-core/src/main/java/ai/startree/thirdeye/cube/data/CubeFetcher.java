/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data;

import static com.google.common.base.Preconditions.checkArgument;

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
    return getTotal(baselineSlice, "baseline");
  }

  public double getCurrentTotal() throws Exception {
    return getTotal(currentSlice, "current");
  }

  private double getTotal(final MetricSlice slice, final String sliceName) throws Exception {
    final DataFrame df = aggregationLoader.loadAggregate(slice, List.of(), 2);
    checkArgument(!df.isEmpty(),
        String.format("No data found in %s timeframe. Cannot perform dimension analysis.",
            sliceName));

    return df.getDouble(Constants.COL_VALUE, 0);
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
  public List<List<AdditiveRow>> getAggregatedValuesOfDimension(Dimensions dimensions)
      throws Exception {

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
  public List<List<AdditiveRow>> getAggregatedValuesOfLevels(Dimensions dimensions)
      throws Exception {

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

  @NonNull
  private List<List<AdditiveRow>> constructRows(List<List<String>> dimensionsLists,
      final List<Future<DataFrame>> baselineResults, final List<Future<DataFrame>> currentResults,
      final Dimensions baseDimensions)
      throws InterruptedException, ExecutionException, TimeoutException {
    List<List<AdditiveRow>> res = new ArrayList<>();
    for (int level = 0; level < dimensionsLists.size(); level++) {
      final List<String> levelDimensions = dimensionsLists.get(level);
      final List<AdditiveRow> rows = new RowsBuilder(baseDimensions, levelDimensions)
          .addBaselineRows(baselineResults.get(level).get(TIME_OUT_VALUE, TIME_OUT_UNIT))
          .addCurrentRows(currentResults.get(level).get(TIME_OUT_VALUE, TIME_OUT_UNIT))
          .build();
      if (rows.size() == 0) {
        LOG.warn("Failed to retrieve non-zero results for dimensions {}.", levelDimensions);
      }
      res.add(rows);
    }

    return res;
  }

  private static class RowsBuilder {

    private enum CubeTag {
      Baseline, Current,
    }

    private final Map<List<String>, AdditiveRow> rows = new HashMap<>();
    private final Dimensions baseDimensions;
    private final List<String> levelDimensions;

    public RowsBuilder(final Dimensions baseDimensions, final List<String> levelDimensions) {
      this.baseDimensions = baseDimensions;
      this.levelDimensions = levelDimensions;
    }

    public List<AdditiveRow> build() {
      return new ArrayList<>(rows.values());
    }

    public RowsBuilder addBaselineRows(final DataFrame baselineDf) {
      addRows(CubeTag.Baseline, baselineDf);
      return this;
    }

    public RowsBuilder addCurrentRows(final DataFrame currentDf) {
      addRows(CubeTag.Current, currentDf);
      return this;
    }

    private void addRows(final CubeTag tag, final DataFrame df) {
      if (df.size() == 0) {
        LOG.warn("Got 0 rows for dimensions: {} for {} timeframe", levelDimensions, tag);
        return;
      }
      if (df.size() == QUERY_LIMIT) {
        LOG.warn(
            "Got {} rows for dimensions: {} for {} timeframe. This corresponds to the LIMIT clause. "
                + "Rows are randomly chosen, dimension analysis algorithm may not return the best results.",
            QUERY_LIMIT,
            levelDimensions,
            tag);
      }

      for (int rowIdx = 0; rowIdx < df.size(); ++rowIdx) {
        double metricValue = df.getDouble(Constants.COL_VALUE, rowIdx);
        // fixme cyril - investigate why null values happen - in the mean time mitigate by replacing by "null"
        List<@NonNull String> dimensionValues = new ArrayList<>();
        for (String dimensionName : levelDimensions) {
          String dimensionValue = df.getString(dimensionName, rowIdx);
          if (dimensionValue == null) {
            LOG.error(
                "Encountered null dimension value for dimension: {}. Should not happen. Mitigating - replacing by \"null\". Level dimensions: {}. Full dataframe: \n{}",
                dimensionName,
                levelDimensions,
                df);
            dimensionValue = "null";
          }
          dimensionValues.add(dimensionValue);
        }
        addOrUpdateRow(dimensionValues, metricValue, tag);
      }
    }

    private void addOrUpdateRow(final List<String> dimensionValues, double metricValue,
        CubeTag tag) {
      if (Double.compare(0d, metricValue) >= 0) {
        LOG.warn("Value not added to rowTable: it is too small. Value: {}. Tag: {}", metricValue, tag);
        return;
      }
      if (Double.isInfinite(metricValue)) {
        LOG.warn("Value not added to rowTable: it is infinite. Value: {}. Tag: {}", metricValue, tag);
        return;
      }
      AdditiveRow row = rows.get(dimensionValues);
      if (row == null) {
        // fixme cyril - due to incorect implementation in AdditiveCubeNode, baseDimensons must be passed rather than levelDimensions
        row = new AdditiveRow(baseDimensions, new DimensionValues(dimensionValues));
        rows.put(dimensionValues, row);
      }
      switch (tag) {
        case Baseline:
          row.setBaselineValue(metricValue);
          break;
        case Current:
          row.setCurrentValue(metricValue);
          break;
        default:
          throw new IllegalArgumentException("Unsupported CubeTag: " + tag.name());
      }
    }
  }
}
