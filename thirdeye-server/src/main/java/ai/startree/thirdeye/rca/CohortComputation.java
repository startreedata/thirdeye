/*
 * Copyright 2023 StarTree Inc
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
import static ai.startree.thirdeye.util.CalciteUtils.combinePredicates;
import static ai.startree.thirdeye.util.CalciteUtils.identifierDescOf;
import static ai.startree.thirdeye.util.ResourceUtils.badRequest;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.datasource.calcite.QueryProjection;
import ai.startree.thirdeye.datasource.calcite.SelectQuery;
import ai.startree.thirdeye.datasource.calcite.SelectQueryTranslator;
import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
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
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import ai.startree.thirdeye.spi.metric.DimensionType;
import ai.startree.thirdeye.util.CalciteUtils;
import com.google.inject.Singleton;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.chrono.ISOChronology;

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

  public static Chronology toChronology(final String timezone) {
    return optional(timezone)
        .filter(StringUtils::isNotEmpty)
        .map(DateTimeZone::forID)
        .map(ISOChronology::getInstance)
        .map(e -> (Chronology) e)
        .orElse(Constants.DEFAULT_CHRONOLOGY);
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

  private static QueryPredicate thresholdPredicate(final Double threshold,
      final boolean roundOffThreshold) {
    final String pattern = roundOffThreshold ? "#" : "#.##";
    final String value = new DecimalFormat(pattern).format(threshold);
    final Predicate predicate = Predicate.GE(COL_AGGREGATE, value);

    return QueryPredicate.of(predicate, DimensionType.NUMERIC);
  }

  private CohortComputationContext buildContext(final CohortComputationApi request) {
    final Interval currentInterval = new Interval(
        request.getStart(),
        request.getEnd(),
        toChronology(request.getTimezone()));

    final MetricConfigDTO metric = getMetric(request.getMetric());
    final DatasetConfigDTO dataset = ensureExists(datasetConfigManager.findByName(metric.getDataset())
        .stream()
        .findFirst()
        .orElse(null), "dataset not found. name: " + metric.getDataset());
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataset.getDataSource());

    final CohortComputationContext context = new CohortComputationContext()
        .setMetric(metric)
        .setDataset(dataset)
        .setDataSource(dataSource)
        .setInterval(currentInterval);

    optional(request.getLimit())
        .ifPresent(context::setLimit);

    optional(request.getMaxDepth())
        .ifPresent(context::setMaxDepth);

    optional(request.getWhere())
        .map(where -> parseWhere(where, dataSource))
        .ifPresent(context::setWhere);

    optional(request.getHaving())
        .map(having -> parseHaving(having, dataSource))
        .ifPresent(context::setHaving);

    optional(request.getRoundOffThreshold())
        .ifPresent(context::setRoundOffThreshold);

    final List<String> dimensions = new ArrayList<>(optional(request.getDimensions())
        .orElse(optional(dataset.getDimensions())
            .map(Templatable::value)
            .map(dims -> removeWhereDimensions(dims, context.getWhere()))
            .orElse(List.of())));
    ensure(!dimensions.isEmpty(), "Dimension list is empty");
    context.setAllDimensions(dimensions);

    return context;
  }

  /**
   * remove only from dataset dimensions. Do not filter dimensions manually fed into request
   */
  private List<String> removeWhereDimensions(final List<String> dims, final SqlNode where) {
    if (where == null) {
      return dims;
    }
    final List<String> whereClauseDimensions = where.accept(new SqlBasicVisitor<>() {
      @Override
      public List<String> visit(final SqlCall sqlCall) {
        return sqlCall.getOperandList().stream()
            .map(sqlNode -> sqlNode.accept(this))
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
      }

      @Override
      public List<String> visit(final SqlNodeList nodeList) {
        return nodeList.stream()
            .map(sqlNode -> sqlNode.accept(this))
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
      }

      @Override
      public List<String> visit(final SqlIdentifier identifier) {
        return List.of(identifier.getSimple());
      }
    });

    dims.removeAll(whereClauseDimensions);
    return dims;
  }

  private SqlNode parseWhere(final String where, final ThirdEyeDataSource ds) {
    final ThirdEyeSqlParserConfig teSqlParserConfig = ds.getSqlLanguage()
        .getSqlParserConfig();
    final SqlParser.Config sqlParserConfig = SqlLanguageTranslator.translate(teSqlParserConfig);
    try {
      return SqlParser.create(where, sqlParserConfig).parseExpression();
    } catch (final SqlParseException e) {
      throw badRequest(ThirdEyeStatus.ERR_UNKNOWN, "Failed to parse where clause: " + where);
    }
  }

  private SqlNode parseHaving(final String having, final ThirdEyeDataSource dataSource) {
    final ThirdEyeSqlParserConfig teSqlParserConfig = dataSource.getSqlLanguage()
        .getSqlParserConfig();
    final SqlParser.Config sqlParserConfig = SqlLanguageTranslator.translate(teSqlParserConfig);
    try {
      return SqlParser.create(having, sqlParserConfig).parseExpression();
    } catch (final SqlParseException e) {
      throw badRequest(ThirdEyeStatus.ERR_UNKNOWN, "Failed to parse having clause: " + having);
    }
  }

  private Double computeAggregate(final CohortComputationContext c) throws Exception {
    final DatasetConfigDTO dataset = c.getDataset();
    final SelectQuery builder = new SelectQuery(dataset.getDataset())
        .select(selectable(c.getMetric()))
        .whereTimeFilter(c.getInterval(),
            dataset.getTimeColumn(),
            dataset.getTimeFormat(),
            dataset.getTimeUnit().name());

    optional(c.getWhere())
        .ifPresent(builder::where);

    final SelectQueryTranslator r = builder.build();
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
        .setMaxDepth(context.getMaxDepth())
        .setDimensions(context.getAllDimensions())
        .setWhere(request.getWhere())
        .setHaving(request.getHaving());

    if (context.isRoundOffThreshold()) {
      output.setRoundOffThreshold(true);
    }

    if (request.isGenerateEnumerationItems()) {
      final String key = optional(request.getEnumerationItemParamKey())
          .orElse(K_QUERY_FILTERS_DEFAULT);
      output.setEnumerationItems(results.stream()
          .map(api -> toEnumerationItem(api, key, context))
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
    final SelectQuery builder = new SelectQuery(dataset.getDataset())
        .whereTimeFilter(c.getInterval(),
            dataset.getTimeColumn(),
            dataset.getTimeFormat(),
            dataset.getTimeUnit().name());

    optional(c.getWhere())
        .ifPresent(builder::where);

    final List<SqlIdentifier> subDimensionsIdentifiers = subDimensions.stream()
        .map(CalciteUtils::identifierOf)
        .collect(Collectors.toList());
    subDimensionsIdentifiers.forEach(builder::select);
    builder.select(selectable(c.getMetric()));
    subDimensionsIdentifiers.forEach(builder::groupBy);

    optional(c.getHaving())
        .ifPresent(builder::having);

    final SelectQueryTranslator query = builder
        .having(thresholdPredicate(c.getThreshold(), c.isRoundOffThreshold()))
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
    if (name != null) {
      final List<MetricConfigDTO> byMetricName = metricConfigManager.findByMetricName(name);
      ensure(byMetricName.size() <= 1, String.format("Found %d metrics with the same name. "
          + "Please use id or define the metric manually", byMetricName.size()));
      ensure(byMetricName.size() == 1, "Metric not found. name: " + name);
      return byMetricName.get(0);
    }

    ensureExists(metric.getDataset(), "dataset is a required field");
    ensureExists(metric.getDataset().getName(), "dataset.name is a required field");
    ensureExists(metric.getAggregationColumn(), "aggregationColumn is a required field");
    ensureExists(metric.getAggregationFunction(), "aggregationFunction is a required field");

    return new MetricConfigDTO()
        .setDataset(metric.getDataset().getName())
        .setAggregationColumn(metric.getAggregationColumn())
        .setDefaultAggFunction(metric.getAggregationFunction());
  }

  private EnumerationItemApi toEnumerationItem(final DimensionFilterContributionApi api,
      final String queryFiltersKey,
      final CohortComputationContext context) {
    final String whereFragment = whereFragment(api.getDimensionFilters(), context);
    return new EnumerationItemApi()
        .setName(generateName(api.getDimensionFilters()))
        .setParams(Map.of(queryFiltersKey, whereFragment));
  }

  private String generateName(final Map<String, String> dimensionFilters) {
    return String.join(",", dimensionFilters.values());
  }

  private String whereFragment(final Map<String, String> dimensionFilters,
      final CohortComputationContext context) {

    final List<SqlNode> queryPredicates = dimensionFilters.entrySet()
        .stream()
        .map(e -> Predicate.EQ(e.getKey(), e.getValue()))
        .map(e -> QueryPredicate.of(e, DimensionType.STRING))
        .map(QueryPredicate::toSqlNode)
        .collect(Collectors.toList());

    optional(context.getWhere())
        .ifPresent(queryPredicates::add);

    final SqlNode combinedPredicates = requireNonNull(combinePredicates(queryPredicates));
    final SqlLanguage sqlLanguage = context.getDataSource().getSqlLanguage();
    final SqlDialect sqlDialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());

    return " AND " + combinedPredicates.toSqlString(sqlDialect);
  }

  public DataFrame runQuery(final SelectQueryTranslator selectQueryTranslator,
      final ThirdEyeDataSource ds) {
    final String sql = selectQueryTranslator.getSql(ds.getSqlLanguage(),
        ds.getSqlExpressionBuilder());

    final DataSourceRequest request = new DataSourceRequest(null, sql, Map.of());
    try {
      return ds.fetchDataTable(request).getDataFrame();
    } catch (final Exception e) {
      throw badRequest(ThirdEyeStatus.ERR_UNKNOWN, e.getMessage());
    }
  }
}
