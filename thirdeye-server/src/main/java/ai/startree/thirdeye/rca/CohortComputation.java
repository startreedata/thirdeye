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

package ai.startree.thirdeye.rca;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.calcite.CalciteRequest;
import ai.startree.thirdeye.datasource.calcite.CalciteRequest.Builder;
import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.datasource.calcite.QueryProjection;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.BreakdownApi;
import ai.startree.thirdeye.spi.api.DimensionFilterContributionApi;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.metric.DimensionType;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

@Singleton
public class CohortComputation {

  public static final String COL_AGGREGATE = "agg";
  private final DataSourceCache dataSourceCache;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;

  @Inject
  public CohortComputation(final DataSourceCache dataSourceCache,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    this.dataSourceCache = dataSourceCache;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
  }

  public static DateTimeZone getDateTimeZone(final String timezone) {
    return optional(timezone)
        .filter(StringUtils::isNotEmpty)
        .map(DateTimeZone::forID)
        .orElse(Constants.DEFAULT_TIMEZONE);
  }

  private static QueryProjection selectable(final MetricConfigDTO metric) {
    final String aggregationColumn = optional(metric.getAggregationColumn()).orElse(metric.getName());
    return QueryProjection
        .of(metric.getDefaultAggFunction(), List.of(aggregationColumn))
        .withAlias(COL_AGGREGATE);
  }

  private static CalciteRequest calciteRequest(final DatasetConfigDTO dataset,
      final MetricConfigDTO metric,
      final List<String> dimensions, final Interval currentInterval, final Double threshold) {
    final Builder builder = CalciteRequest.newBuilder(dataset.getDataset())
        .withTimeFilter(currentInterval,
            dataset.getTimeColumn(),
            dataset.getTimeFormat(),
            dataset.getTimeUnit().name());

    dimensions.forEach(builder::addSelectProjection);
    builder.addSelectProjection(selectable(metric));
    dimensions.forEach(builder::addGroupByProjection);

    final Predicate predicate = Predicate.GE(COL_AGGREGATE, String.valueOf(threshold));
    return builder
        .having(QueryPredicate.of(predicate, DimensionType.NUMERIC))
        .withLimit(100000).build();
  }

  private static List<DimensionFilterContributionApi> readDf(final DataFrame df) {
    final Set<String> dimensions = new HashSet<>(df.getSeriesNames());
    dimensions.remove(COL_AGGREGATE);

    final int nColumns = dimensions.size();

    final List<DimensionFilterContributionApi> results = new ArrayList<>();
    for (int i = 0; i < df.size(); ++i) {
      final Map<String, String> dimensionFilters = new HashMap<>(nColumns);
      final DimensionFilterContributionApi api = new DimensionFilterContributionApi()
          .setDimensionFilters(dimensionFilters)
          .setValue(df.getDouble(COL_AGGREGATE, i));
      for (String seriesName : dimensions) {
        dimensionFilters.put(seriesName, df.getString(seriesName, i));
      }
      results.add(api);
    }
    return results;
  }

  public BreakdownApi computeBreakdown(final BreakdownApi request)
      throws Exception {
    final Interval currentInterval = new Interval(
        request.getStart(),
        request.getEnd(),
        getDateTimeZone(request.getTimezone()));

    final DatasetConfigDTO dataset = datasetConfigManager.findById(request.getDataset().getId());
    final MetricConfigDTO metric = metricConfigManager.findById(request.getMetric().getId());
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataset.getDataSource());

    final Double agg = computeAggregate(metric, dataset, currentInterval, dataSource);

    final Double threshold = optional(request.getPercentage())
        .map(p -> agg * p / 100.0)
        .orElse(request.getThreshold());

    final List<String> dimensions = new ArrayList<>(optional(dataset.getDimensions())
        .map(Templatable::value)
        .orElse(List.of()));

    final Set<Set<String>> visited = new HashSet<>();
    final List<DimensionFilterContributionApi> results1 = computeBreakdown0(
        dataset,
        metric,
        List.of(),
        dimensions,
        threshold,
        visited,
        currentInterval,
        dataSource);

    return new BreakdownApi()
        .setThreshold(threshold)
        .setAggregate(agg)
        .setResultSize(results1.size())
        .setResults(results1);
  }

  private Double computeAggregate(final MetricConfigDTO metric, final DatasetConfigDTO dataset,
      final Interval currentInterval, final ThirdEyeDataSource dataSource)
      throws Exception {
    final CalciteRequest r = CalciteRequest.newBuilder(dataset.getDataset())
        .addSelectProjection(selectable(metric))
        .withTimeFilter(currentInterval,
            dataset.getTimeColumn(),
            dataset.getTimeFormat(),
            dataset.getTimeUnit().name())
        .build();
    final DataFrame df = query(r, dataSource);
    return df.get(COL_AGGREGATE).getDouble(0);
  }

  private List<DimensionFilterContributionApi> computeBreakdown0(
      final DatasetConfigDTO dataset,
      final MetricConfigDTO metric,
      final List<String> dimensions,
      final List<String> allDimensions,
      final Double threshold,
      final Set<Set<String>> visited,
      final Interval currentInterval,
      final ThirdEyeDataSource dataSource)
      throws Exception {
    final Set<String> dimensionsSet = Set.copyOf(dimensions);
    if (visited.contains(dimensionsSet)) {
      throw new RuntimeException("Invalid code path! Should always explore new pathways");
    }
    visited.add(dimensionsSet);

    final List<DimensionFilterContributionApi> results = new ArrayList<>();

    final List<String> dimensionsToExplore = new ArrayList<>(allDimensions);
    dimensionsToExplore.removeAll(dimensions);

    for (String dimension : dimensionsToExplore) {
      final List<String> subDimensions = new ArrayList<>(dimensions);
      subDimensions.add(dimension);
      if (visited.contains(Set.copyOf(subDimensions))) {
        continue;
      }

      final CalciteRequest query = calciteRequest(dataset,
          metric,
          subDimensions,
          currentInterval,
          threshold);
      final var l = executeQuery(query, dataSource);
      results.addAll(l);
      if (l.size() > 0) {
        results.addAll(computeBreakdown0(dataset,
            metric,
            subDimensions,
            dimensionsToExplore,
            threshold,
            visited,
            currentInterval,
            dataSource));
      }
    }
    return results;
  }

  private List<DimensionFilterContributionApi> executeQuery(final CalciteRequest calciteRequest,
      final ThirdEyeDataSource dataSource)
      throws Exception {
    final DataFrame df = query(calciteRequest, dataSource);
    return readDf(df);
  }

  public DataFrame query(final CalciteRequest calciteRequest, final ThirdEyeDataSource ds)
      throws Exception {
    final String sql = calciteRequest.getSql(ds.getSqlLanguage(), ds.getSqlExpressionBuilder());

    final DataSourceRequest request = new DataSourceRequest(null, sql, Map.of());
    return ds.fetchDataTable(request).getDataFrame();
  }
}
