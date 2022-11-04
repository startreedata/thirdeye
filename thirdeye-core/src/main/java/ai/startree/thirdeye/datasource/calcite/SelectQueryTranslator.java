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
package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.util.CalciteUtils.addAlias;
import static ai.startree.thirdeye.util.CalciteUtils.combinePredicates;
import static ai.startree.thirdeye.util.CalciteUtils.expressionToNode;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.nodeToQuery;
import static ai.startree.thirdeye.util.CalciteUtils.numericLiteralOf;
import static ai.startree.thirdeye.util.CalciteUtils.quoteIdentifierIfReserved;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * Class that helps build and generate SQL queries.
 * Use the builder {@link #newBuilder}.
 **/
public class SelectQueryTranslator {

  public static final String TIME_AGGREGATION_ALIAS = "teTimeGroup";
  public static final boolean QUOTE_IDENTIFIERS = true;

  // SELECT clause
  final private List<QueryProjection> selectProjections;
  final private List<String> freeTextSelectProjections;
  final private List<SqlNode> sqlNodeSelectProjections;

  // aggregation - in SELECT, GROUP BY, ORDER BY?
  final private Period timeAggregationGranularity;
  final private String timeAggregationColumnFormat;
  final private String timeAggregationColumnUnit;
  final private String timeAggregationColumn;
  /**
   * If set, is the timeAggregation will be the first order by column.
   */
  final private boolean timeAggregationOrderBy;
  final private String timeAggregationTimezone;

  // FROM clause
  final private String database;
  final private String table;

  // time filter in WHERE clause
  final private Interval timeFilterInterval;
  final private String timeFilterColumn;
  final private String timeFilterColumnFormat;
  final private String timeFilterColumnUnit;
  // todo cyril add a partitionTimeFilterColumn with a period granularity - for partition constraint - to be used by Presto/BQ

  // WHERE clause
  final private List<QueryPredicate> predicates;
  final private List<String> freeTextPredicates;
  final private List<SqlNode> sqlNodePredicates;

  // GROUP BY clause
  final private List<QueryProjection> groupByProjections;
  final private List<String> freeTextGroupByProjections;
  final private List<SqlNode> sqlNodeGroupByProjections;

  // WHERE clause
  final private List<QueryPredicate> havingPredicates;

  // ORDER BY clause
  final private List<QueryProjection> orderByProjections;
  final private List<String> freeTextOrderByProjections;
  final private List<SqlNode> sqlNodeOrderByProjections;

  // LIMIT clause
  final private Long limit;

  SelectQueryTranslator(final SelectQuery selectQuery) {
    checkArgument(selectQuery.selectProjections.size() > 0
            || selectQuery.freeTextSelectProjections.size() > 0
            || selectQuery.slqNodeSelectProjections.size() > 0
            || selectQuery.timeAggregationGranularity != null,
        "Projection (selection) lists are empty. Invalid SQL request.");

    selectProjections = List.copyOf(selectQuery.selectProjections);
    freeTextSelectProjections = List.copyOf(selectQuery.freeTextSelectProjections);
    sqlNodeSelectProjections = List.copyOf(selectQuery.slqNodeSelectProjections);
    timeAggregationGranularity = selectQuery.timeAggregationGranularity;
    timeAggregationColumnFormat = selectQuery.timeAggregationColumnFormat;
    timeAggregationColumnUnit = selectQuery.timeAggregationColumnUnit;
    timeAggregationColumn = selectQuery.timeAggregationColumn;
    timeAggregationOrderBy = selectQuery.timeAggregationOrderBy;
    timeAggregationTimezone = selectQuery.timeAggregationTimezone;
    database = selectQuery.database;
    table = selectQuery.table;

    timeFilterInterval = selectQuery.timeFilterInterval;
    timeFilterColumn = selectQuery.timeFilterColumn;
    timeFilterColumnFormat = selectQuery.timeFilterColumnFormat;
    timeFilterColumnUnit = selectQuery.timeFilterColumnUnit;

    predicates = List.copyOf(selectQuery.predicates);
    freeTextPredicates = List.copyOf(selectQuery.freeTextPredicates);
    sqlNodePredicates = List.copyOf(selectQuery.sqlNodePredicates);

    groupByProjections = List.copyOf(selectQuery.groupByProjections);
    freeTextGroupByProjections = List.copyOf(selectQuery.freeTextGroupByProjections);
    sqlNodeGroupByProjections = List.copyOf(selectQuery.sqlNodeGroupByProjections);

    havingPredicates = List.copyOf(selectQuery.havingPredicates);

    orderByProjections = List.copyOf(selectQuery.orderByProjections);
    freeTextOrderByProjections = List.copyOf(selectQuery.freeTextOrderByProjections);
    sqlNodeOrderByProjections = List.copyOf(selectQuery.sqlNodeOrderByProjections);

    limit = selectQuery.limit;
  }

  public String getSql(final SqlLanguage sqlLanguage,
      final SqlExpressionBuilder expressionBuilder) {
    final SqlParser.Config sqlParserConfig = SqlLanguageTranslator.translate(sqlLanguage.getSqlParserConfig());
    final SqlDialect sqlDialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());

    final SqlNode sqlNode = getSqlNode(sqlParserConfig, expressionBuilder, sqlDialect);
    return nodeToQuery(sqlNode, sqlDialect, QUOTE_IDENTIFIERS);
  }

  protected SqlNode getSqlNode(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder, final SqlDialect dialect) {

    return new SqlSelect(
        SqlParserPos.ZERO,
        null,
        getSelectList(sqlParserConfig, expressionBuilder, dialect),
        getFrom(),
        getWhere(sqlParserConfig, expressionBuilder, dialect),
        getGroupBy(sqlParserConfig, expressionBuilder),
        having(),
        null,
        getOrderBy(sqlParserConfig, expressionBuilder),
        null,
        getFetch(),
        null
    );
  }

  private SqlNodeList getSelectList(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder, final SqlDialect dialect) {
    final List<SqlNode> selectIdentifiers = mergeProjectionsLists(sqlParserConfig, expressionBuilder,
        selectProjections, freeTextSelectProjections, sqlNodeSelectProjections
    );
    // add time aggregation projection
    if (timeAggregationGranularity != null) {
      final String timeGroupExpression = expressionBuilder.getTimeGroupExpression(
          quoteIdentifierIfReserved(timeAggregationColumn, sqlParserConfig, dialect),
          timeAggregationColumnFormat,
          timeAggregationGranularity,
          timeAggregationColumnUnit,
          timeAggregationTimezone);
      final SqlNode timeGroupNode = expressionToNode(timeGroupExpression, sqlParserConfig);
      final SqlNode timeGroupWithAlias = addAlias(timeGroupNode, TIME_AGGREGATION_ALIAS);
      selectIdentifiers.add(timeGroupWithAlias);
    }

    return SqlNodeList.of(SqlParserPos.ZERO, selectIdentifiers);
  }

  private SqlNode getFrom() {
    final List<String> identifiers = database != null ? List.of(database, table) : List.of(table);
    return new SqlIdentifier(identifiers, SqlParserPos.ZERO);
  }

  private SqlNode getWhere(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder,
      final SqlDialect dialect) {
    final List<SqlNode> predicates = new ArrayList<>();
    if (timeFilterInterval != null) {
      final String preparedTimeColumn = quoteIdentifierIfReserved(timeFilterColumn,
          sqlParserConfig,
          dialect);
      final String timeFilterExpression = expressionBuilder.getTimeFilterExpression(
          preparedTimeColumn,
          timeFilterInterval,
          timeFilterColumnFormat,
          timeFilterColumnUnit);
      final SqlNode timeGroupNode = expressionToNode(timeFilterExpression, sqlParserConfig);
      predicates.add(timeGroupNode);
    }
    this.predicates.stream().map(QueryPredicate::toSqlNode).forEach(predicates::add);
    for (final String freeTextPredicate : freeTextPredicates) {
      // replacement below to make it easy to use the same default templatesProperty for dataFetcher and custom rca where clause
      final String cleanedFreePredicate = cleanFreeTextPredicate(freeTextPredicate);
      predicates.add(expressionToNode(cleanedFreePredicate, sqlParserConfig));
    }
    predicates.addAll(sqlNodePredicates);

    return combinePredicates(predicates);
  }

  private SqlNode having() {
    final List<SqlNode> predicates = new ArrayList<>();
    havingPredicates.stream().map(QueryPredicate::toSqlNode).forEach(predicates::add);
    return combinePredicates(predicates);
  }

  @NonNull
  public static String cleanFreeTextPredicate(final String freeTextPredicate) {
    return freeTextPredicate.replaceFirst("^ *[aA][nN][dD] +", "");
  }

  private SqlNodeList getGroupBy(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) {
    final List<SqlNode> groupIdentifiers = mergeProjectionsLists(sqlParserConfig, expressionBuilder,
        groupByProjections, freeTextGroupByProjections, sqlNodeGroupByProjections
    );
    if (timeAggregationGranularity != null) {
      groupIdentifiers.add(identifierOf(TIME_AGGREGATION_ALIAS));
    }

    return groupIdentifiers.isEmpty() ? null : SqlNodeList.of(SqlParserPos.ZERO, groupIdentifiers);
  }

  private SqlNodeList getOrderBy(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) {
    final List<SqlNode> orderIdentifiers = mergeProjectionsLists(sqlParserConfig, expressionBuilder,
        orderByProjections, freeTextOrderByProjections, sqlNodeOrderByProjections);
    // add the alias of the timeAggregation if needed
    if (timeAggregationOrderBy) {
      orderIdentifiers.add(identifierOf(TIME_AGGREGATION_ALIAS));
    }
    return orderIdentifiers.isEmpty() ? null : SqlNodeList.of(SqlParserPos.ZERO, orderIdentifiers);
  }

  private SqlNode getFetch() {
    return limit == null ? null : numericLiteralOf(limit.toString());
  }

  private static List<SqlNode> mergeProjectionsLists(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder, final List<QueryProjection> queryProjections,
      final List<String> freeTextProjections, final List<SqlNode> sqlNodeProjections) {
    final List<SqlNode> nodes = new ArrayList<>();
    for (final QueryProjection queryProjection : queryProjections) {
      nodes.add(queryProjection.toDialectSpecificSqlNode(sqlParserConfig,
          expressionBuilder));
    }
    // add free text group by columns
    for (final String freeTextProjection : freeTextProjections) {
      final SqlNode nodeWithAlias = prepareWithAlias(sqlParserConfig, freeTextProjection);
      nodes.add(nodeWithAlias);
    }
    nodes.addAll(sqlNodeProjections);

    return nodes;
  }

  private static SqlNode prepareWithAlias(final Config sqlParserConfig,
      final String freeTextProjection) {
    final String[] expressionAndAlias = freeTextProjection.split(" [aA][sS] ");
    if (expressionAndAlias.length == 1) {
      // no alias
      return expressionToNode(freeTextProjection, sqlParserConfig);
    }
    // manage the case in which there is multiple AS - only the last AS is used as alias
    final String expressionWithoutLastAlias = Arrays.stream(expressionAndAlias)
        .limit(expressionAndAlias.length - 1)
        .collect(Collectors.joining(" AS "));
    final SqlNode expressionNode = expressionToNode(expressionWithoutLastAlias, sqlParserConfig);
    final String alias = expressionAndAlias[expressionAndAlias.length - 1];
    return addAlias(expressionNode, alias);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("selectProjections", selectProjections)
        .add("freeTextSelectProjections", freeTextSelectProjections)
        .add("sqlNodeSelectProjections", sqlNodeSelectProjections)
        .add("timeAggregationGranularity", timeAggregationGranularity)
        .add("timeAggregationColumnFormat", timeAggregationColumnFormat)
        .add("timeAggregationColumnUnit", timeAggregationColumnUnit)
        .add("timeAggregationColumn", timeAggregationColumn)
        .add("timeAggregationOrderBy", timeAggregationOrderBy)
        .add("timeAggregationTimezone", timeAggregationTimezone)
        .add("database", database)
        .add("table", table)
        .add("timeFilterInterval", timeFilterInterval)
        .add("timeFilterColumn", timeFilterColumn)
        .add("timeFilterColumnFormat", timeFilterColumnFormat)
        .add("timeFilterColumnUnit", timeFilterColumnUnit)
        .add("predicates", predicates)
        .add("freeTextPredicates", freeTextPredicates)
        .add("sqlNodePredicates", sqlNodePredicates)
        .add("groupByProjections", groupByProjections)
        .add("freeTextGroupByProjections", freeTextGroupByProjections)
        .add("sqlNodeGroupByProjections", sqlNodeGroupByProjections)
        .add("havingPredicates", havingPredicates)
        .add("orderByProjections", orderByProjections)
        .add("freeTextOrderByProjections", freeTextOrderByProjections)
        .add("sqlNodeOrderByProjections", sqlNodeOrderByProjections)
        .add("limit", limit)
        .toString();
  }
}
