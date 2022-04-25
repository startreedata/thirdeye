package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.util.CalciteUtils.addAlias;
import static ai.startree.thirdeye.util.CalciteUtils.combinePredicates;
import static ai.startree.thirdeye.util.CalciteUtils.expressionToNode;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.nodeToQuery;
import static ai.startree.thirdeye.util.CalciteUtils.numericLiteralOf;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.calcite.sql.SqlAsOperator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * Class that helps build and generate SQL queries.
 *
 * Use the builder {@link #newBuilder}.
 *
 * todo cyril - later implement having clause
 **/
public class CalciteRequest {

  public static final String TIME_AGGREGATION_ALIAS = "teTimeGroup";

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

  // ORDER BY clause
  final private List<QueryProjection> orderByProjections;
  final private List<String> freeTextOrderByProjections;
  final private List<SqlNode> sqlNodeOrderByProjections;

  // LIMIT clause
  final private Long limit;

  private CalciteRequest(Builder builder) {
    checkArgument(
        builder.selectProjections.size() > 0 || builder.freeTextSelectProjections.size() > 0
            || builder.slqNodeSelectProjections.size() > 0
            || builder.timeAggregationGranularity != null,
        "Projection (selection) lists are empty. Invalid SQL request.");
    this.selectProjections = List.copyOf(builder.selectProjections);
    this.freeTextSelectProjections = List.copyOf(builder.freeTextSelectProjections);
    this.sqlNodeSelectProjections = List.copyOf(builder.slqNodeSelectProjections);
    this.timeAggregationGranularity = builder.timeAggregationGranularity;
    this.timeAggregationColumnFormat = builder.timeAggregationColumnFormat;
    this.timeAggregationColumnUnit = builder.timeAggregationColumnUnit;
    this.timeAggregationColumn = builder.timeAggregationColumn;
    this.timeAggregationOrderBy = builder.timeAggregationOrderBy;
    this.database = builder.database;
    this.table = builder.table;

    this.timeFilterInterval = builder.timeFilterInterval;
    this.timeFilterColumn = builder.timeFilterColumn;
    this.timeFilterColumnFormat = builder.timeFilterColumnFormat;
    this.timeFilterColumnUnit = builder.timeFilterColumnUnit;

    this.predicates = List.copyOf(builder.predicates);
    this.freeTextPredicates = List.copyOf(builder.freeTextPredicates);
    this.sqlNodePredicates = List.copyOf(builder.sqlNodePredicates);

    this.groupByProjections = List.copyOf(builder.groupByProjections);
    this.freeTextGroupByProjections = List.copyOf(builder.freeTextGroupByProjections);
    this.sqlNodeGroupByProjections = List.copyOf(builder.sqlNodeGroupByProjections);

    this.orderByProjections = List.copyOf(builder.orderByProjections);
    this.freeTextOrderByProjections = List.copyOf(builder.freeTextOrderByProjections);
    this.sqlNodeOrderByProjections = List.copyOf(builder.sqlNodeOrderByProjections);

    this.limit = builder.limit;
  }

  public static Builder newBuilder(final String database, final String table) {
    return new Builder(database, table);
  }

  public String getSql(final SqlLanguage sqlLanguage, final SqlExpressionBuilder expressionBuilder)
      throws SqlParseException {
    SqlParser.Config sqlParserConfig = SqlLanguageTranslator.translate(sqlLanguage.getSqlParserConfig());
    SqlDialect sqlDialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());

    SqlNode sqlNode = getSqlNode(sqlParserConfig, expressionBuilder);
    return nodeToQuery(sqlNode, sqlDialect);
  }

  protected SqlNode getSqlNode(SqlParser.Config sqlParserConfig,
      SqlExpressionBuilder expressionBuilder)
      throws SqlParseException {

    return new SqlSelect(
        SqlParserPos.ZERO,
        null,
        getSelectList(sqlParserConfig, expressionBuilder),
        getFrom(),
        getWhere(sqlParserConfig, expressionBuilder),
        getGroupBy(sqlParserConfig, expressionBuilder),
        null,
        null,
        getOrderBy(sqlParserConfig, expressionBuilder),
        null,
        getFetch(),
        null
    );
  }

  private SqlNodeList getSelectList(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder)
      throws SqlParseException {
    List<SqlNode> selectIdentifiers = mergeProjectionsLists(sqlParserConfig, expressionBuilder,
        selectProjections, freeTextSelectProjections, sqlNodeSelectProjections
    );
    // add time aggregation projection
    if (timeAggregationGranularity != null) {
      String timeGroupExpression = expressionBuilder.getTimeGroupExpression(
          timeAggregationColumn,
          timeAggregationColumnFormat,
          timeAggregationGranularity,
          timeAggregationColumnUnit);
      SqlNode timeGroupNode = expressionToNode(timeGroupExpression, sqlParserConfig);
      List<SqlNode> aliasOperands = List.of(timeGroupNode, identifierOf(TIME_AGGREGATION_ALIAS));
      SqlNode timeGroupWithAlias = new SqlBasicCall(new SqlAsOperator(),
          aliasOperands.toArray(new SqlNode[0]),
          SqlParserPos.ZERO);
      selectIdentifiers.add(timeGroupWithAlias);
    }

    return SqlNodeList.of(SqlParserPos.ZERO, selectIdentifiers);
  }

  private SqlNode getFrom() {
    List<String> identifiers = List.of(database, table);
    return new SqlIdentifier(identifiers, SqlParserPos.ZERO);
  }

  private SqlNode getWhere(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) throws SqlParseException {
    List<SqlNode> predicates = new ArrayList<>();
    if (timeFilterInterval != null) {
      // use the alias of the aggregated time column - in this case it's sure time is in epoch millis
      boolean isAggregatedTimeColumn =
          timeAggregationGranularity != null && timeAggregationColumn.equals(timeFilterColumn);
      // todo cyril remove the objects.requireNonNull with builder pattern
      String timeFilterExpression = expressionBuilder.getTimeFilterExpression(
          isAggregatedTimeColumn ? TIME_AGGREGATION_ALIAS : timeFilterColumn,
          timeFilterInterval.getStartMillis(),
          timeFilterInterval.getEndMillis(),
          isAggregatedTimeColumn ? null : timeFilterColumnFormat,
          isAggregatedTimeColumn ? null : timeFilterColumnUnit);
      SqlNode timeGroupNode = expressionToNode(timeFilterExpression, sqlParserConfig);
      predicates.add(timeGroupNode);
    }
    this.predicates.stream().map(QueryPredicate::toSqlNode).forEach(predicates::add);
    for (String freeTextPredicate : freeTextPredicates) {
      // replacement below to make it easy to use the same default templatesProperty for dataFetcher and custom rca where clause
      String cleanedFreePredicate = freeTextPredicate.replaceFirst("^ *[aA][nN][dD] +", "");
      predicates.add(expressionToNode(cleanedFreePredicate, sqlParserConfig));
    }
    predicates.addAll(sqlNodePredicates);

    return combinePredicates(predicates);
  }

  private SqlNodeList getGroupBy(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) throws SqlParseException {
    List<SqlNode> groupIdentifiers = mergeProjectionsLists(sqlParserConfig, expressionBuilder,
        groupByProjections, freeTextGroupByProjections, sqlNodeGroupByProjections
    );
    if (timeAggregationGranularity != null) {
      groupIdentifiers.add(identifierOf(TIME_AGGREGATION_ALIAS));
    }

    return groupIdentifiers.isEmpty() ? null : SqlNodeList.of(SqlParserPos.ZERO, groupIdentifiers);
  }

  private SqlNodeList getOrderBy(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) throws SqlParseException {
    List<SqlNode> orderIdentifiers = mergeProjectionsLists(sqlParserConfig, expressionBuilder,
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
      final List<String> freeTextProjections, final List<SqlNode> sqlNodeProjections)
      throws SqlParseException {
    List<SqlNode> nodes = new ArrayList<>();
    for (QueryProjection queryProjection : queryProjections) {
      nodes.add(queryProjection.toDialectSpecificSqlNode(sqlParserConfig,
          expressionBuilder));
    }
    // add free text group by columns
    for (String freeTextProjection : freeTextProjections) {
      SqlNode nodeWithAlias = prepareWithAlias(sqlParserConfig, freeTextProjection);
      nodes.add(nodeWithAlias);
    }
    nodes.addAll(sqlNodeProjections);

    return nodes;
  }

  private static SqlNode prepareWithAlias(final Config sqlParserConfig,
      final String freeTextProjection)
      throws SqlParseException {
    String[] expressionAndAlias = freeTextProjection.split(" [aA][sS] ");
    if (expressionAndAlias.length == 1) {
      // no alias
      return expressionToNode(freeTextProjection, sqlParserConfig);
    }
    // manage the case in which there is multiple AS - only the last AS is used as alias
    String expressionWithoutLastAlias = Arrays.stream(expressionAndAlias)
        .limit(expressionAndAlias.length - 1)
        .collect(Collectors.joining(" AS "));
    SqlNode expressionNode = expressionToNode(expressionWithoutLastAlias, sqlParserConfig);
    String alias = expressionAndAlias[expressionAndAlias.length - 1];
    return addAlias(expressionNode, alias);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CalciteRequest that = (CalciteRequest) o;
    return timeAggregationOrderBy == that.timeAggregationOrderBy && Objects.equals(
        selectProjections,
        that.selectProjections) && Objects.equals(freeTextSelectProjections,
        that.freeTextSelectProjections) && Objects.equals(sqlNodeSelectProjections,
        that.sqlNodeSelectProjections) && Objects.equals(timeAggregationGranularity,
        that.timeAggregationGranularity) && Objects.equals(timeAggregationColumnFormat,
        that.timeAggregationColumnFormat) && Objects.equals(timeAggregationColumnUnit,
        that.timeAggregationColumnUnit) && Objects.equals(timeAggregationColumn,
        that.timeAggregationColumn) && Objects.equals(database, that.database)
        && Objects.equals(table, that.table) && Objects.equals(timeFilterInterval,
        that.timeFilterInterval) && Objects.equals(timeFilterColumn, that.timeFilterColumn)
        && Objects.equals(timeFilterColumnFormat, that.timeFilterColumnFormat)
        && Objects.equals(timeFilterColumnUnit, that.timeFilterColumnUnit)
        && Objects.equals(predicates, that.predicates) && Objects.equals(
        freeTextPredicates,
        that.freeTextPredicates) && Objects.equals(sqlNodePredicates,
        that.sqlNodePredicates) && Objects.equals(groupByProjections,
        that.groupByProjections) && Objects.equals(freeTextGroupByProjections,
        that.freeTextGroupByProjections) && Objects.equals(sqlNodeGroupByProjections,
        that.sqlNodeGroupByProjections) && Objects.equals(orderByProjections,
        that.orderByProjections) && Objects.equals(freeTextOrderByProjections,
        that.freeTextOrderByProjections) && Objects.equals(sqlNodeOrderByProjections,
        that.sqlNodeOrderByProjections) && Objects.equals(limit, that.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(selectProjections,
        freeTextSelectProjections,
        sqlNodeSelectProjections,
        timeAggregationGranularity,
        timeAggregationColumnFormat,
        timeAggregationColumnUnit,
        timeAggregationColumn,
        timeAggregationOrderBy,
        database,
        table,
        timeFilterInterval,
        timeFilterColumn,
        timeFilterColumnFormat,
        timeFilterColumnUnit,
        predicates,
        freeTextPredicates,
        sqlNodePredicates,
        groupByProjections,
        freeTextGroupByProjections,
        sqlNodeGroupByProjections,
        orderByProjections,
        freeTextOrderByProjections,
        sqlNodeOrderByProjections,
        limit);
  }

  @Override
  public String toString() {
    return "CalciteRequest{" +
        "selectProjections=" + selectProjections +
        ", freeTextSelectProjections=" + freeTextSelectProjections +
        ", sqlNodeSelectProjections=" + sqlNodeSelectProjections +
        ", timeAggregationGranularity=" + timeAggregationGranularity +
        ", timeAggregationColumnFormat='" + timeAggregationColumnFormat + '\'' +
        ", timeAggregationColumnUnit='" + timeAggregationColumnUnit + '\'' +
        ", timeAggregationColumn='" + timeAggregationColumn + '\'' +
        ", timeAggregationOrderBy=" + timeAggregationOrderBy +
        ", database='" + database + '\'' +
        ", table='" + table + '\'' +
        ", timeFilterInterval=" + timeFilterInterval +
        ", timeFilterColumn='" + timeFilterColumn + '\'' +
        ", timeFilterColumnFormat='" + timeFilterColumnFormat + '\'' +
        ", timeFilterColumnUnit='" + timeFilterColumnUnit + '\'' +
        ", predicates=" + predicates +
        ", freeTextPredicates=" + freeTextPredicates +
        ", sqlNodePredicates=" + sqlNodePredicates +
        ", groupByProjections=" + groupByProjections +
        ", freeTextGroupByProjections=" + freeTextGroupByProjections +
        ", sqlNodeGroupByProjections=" + sqlNodeGroupByProjections +
        ", orderByProjections=" + orderByProjections +
        ", freeTextOrderByProjections=" + freeTextOrderByProjections +
        ", sqlNodeOrderByProjections=" + sqlNodeOrderByProjections +
        ", limit=" + limit +
        '}';
  }

  public static class Builder {

    final private List<QueryProjection> selectProjections = new ArrayList<>();
    final private List<String> freeTextSelectProjections = new ArrayList<>();
    final private List<SqlNode> slqNodeSelectProjections = new ArrayList<>();

    private final String database;
    private final String table;

    private Period timeAggregationGranularity = null;
    private String timeAggregationColumnFormat = null;
    private String timeAggregationColumn = null;
    private String timeAggregationColumnUnit = null;
    private boolean timeAggregationOrderBy = false;

    private Interval timeFilterInterval = null;
    private String timeFilterColumn = null;
    private String timeFilterColumnFormat = null;
    private String timeFilterColumnUnit;

    final private List<QueryPredicate> predicates = new ArrayList<>();
    final private List<String> freeTextPredicates = new ArrayList<>();
    final private List<SqlNode> sqlNodePredicates = new ArrayList<>();

    final private List<QueryProjection> groupByProjections = new ArrayList<>();
    final private List<String> freeTextGroupByProjections = new ArrayList<>();
    final private List<SqlNode> sqlNodeGroupByProjections = new ArrayList<>();

    final private List<QueryProjection> orderByProjections = new ArrayList<>();
    final private List<String> freeTextOrderByProjections = new ArrayList<>();
    final private List<SqlNode> sqlNodeOrderByProjections = new ArrayList<>();

    private Long limit;

    public Builder(final String database, final String table) {
      this.database = Objects.requireNonNull(database);
      this.table = Objects.requireNonNull(table);
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
    public Builder withTimeAggregation(final Period timeAggregationGranularity,
        final String timeAggregationColumn,
        final String timeAggregationColumnFormat,
        @Nullable final String timeAggregationColumnUnit,
        final boolean timeAggregationOrderBy) {
      this.timeAggregationGranularity = Objects.requireNonNull(timeAggregationGranularity);
      this.timeAggregationColumn = Objects.requireNonNull(timeAggregationColumn);
      this.timeAggregationColumnFormat = Objects.requireNonNull(timeAggregationColumnFormat);
      this.timeAggregationColumnUnit = timeAggregationColumnUnit;
      this.timeAggregationOrderBy = timeAggregationOrderBy;
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
    public Builder withTimeFilter(final Interval timeFilterInterval, final String timeFilterColumn,
        final String timeFilterColumnFormat, @Nullable final String timeFilterColumnUnit) {
      this.timeFilterInterval = Objects.requireNonNull(timeFilterInterval);
      this.timeFilterColumn = Objects.requireNonNull(timeFilterColumn);
      this.timeFilterColumnFormat = Objects.requireNonNull(timeFilterColumnFormat);
      this.timeFilterColumnUnit = timeFilterColumnUnit;

      return this;
    }

    public Builder addSelectProjection(final QueryProjection projection) {
      this.selectProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    public Builder addSelectProjection(final String textProjection) {
      checkArgument(isNotBlank(textProjection));
      this.freeTextSelectProjections.add(textProjection);
      return this;
    }

    public Builder addSelectProjection(final SqlNode sqlNodeProjection) {
      this.slqNodeSelectProjections.add(Objects.requireNonNull(sqlNodeProjection));
      return this;
    }

    /**
     * Add a predicate. Predicates are combined with the AND operator.
     */
    public Builder addPredicate(final QueryPredicate predicate) {
      this.predicates.add(Objects.requireNonNull(predicate));
      return this;
    }

    /**
     * Add a free text predicate. Prefix AND or OR will be removed.
     * Predicates are combined with the AND operator.
     */
    public Builder addPredicate(final String predicates) {
      checkArgument(isNotBlank(predicates));
      this.freeTextPredicates.add(Objects.requireNonNull(predicates));
      return this;
    }

    /**
     * Add a SqlNode predicate.
     * Predicates are combined with the AND operator.
     */
    public Builder addPredicate(final SqlNode sqlPredicate) {
      this.sqlNodePredicates.add(Objects.requireNonNull(sqlPredicate));
      return this;
    }

    /**
     * Add a group by projection.
     * GroupBy projections are NOT automatically added to the select projections.
     */
    public Builder addGroupByProjection(final QueryProjection projection) {
      this.groupByProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    /**
     * Add a free text group by projection.
     * GroupBy projections are NOT automatically added to the select projections.
     */
    public Builder addGroupByProjection(final String projection) {
      checkArgument(isNotBlank(projection));
      this.freeTextGroupByProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    /**
     * Add a SqlNode group by projection.
     * GroupBy projections are NOT automatically added to the select projections.
     */
    public Builder addGroupByProjection(final SqlNode projection) {
      this.sqlNodeGroupByProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    public Builder addOrderByProjection(final QueryProjection projection) {
      this.orderByProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    public Builder addOrderByProjection(final String projection) {
      checkArgument(isNotBlank(projection));
      this.freeTextOrderByProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    public Builder addOrderByProjection(final SqlNode projection) {
      this.sqlNodeOrderByProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    public Builder withLimit(final long limit) {
      checkArgument(limit > 0);
      this.limit = limit;
      return this;
    }

    public CalciteRequest build() {
      return new CalciteRequest(this);
    }
  }
}
