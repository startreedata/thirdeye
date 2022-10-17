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
import static ai.startree.thirdeye.util.CalciteUtils.identifierDescOf;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.calcite.CalciteRequest;
import ai.startree.thirdeye.datasource.calcite.CalciteRequest.Builder;
import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.datasource.calcite.QueryProjection;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.CohortComputationApi;
import ai.startree.thirdeye.spi.api.DimensionFilterContributionApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.MetricApi;
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
import ai.startree.thirdeye.util.CalciteUtils;
import com.google.inject.Singleton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

@Singleton
public class CohortComputation {

  public static final String COL_AGGREGATE = "agg";
  public static final String K_QUERY_FILTERS_DEFAULT = "queryFilters";
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

  public static DateTimeZone toDateTimeZone(final String timezone) {
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

  private static List<DimensionFilterContributionApi> readDf(final DataFrame df,
      final Double aggregate) {
    final Set<String> dimensions = new HashSet<>(df.getSeriesNames());
    dimensions.remove(COL_AGGREGATE);

    final int nColumns = dimensions.size();

    final List<DimensionFilterContributionApi> results = new ArrayList<>();
    for (int i = 0; i < df.size(); ++i) {
      final Map<String, String> dimensionFilters = new HashMap<>(nColumns);
      final double value = df.getDouble(COL_AGGREGATE, i);
      final DimensionFilterContributionApi api = new DimensionFilterContributionApi()
          .setDimensionFilters(dimensionFilters)
          .setValue(value);
      if (aggregate != null) {
        api.setPercentage(trimDouble(100.0 * value / aggregate));
      }
      for (final String seriesName : dimensions) {
        dimensionFilters.put(seriesName, df.getString(seriesName, i));
      }
      results.add(api);
    }
    return results;
  }

  private static double trimDouble(final double v) {
    final DecimalFormat df = new DecimalFormat("#.##");
    return Double.parseDouble(df.format(v));
  }

  private CohortComputationContext buildContext(final CohortComputationApi request) {
    final Interval currentInterval = new Interval(
        request.getStart(),
        request.getEnd(),
        toDateTimeZone(request.getTimezone()));

    final MetricConfigDTO metric = getMetric(request.getMetric());
    final DatasetConfigDTO dataset = ensureExists(datasetConfigManager.findByName(metric.getDataset())
        .stream()
        .findFirst()
        .orElse(null), "dataset not found. name: " + metric.getDataset());
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataset.getDataSource());

    final List<String> dimensions = new ArrayList<>(optional(request.getDimensions())
        .orElse(optional(dataset.getDimensions())
            .map(Templatable::value)
            .orElse(List.of())));
    ensure(!dimensions.isEmpty(), "Dimension list is empty");

    final CohortComputationContext context = new CohortComputationContext()
        .setMetric(metric)
        .setDataset(dataset)
        .setDataSource(dataSource)
        .setInterval(currentInterval)
        .setAllDimensions(dimensions);

    optional(request.getLimit())
        .ifPresent(context::setLimit);

    optional(request.getMaxDepth())
        .ifPresent(context::setMaxDepth);

    return context;
  }

  private Double computeAggregate(final CohortComputationContext c) throws Exception {
    final DatasetConfigDTO dataset = c.getDataset();
    final CalciteRequest r = CalciteRequest.newBuilder(dataset.getDataset())
        .select(selectable(c.getMetric()))
        .whereTimeFilter(c.getInterval(),
            dataset.getTimeColumn(),
            dataset.getTimeFormat(),
            dataset.getTimeUnit().name())
        .build();
    final DataFrame df = runQuery(r, c.getDataSource());
    return df.get(COL_AGGREGATE).getDouble(0);
  }

  public CohortComputationApi compute(final CohortComputationApi request)
      throws Exception {
    optional(request.getMaxDepth())
        .ifPresent(maxDepth -> ensure(maxDepth > 0, "maxDepth must be a positive integer"));

    final CohortComputationContext context = buildContext(request);

    final Double agg = computeAggregate(context);
    final Double threshold = optional(request.getPercentage())
        .map(p -> agg * p / 100.0)
        .orElse(request.getThreshold());

    context
        .setThreshold(threshold)
        .setAggregate(agg);

    final Set<Set<String>> visited = new HashSet<>();
    final var results = compute0(List.of(), visited, context);

    final CohortComputationApi output = new CohortComputationApi()
        .setMetric(ApiBeanMapper.toApi(context.getMetric()))
        .setThreshold(threshold)
        .setPercentage(request.getPercentage())
        .setAggregate(agg)
        .setGenerateEnumerationItems(request.isGenerateEnumerationItems())
        .setResultSize(results.size())
        .setResults(results)
        .setLimit(context.getLimit())
        .setMaxDepth(context.getMaxDepth());

    if (request.isGenerateEnumerationItems()) {
      final String key = optional(request.getEnumerationItemParamKey())
          .orElse(K_QUERY_FILTERS_DEFAULT);
      output.setEnumerationItems(results.stream()
          .map(api -> toEnumerationItem(api, key))
          .collect(Collectors.toList()));
    }
    return output;
  }

  private List<DimensionFilterContributionApi> compute0(
      final List<String> dimensions,
      final Set<Set<String>> visited,
      final CohortComputationContext c)
      throws Exception {
    final List<DimensionFilterContributionApi> results = new ArrayList<>();

    final List<String> dimensionsToExplore = new ArrayList<>(c.getAllDimensions());
    dimensionsToExplore.removeAll(dimensions);

    for (final String dimension : dimensionsToExplore) {
      final List<String> subDimensions = new ArrayList<>(dimensions.size() + 1);
      subDimensions.addAll(dimensions);
      subDimensions.add(dimension);
      final Set<String> subDimensionSet = Set.copyOf(subDimensions);
      if (visited.contains(subDimensionSet)) {
        continue;
      }
      visited.add(subDimensionSet);

      final List<DimensionFilterContributionApi> l = query(subDimensions, c);
      results.addAll(l);
      if (l.size() > 0 && subDimensions.size() < c.getMaxDepth()) {
        results.addAll(compute0(subDimensions, visited, c));
      }
    }
    return results;
  }

  private List<DimensionFilterContributionApi> query(
      final List<String> subDimensions,
      final CohortComputationContext c) throws Exception {
    final DatasetConfigDTO dataset = c.getDataset();
    final Builder builder = CalciteRequest.newBuilder(dataset.getDataset())
        .whereTimeFilter(c.getInterval(),
            dataset.getTimeColumn(),
            dataset.getTimeFormat(),
            dataset.getTimeUnit().name());

    final List<SqlIdentifier> subDimensionsIdentifiers = subDimensions.stream()
        .map(CalciteUtils::identifierOf)
        .collect(Collectors.toList());
    subDimensionsIdentifiers.forEach(builder::select);
    builder.select(selectable(c.getMetric()));
    subDimensionsIdentifiers.forEach(builder::groupBy);

    final Predicate predicate = Predicate.GE(COL_AGGREGATE, String.valueOf(c.getThreshold()));
    final CalciteRequest query = builder
        .having(QueryPredicate.of(predicate, DimensionType.NUMERIC))
        .limit(c.getLimit())
        .orderBy(identifierDescOf(COL_AGGREGATE))
        .build();
    final DataFrame df = runQuery(query, c.getDataSource());
    return readDf(df, c.getAggregate());
  }

  private MetricConfigDTO getMetric(final MetricApi metric) {
    if (metric.getId() != null) {
      return ensureExists(metricConfigManager.findById(metric.getId()),
          "metric not found. id: " + metric.getId());
    }
    final String name = metric.getName();
    ensureExists(name, "metric id or name must be provided.");
    final MetricConfigDTO dto = ensureExists(metricConfigManager.findByMetricName(name)
        .stream()
        .findFirst()
        .orElse(null), "metric not found: name: " + name);
    return dto;
  }

  private EnumerationItemApi toEnumerationItem(final DimensionFilterContributionApi api,
      final String queryFiltersKey) {
    return new EnumerationItemApi()
        .setName(generateName(api.getDimensionFilters()))
        .setParams(Map.of(queryFiltersKey, toPartialQuery(api.getDimensionFilters())));
  }

  private String generateName(final Map<String, String> dimensionFilters) {
    return String.join(",", dimensionFilters.values());
  }

  private String toPartialQuery(final Map<String, String> dimensionFilters) {
    return " AND " + dimensionFilters.entrySet()
        .stream()
        .map(e -> String.format("'%s' = '%s'", e.getKey(), e.getValue()))
        .collect(Collectors.joining(" AND "));
  }

  public DataFrame runQuery(final CalciteRequest calciteRequest, final ThirdEyeDataSource ds)
      throws Exception {
    final String sql = calciteRequest.getSql(ds.getSqlLanguage(), ds.getSqlExpressionBuilder());

    final DataSourceRequest request = new DataSourceRequest(null, sql, Map.of());
    return ds.fetchDataTable(request).getDataFrame();
  }
}
