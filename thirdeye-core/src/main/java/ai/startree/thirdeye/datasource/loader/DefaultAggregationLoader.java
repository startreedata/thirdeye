/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.datasource.loader;

import static ai.startree.thirdeye.datasource.calcite.QueryProjection.getFunctionName;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.calcite.CalciteRequest;
import ai.startree.thirdeye.datasource.calcite.QueryProjection;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultAggregationLoader implements AggregationLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAggregationLoader.class);

  private static final long TIMEOUT = 600000;
  private static final String ROLLUP_NAME = "OTHER";

  private final DataSourceCache dataSourceCache;
  private final ExecutorService executorService;

  @Inject
  public DefaultAggregationLoader(final DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
    executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(
        "aggregation-loader-%d").build());
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
    final Map<String, Map<String, Double>> output = new TreeMap<>();

    dataBreakdown = dataBreakdown.dropNull();
    dataAggregate = dataAggregate.dropNull();

    final Map<String, Double> dimensionTotals = new HashMap<>();

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
      final double total = dataAggregate.getDouble(Constants.COL_VALUE, 0);
      for (final Map.Entry<String, Double> entry : dimensionTotals.entrySet()) {
        if (entry.getValue() < total) {
          output.get(entry.getKey()).put(ROLLUP_NAME, total - entry.getValue());
        }
      }
    }

    return output;
  }

  @Override
  public DataFrame loadBreakdown(final MetricSlice slice, final int limit) throws Exception {
    final DatasetConfigDTO datasetConfigDTO = slice.getDatasetConfigDTO();

    final List<String> dimensions = new ArrayList<>(optional(datasetConfigDTO.getDimensions()).map(
        Templatable::value).orElse(List.of()));
    dimensions.removeAll(slice.getPredicates()
        .stream()
        .map(Predicate::getLhs)
        .collect(Collectors.toList()));
    dimensions.remove(datasetConfigDTO.getTimeColumn());

    LOG.info("Querying breakdown '{}' for dimensions '{}'", slice, dimensions);

    final DataFrame dfAll = DataFrame
        .builder(COL_DIMENSION_NAME + ":STRING",
            COL_DIMENSION_VALUE + ":STRING",
            Constants.COL_VALUE + ":DOUBLE")
        .build()
        .setIndex(COL_DIMENSION_NAME, COL_DIMENSION_VALUE);

    final Map<String, Future<DataFrame>> responses = new HashMap<>();

    // submit requests
    for (final String dimension : dimensions) {
      final QueryProjection dimensionProjection = QueryProjection.of(dimension);
      final CalciteRequest request = CalciteRequest.newBuilderFrom(slice)
          .select(dimensionProjection)
          .groupBy(dimensionProjection)
          // ensure multiple runs return the same values when num rows > limit - see te-636
          .orderBy(QueryProjection.of(Constants.COL_VALUE).withDescOrder())
          .limit(limit)
          .build();
      final Future<DataFrame> res = getQueryResultAsync(request,
          datasetConfigDTO.getDataSource());

      responses.put(dimension, res);
    }

    // collect responses
    final List<DataFrame> results = new ArrayList<>();
    for (final String dimension : dimensions) {
      final DataFrame res = responses.get(dimension).get(TIMEOUT, TimeUnit.MILLISECONDS);
      final DataFrame dfResult = new DataFrame()
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
  public Future<DataFrame> loadAggregateAsync(final MetricSlice slice,
      final List<String> dimensions, final int limit) {
    LOG.info("Aggregating '{}'", slice);
    final CalciteRequest.Builder requestBuilder = CalciteRequest
        .newBuilderFrom(slice)
        .limit(limit);
    if (dimensions.isEmpty()) {
      // add this count to help check if there is data in aggregate only queries - some aggregations
      // can return a value even if there is no data see
      // https://docs.pinot.apache.org/users/user-guide-query/supported-aggregations
      // count rows non null
      requestBuilder.select(QueryProjection.of("COUNT",
          List.of(getFunctionName(slice.getMetricConfigDTO()))).withAlias(
          COL_AGGREGATION_ONLY_NON_NULL_ROWS_COUNT));
      // count all rows
      requestBuilder.select(QueryProjection.of("COUNT", List.of("*")).withAlias(
          COL_AGGREGATION_ONLY_ROWS_COUNT));
    }
    for (final String dimension : dimensions) {
      final QueryProjection dimensionProjection = QueryProjection.of(dimension);
      requestBuilder
          .select(dimensionProjection)
          .groupBy(dimensionProjection);
    }
    final String dataSource = slice.getDatasetConfigDTO().getDataSource();
    return getQueryResultAsync(requestBuilder.build(), dataSource);
  }

  private Future<DataFrame> getQueryResultAsync(final CalciteRequest request,
      final String dataSource) {
    return executorService.submit(() -> getQueryResult(request, dataSource));
  }

  public DataFrame getQueryResult(final CalciteRequest request, final String dataSource)
      throws Exception {
    final ThirdEyeDataSource thirdEyeDataSource = dataSourceCache.getDataSource(dataSource);
    final String query = request.getSql(thirdEyeDataSource.getSqlLanguage(),
        thirdEyeDataSource.getSqlExpressionBuilder());
    // table info is only used with legacy Pinot client - should be removed
    final DataSourceRequest requestV2 = new DataSourceRequest(null, query, Map.of());
    return thirdEyeDataSource.fetchDataTable(requestV2).getDataFrame();
  }
}
