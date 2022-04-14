/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.loader;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.util.DataFrameUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAggregationLoader implements AggregationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAggregationLoader.class);

  private static final String COL_DIMENSION_NAME = "dimName";
  private static final String COL_DIMENSION_VALUE = "dimValue";
  private static final long TIMEOUT = 600000;
  private static final String ROLLUP_NAME = "OTHER";

  private final DataSourceCache dataSourceCache;

  @Inject
  public DefaultAggregationLoader(final DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
  }

  @Override
  public DataFrame loadBreakdown(MetricSlice slice, int limit) throws Exception {
    DatasetConfigDTO datasetConfigDTO = slice.getDatasetConfigDTO();

    List<String> dimensions = new ArrayList<>(datasetConfigDTO.getDimensions());
    dimensions.removeAll(slice.getFilters().keySet());
    dimensions.remove(datasetConfigDTO.getTimeColumn());

    LOG.info("De-Aggregating '{}' for dimensions '{}'", slice, dimensions);

    DataFrame dfAll = DataFrame
        .builder(COL_DIMENSION_NAME + ":STRING", COL_DIMENSION_VALUE + ":STRING",
            DataFrame.COL_VALUE + ":DOUBLE").build()
        .setIndex(COL_DIMENSION_NAME, COL_DIMENSION_VALUE);

    Map<String, ThirdEyeRequest> requests = new HashMap<>();
    Map<String, Future<ThirdEyeResponse>> responses = new HashMap<>();

    // submit requests
    for (String dimension : dimensions) {
      ThirdEyeRequest thirdEyeRequest = DataFrameUtils.makeAggregateRequest(slice, Collections.singletonList(dimension), limit, "ref");
      Future<ThirdEyeResponse> res = dataSourceCache.getQueryResultAsync(thirdEyeRequest);

      requests.put(dimension, thirdEyeRequest);
      responses.put(dimension, res);
    }

    // collect responses
    List<DataFrame> results = new ArrayList<>();
    for (String dimension : dimensions) {
      ThirdEyeRequest thirdEyeRequest = requests.get(dimension);
      ThirdEyeResponse res = responses.get(dimension)
          .get(TIMEOUT, TimeUnit.MILLISECONDS);
      DataFrame dfRaw = DataFrameUtils.evaluateResponse(res, thirdEyeRequest.getMetricFunctions().get(0));
      DataFrame dfResult = new DataFrame()
          .addSeries(COL_DIMENSION_NAME, StringSeries.fillValues(dfRaw.size(), dimension))
          .addSeries(COL_DIMENSION_VALUE, dfRaw.get(dimension))
          .addSeries(DataFrame.COL_VALUE, dfRaw.get(DataFrame.COL_VALUE));
      results.add(dfResult);
    }

    final DataFrame breakdown = dfAll.append(results);
    // add time column containing start time of slice
    return breakdown
        .addSeries(DataFrame.COL_TIME,
            LongSeries.fillValues(breakdown.size(), slice.getStartMillis()))
        .setIndex(DataFrame.COL_TIME, COL_DIMENSION_NAME, COL_DIMENSION_VALUE);
  }

  @Override
  public DataFrame loadAggregate(MetricSlice slice, List<String> dimensions, int limit)
      throws Exception {
    LOG.info("Aggregating '{}'", slice);
    ThirdEyeRequest thirdEyeRequest = DataFrameUtils.makeAggregateRequest(slice,
        new ArrayList<>(dimensions),
        limit,
        "ref");
    ThirdEyeResponse res = dataSourceCache.getQueryResult(thirdEyeRequest);
    if (res.getNumRows() == 0) {
      return emptyDataframe(dimensions);
    }
    final DataFrame aggregate = DataFrameUtils.evaluateResponse(res, thirdEyeRequest.getMetricFunctions().get(0));

    // fill in timestamps
    return aggregate
        .addSeries(DataFrame.COL_TIME,
            LongSeries.fillValues(aggregate.size(), slice.getStartMillis()))
        .setIndex(DataFrame.COL_TIME);
  }

  private DataFrame emptyDataframe(final List<String> dimensions) {
    List<String> cols = new ArrayList<>();
    cols.add(DataFrame.COL_TIME + ":LONG");
    dimensions.forEach(dimName -> cols.add(dimName + ":STRING"));
    cols.add(DataFrame.COL_VALUE + ":DOUBLE");

    List<String> indexes = new ArrayList<>();
    indexes.add(DataFrame.COL_TIME);
    indexes.addAll(dimensions);
    return DataFrame.builder(cols).build().setIndex(indexes);
  }

  /**
   * Returns a map of maps (keyed by dimension name, keyed by dimension value) derived from the
   * breakdown results dataframe.
   *
   * @param dataBreakdown (transformed) breakdown query results
   * @param dataAggregate (transformed) aggregate query results
   * @return map of maps of value (keyed by dimension name, keyed by dimension value)
   */
  public static Map<String, Map<String, Double>> makeBreakdownMap(DataFrame dataBreakdown,
      DataFrame dataAggregate) {
    Map<String, Map<String, Double>> output = new TreeMap<>();

    dataBreakdown = dataBreakdown.dropNull();
    dataAggregate = dataAggregate.dropNull();

    Map<String, Double> dimensionTotals = new HashMap<>();

    for (int i = 0; i < dataBreakdown.size(); i++) {
      final String dimName = dataBreakdown.getString(COL_DIMENSION_NAME, i);
      final String dimValue = dataBreakdown.getString(COL_DIMENSION_VALUE, i);
      final double value = dataBreakdown.getDouble(DataFrame.COL_VALUE, i);

      // cell
      if (!output.containsKey(dimName)) {
        output.put(dimName, new HashMap<>());
      }
      output.get(dimName).put(dimValue, value);

      // total
      dimensionTotals.put(dimName, MapUtils.getDoubleValue(dimensionTotals, dimName, 0) + value);
    }

    // add rollup column
    if (!dataAggregate.isEmpty()) {
      double total = dataAggregate.getDouble(DataFrame.COL_VALUE, 0);
      for (Map.Entry<String, Double> entry : dimensionTotals.entrySet()) {
        if (entry.getValue() < total) {
          output.get(entry.getKey()).put(ROLLUP_NAME, total - entry.getValue());
        }
      }
    }

    return output;
  }
}
