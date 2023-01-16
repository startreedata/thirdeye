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
package ai.startree.thirdeye.datasource.calcite;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.metric.DimensionType;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import java.util.ArrayList;
import java.util.List;
import org.apache.calcite.sql.SqlNode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;
import org.joda.time.Period;

public class SelectQuery {

  final List<QueryProjection> selectProjections = new ArrayList<>();
  final List<String> freeTextSelectProjections = new ArrayList<>();
  final List<SqlNode> slqNodeSelectProjections = new ArrayList<>();

  String database = null;
  final String table;

  Period timeAggregationGranularity = null;
  String timeAggregationColumnFormat = null;
  String timeAggregationColumn = null;
  String timeAggregationColumnUnit = null;
  boolean timeAggregationOrderBy = false;
  String timeAggregationTimezone = null;

  Interval timeFilterInterval = null;
  String timeFilterColumn = null;
  String timeFilterColumnFormat = null;
  String timeFilterColumnUnit;

  final List<QueryPredicate> predicates = new ArrayList<>();
  final List<String> freeTextPredicates = new ArrayList<>();
  final List<SqlNode> sqlNodePredicates = new ArrayList<>();

  final List<QueryProjection> groupByProjections = new ArrayList<>();
  final List<String> freeTextGroupByProjections = new ArrayList<>();
  final List<SqlNode> sqlNodeGroupByProjections = new ArrayList<>();

  final List<QueryPredicate> havingPredicates = new ArrayList<>();
  final List<SqlNode> havingSqlNodePredicates = new ArrayList<>();

  final List<QueryProjection> orderByProjections = new ArrayList<>();
  final List<String> freeTextOrderByProjections = new ArrayList<>();
  final List<SqlNode> sqlNodeOrderByProjections = new ArrayList<>();

  Long limit;

  public SelectQuery(final String table) {
    this.table = requireNonNull(table);
  }

  /**
   * Generates a query for a metric. The metric is aliased as {@link  Constants}.COL_VALUE.
   */
  public static SelectQuery from(final MetricSlice slice) {
    DatasetConfigDTO datasetConfigDTO = slice.getDatasetConfigDTO();
    MetricConfigDTO metricConfigDTO = slice.getMetricConfigDTO();
    final SelectQuery builder = new SelectQuery(datasetConfigDTO.getDataset())
        .whereTimeFilter(slice.getInterval(),
            datasetConfigDTO.getTimeColumn(),
            datasetConfigDTO.getTimeFormat(),
            datasetConfigDTO.getTimeUnit().name())
        .select(QueryProjection.fromMetricConfig(metricConfigDTO)
            .withAlias(Constants.COL_VALUE));
    if (isNotBlank(metricConfigDTO.getWhere())) {
      builder.where(metricConfigDTO.getWhere());
    }
    for (Predicate predicate : slice.getPredicates()) {
      builder.where(QueryPredicate.of(predicate, DimensionType.STRING));
    }

    return builder;
  }

  public SelectQuery withDatabase(final String database) {
    this.database = database;
    return this;
  }

  /**
   * Add a timeAggregation.
   *
   * Add a projection on a time column with bucketing of the given period.
   * Add group by on the buckets.
   * The alias of the time bucket projection is {@value TIME_AGGREGATION_ALIAS}.
   *
   * At SQL generation time, the bucketing sql expression is provided by the Datasource {@link
   * SqlExpressionBuilder}.
   */
  public SelectQuery withTimeAggregation(final Period timeAggregationGranularity,
      final String timeAggregationColumn,
      final String timeAggregationColumnFormat,
      final @Nullable String timeAggregationColumnUnit,
      final boolean timeAggregationOrderBy,
      final @Nullable String timeAggregationTimezone) {
    this.timeAggregationGranularity = requireNonNull(timeAggregationGranularity);
    this.timeAggregationColumn = requireNonNull(timeAggregationColumn);
    this.timeAggregationColumnFormat = requireNonNull(timeAggregationColumnFormat);
    this.timeAggregationColumnUnit = timeAggregationColumnUnit;
    this.timeAggregationOrderBy = timeAggregationOrderBy;
    this.timeAggregationTimezone = timeAggregationTimezone;
    return this;
  }

  public SelectQuery select(final QueryProjection projection) {
    this.selectProjections.add(requireNonNull(projection));
    return this;
  }

  public SelectQuery select(final String textProjection) {
    checkArgument(isNotBlank(textProjection));
    this.freeTextSelectProjections.add(textProjection);
    return this;
  }

  public SelectQuery select(final SqlNode sqlNodeProjection) {
    this.slqNodeSelectProjections.add(requireNonNull(sqlNodeProjection));
    return this;
  }

  /**
   * Add a predicate. Predicates are combined with the AND operator.
   */
  public SelectQuery where(final QueryPredicate predicate) {
    this.predicates.add(requireNonNull(predicate));
    return this;
  }

  /**
   * Add a free text predicate. Prefix AND or OR will be removed.
   * Predicates are combined with the AND operator.
   */
  public SelectQuery where(final String predicates) {
    checkArgument(isNotBlank(predicates));
    this.freeTextPredicates.add(requireNonNull(predicates));
    return this;
  }

  /**
   * Add a SqlNode predicate.
   * Predicates are combined with the AND operator.
   */
  public SelectQuery where(final SqlNode sqlPredicate) {
    this.sqlNodePredicates.add(requireNonNull(sqlPredicate));
    return this;
  }

  /**
   * Add a timeFilter
   *
   * Add a predicate on a time column.
   * If {@link #withTimeAggregation}, and the time column is the same, then the filter
   * will be applied to the bucketed time.
   *
   * At SQL generation time, the filtering sql expression is provided by the Datasource {@link
   * SqlExpressionBuilder}.
   */
  public SelectQuery whereTimeFilter(final Interval timeFilterInterval,
      final String timeFilterColumn,
      final String timeFilterColumnFormat,
      @Nullable final String timeFilterColumnUnit) {
    this.timeFilterInterval = requireNonNull(timeFilterInterval);
    this.timeFilterColumn = requireNonNull(timeFilterColumn);
    this.timeFilterColumnFormat = requireNonNull(timeFilterColumnFormat);
    this.timeFilterColumnUnit = timeFilterColumnUnit;

    return this;
  }

  /**
   * Add a group by projection.
   * GroupBy projections are NOT automatically added to the select projections.
   */
  public SelectQuery groupBy(final QueryProjection projection) {
    this.groupByProjections.add(requireNonNull(projection));
    return this;
  }

  /**
   * Add a free text group by projection.
   * GroupBy projections are NOT automatically added to the select projections.
   */
  public SelectQuery groupBy(final String projection) {
    checkArgument(isNotBlank(projection));
    this.freeTextGroupByProjections.add(requireNonNull(projection));
    return this;
  }

  /**
   * Add a SqlNode group by projection.
   * GroupBy projections are NOT automatically added to the select projections.
   */
  public SelectQuery groupBy(final SqlNode projection) {
    this.sqlNodeGroupByProjections.add(requireNonNull(projection));
    return this;
  }

  public SelectQuery orderBy(final QueryProjection projection) {
    this.orderByProjections.add(requireNonNull(projection));
    return this;
  }

  public SelectQuery orderBy(final String projection) {
    checkArgument(isNotBlank(projection));
    this.freeTextOrderByProjections.add(requireNonNull(projection));
    return this;
  }

  public SelectQuery orderBy(final SqlNode projection) {
    this.sqlNodeOrderByProjections.add(requireNonNull(projection));
    return this;
  }

  public SelectQuery having(final QueryPredicate predicate) {
    havingPredicates.add(requireNonNull(predicate));
    return this;
  }

  public SelectQuery having(final SqlNode sqlNode) {
    havingSqlNodePredicates.add(requireNonNull(sqlNode));
    return this;
  }

  public SelectQuery limit(final long limit) {
    checkArgument(limit > 0);
    this.limit = limit;
    return this;
  }

  public SelectQueryTranslator build() {
    return new SelectQueryTranslator(this);
  }
}
