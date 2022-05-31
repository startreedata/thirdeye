/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.loader;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.calcite.CalciteRequest;
import ai.startree.thirdeye.datasource.calcite.QueryProjection;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
    final DatasetConfigDTO datasetConfigDTO = slice.getDatasetConfigDTO();

    List<String> dimensions = new ArrayList<>(datasetConfigDTO.getDimensions());
    dimensions.removeAll(slice.getPredicates()
        .stream()
        .map(Predicate::getLhs)
        .collect(Collectors.toList()));
    dimensions.remove(datasetConfigDTO.getTimeColumn());

    LOG.info("Querying breakdown '{}' for dimensions '{}'", slice, dimensions);

    DataFrame dfAll = DataFrame
        .builder(COL_DIMENSION_NAME + ":STRING",
            COL_DIMENSION_VALUE + ":STRING",
            Constants.COL_VALUE + ":DOUBLE")
        .build()
        .setIndex(COL_DIMENSION_NAME, COL_DIMENSION_VALUE);

    Map<String, Future<DataFrame>> responses = new HashMap<>();

    // submit requests
    for (String dimension : dimensions) {
      final QueryProjection dimensionProjection = QueryProjection.of(dimension);
      final CalciteRequest request = CalciteRequest
          .newBuilderFrom(slice)
          .addSelectProjection(dimensionProjection)
          .addGroupByProjection(dimensionProjection)
          // ensure multiple runs return the same values when num rows > limit - see te-636
          .addOrderByProjection(QueryProjection.of(Constants.COL_VALUE).withDescOrder())
          .withLimit(limit).build();
      Future<DataFrame> res = dataSourceCache.getQueryResultAsync(request,
          datasetConfigDTO.getDataSource());

      responses.put(dimension, res);
    }

    // collect responses
    List<DataFrame> results = new ArrayList<>();
    for (String dimension : dimensions) {
      DataFrame res = responses.get(dimension).get(TIMEOUT, TimeUnit.MILLISECONDS);
      DataFrame dfResult = new DataFrame()
          .addSeries(COL_DIMENSION_NAME, StringSeries.fillValues(res.size(), dimension))
          .addSeries(COL_DIMENSION_VALUE, res.get(dimension))
          .addSeries(Constants.COL_VALUE, res.get(Constants.COL_VALUE));
      results.add(dfResult);
    }

    final DataFrame breakdown = dfAll.append(results);
    // add time column containing start time of slice
    return breakdown
        .addSeries(Constants.COL_TIME,
            LongSeries.fillValues(breakdown.size(), slice.getInterval().getStartMillis()))
        .setIndex(Constants.COL_TIME, COL_DIMENSION_NAME, COL_DIMENSION_VALUE);
  }

  @Override
  public DataFrame loadAggregate(MetricSlice slice, List<String> dimensions, int limit)
      throws Exception {
    LOG.info("Aggregating '{}'", slice);
    final CalciteRequest.Builder requestBuilder = CalciteRequest
        .newBuilderFrom(slice)
        .withLimit(limit);
    for (String dimension : dimensions) {
      final QueryProjection dimensionProjection = QueryProjection.of(dimension);
      requestBuilder
          .addSelectProjection(dimensionProjection)
          .addGroupByProjection(dimensionProjection);
    }
    final DataFrame res = dataSourceCache.getQueryResult(requestBuilder.build(),
        slice.getDatasetConfigDTO().getDataSource());

    if (res.size() == 0) {
      return emptyDataframe(dimensions);
    }

    // fill in timestamps
    return res
        .addSeries(Constants.COL_TIME,
            LongSeries.fillValues(res.size(), slice.getInterval().getStartMillis()))
        .setIndex(Constants.COL_TIME);
  }

  private DataFrame emptyDataframe(final List<String> dimensions) {
    List<String> cols = new ArrayList<>();
    cols.add(Constants.COL_TIME + ":LONG");
    dimensions.forEach(dimName -> cols.add(dimName + ":STRING"));
    cols.add(Constants.COL_VALUE + ":DOUBLE");

    List<String> indexes = new ArrayList<>();
    indexes.add(Constants.COL_TIME);
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
      final double value = dataBreakdown.getDouble(Constants.COL_VALUE, i);

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
      double total = dataAggregate.getDouble(Constants.COL_VALUE, 0);
      for (Map.Entry<String, Double> entry : dimensionTotals.entrySet()) {
        if (entry.getValue() < total) {
          output.get(entry.getKey()).put(ROLLUP_NAME, total - entry.getValue());
        }
      }
    }

    return output;
  }
}
