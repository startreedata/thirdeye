package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.util.CalciteUtils.combinePredicates;
import static ai.startree.thirdeye.util.CalciteUtils.expressionToNode;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.nodeToQuery;
import static ai.startree.thirdeye.util.CalciteUtils.numericLiteralOf;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.calcite.sql.SqlAsOperator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * Multiple metrics, single table.
 **/
public class CalciteRequest {

  public static final String TIME_AGGREGATION_ALIAS = "teTimeGroup";

  private static final Long DEFAULT_LIMIT = 100000L;

  // SELECT clause
  final private List<QueryProjection> selectProjections;
  final private List<String> freeTextSelectProjections;

  // Used in SELECT, GROUP BY, ORDER BY?
  final private Period timeAggregationGranularity;
  final private String timeAggregationColumnFormat;
  final private String timeAggregationColumn;
  /**
   * If set, is the timeAggregation will be the first order by column.
   */
  final private boolean timeAggregationOrderBy;

  // FROM clause
  final private String database;
  final private String table;

  // WHERE clause
  final private Interval timeFilterInterval;
  final private String timeFilterColumn;
  final private String timeFilterColumnFormat;
  // todo cyril add a partitionTimeFilterColumn with a period granularity - for partition constraint - to be used by Presto/BQ
  final private List<QueryPredicate> predicates;
  final private String freeTextPredicates;

  // GROUP BY clause
  final private List<QueryProjection> groupByProjections;
  final private List<String> freeTextGroupByProjections;

  // ORDER BY clause
  final private List<QueryProjection> orderByProjections;
  final private List<String> freeTextOrderByProjections;

  // LIMIT clause
  final private Long limit;

  private CalciteRequest(Builder builder) {
    checkArgument(
        builder.selectProjections.size() > 0 || builder.freeTextSelectProjections.size() > 0,
        "Projection (selection) lists are empty. Invalid SQL request.");
    this.selectProjections = builder.selectProjections;
    this.freeTextSelectProjections = List.copyOf(builder.freeTextSelectProjections);
    this.timeAggregationGranularity = builder.timeAggregationGranularity;
    this.timeAggregationColumnFormat = builder.timeAggregationColumnFormat;
    this.timeAggregationColumn = builder.timeAggregationColumn;
    this.timeAggregationOrderBy = builder.timeAggregationOrderBy;
    this.database = builder.database;
    this.table = builder.table;

    this.timeFilterInterval = builder.timeFilterInterval;
    this.timeFilterColumn = builder.timeFilterColumn;
    this.timeFilterColumnFormat = builder.timeFilterColumnFormat;

    this.predicates = builder.predicates;
    this.freeTextPredicates = builder.freeTextPredicates;

    this.groupByProjections = builder.groupByProjections;
    this.freeTextGroupByProjections = builder.freeTextGroupByProjection;

    this.orderByProjections = builder.orderByProjections;
    this.freeTextOrderByProjections = builder.freeTextOrderByProjections;

    this.limit = builder.limit;
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
    List<SqlNode> nodes = new ArrayList<>();
    // add time aggregation projection
    if (timeAggregationGranularity != null) {
      String timeGroupExpression = expressionBuilder.getTimeGroupExpression(
          Objects.requireNonNull(timeAggregationColumn),
          Objects.requireNonNull(timeAggregationColumnFormat),
          timeAggregationGranularity);
      SqlNode timeGroupNode = expressionToNode(timeGroupExpression, sqlParserConfig);
      List<SqlNode> aliasOperands = List.of(timeGroupNode, identifierOf(TIME_AGGREGATION_ALIAS));
      SqlNode timeGroupWithAlias = new SqlBasicCall(new SqlAsOperator(),
          aliasOperands.toArray(new SqlNode[0]),
          SqlParserPos.ZERO);
      nodes.add(timeGroupWithAlias);
    }
    // add structured projection columns - most of the time metrics
    for (QueryProjection selectProjection : selectProjections) {
      nodes.add(selectProjection.toDialectSpecificSqlNode(sqlParserConfig, expressionBuilder));
    }
    // add freeText projections: can be anything complex
    for (String freeTextSelect : freeTextSelectProjections) {
      if (StringUtils.isNotBlank(freeTextSelect)) {
        nodes.add(expressionToNode(freeTextSelect, sqlParserConfig));
      }
    }
    // add structured group by columns - most of the time dimensions
    for (QueryProjection groupByProjection : groupByProjections) {
      nodes.add(groupByProjection.toDialectSpecificSqlNode(sqlParserConfig, expressionBuilder));
    }
    // add free text group by columns
    for (String freeTextGroupBy : freeTextGroupByProjections) {
      if (StringUtils.isNotBlank(freeTextGroupBy)) {
        nodes.add(expressionToNode(freeTextGroupBy, sqlParserConfig));
      }
    }

    return SqlNodeList.of(SqlParserPos.ZERO, nodes);
  }

  private SqlNode getFrom() {
    List<String> identifiers = List.of(database, table);
    return new SqlIdentifier(identifiers, SqlParserPos.ZERO);
  }

  private SqlNode getWhere(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) throws SqlParseException {
    List<SqlNode> predicates = new ArrayList<>();
    if (timeFilterInterval != null) {
      Objects.requireNonNull(timeFilterColumn); // todo cyril ensure this at builder time
      // use the alias of the aggregated time column - in this case it's sure time is in epoch millis
      boolean isAggregatedTimeColumn =
          timeAggregationGranularity != null && timeAggregationColumn.equals(timeFilterColumn);
      // todo cyril remove the objects.requireNonNull with builder pattern
      String timeFilterExpression = expressionBuilder.getTimeFilterExpression(
          isAggregatedTimeColumn ? TIME_AGGREGATION_ALIAS
              : Objects.requireNonNull(timeFilterColumn),
          timeFilterInterval.getStartMillis(),
          timeFilterInterval.getEndMillis(),
          isAggregatedTimeColumn ? null : Objects.requireNonNull(timeFilterColumnFormat));
      SqlNode timeGroupNode = expressionToNode(timeFilterExpression, sqlParserConfig);
      predicates.add(timeGroupNode);
    }
    this.predicates.stream().map(QueryPredicate::toSqlNode).forEach(predicates::add);
    if (StringUtils.isNotBlank(freeTextPredicates)) {
      String cleanedFreePredicate = freeTextPredicates.replaceFirst("^ *[aA][nN][dD] +", "");
      predicates.add(expressionToNode(cleanedFreePredicate, sqlParserConfig));
    }

    return combinePredicates(predicates);
  }

  private SqlNodeList getGroupBy(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) throws SqlParseException {
    List<SqlNode> groupIdentifiers = new ArrayList<>();
    if (timeAggregationGranularity != null) {
      groupIdentifiers.add(identifierOf(TIME_AGGREGATION_ALIAS));
    }

    // add structured group by columns - most of the time dimensions
    for (QueryProjection groupByProjection : groupByProjections) {
      groupIdentifiers.add(groupByProjection.toDialectSpecificSqlNode(sqlParserConfig,
          expressionBuilder));
    }
    // add free text group by columns
    for (String freeTextGroupBy : freeTextGroupByProjections) {
      if (StringUtils.isNotBlank(freeTextGroupBy)) {
        groupIdentifiers.add(expressionToNode(freeTextGroupBy, sqlParserConfig));
      }
    }

    return groupIdentifiers.isEmpty() ? null : SqlNodeList.of(SqlParserPos.ZERO, groupIdentifiers);
  }

  private SqlNodeList getOrderBy(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) throws SqlParseException {
    List<SqlNode> orderIdentifiers = new ArrayList<>();
    // add the alias of the timeAggregation if needed
    if (timeAggregationOrderBy) {
      orderIdentifiers.add(identifierOf(TIME_AGGREGATION_ALIAS));
    }
    // add structured order by columns
    for (QueryProjection orderByProjection : orderByProjections) {
      orderIdentifiers.add(orderByProjection.toDialectSpecificSqlNode(sqlParserConfig,
          expressionBuilder));
    }
    // add free text order by columns
    for (String freeTextOrderBy : freeTextOrderByProjections) {
      if (StringUtils.isNotBlank(freeTextOrderBy)) {
        orderIdentifiers.add(expressionToNode(freeTextOrderBy, sqlParserConfig));
      }
    }
    return orderIdentifiers.isEmpty() ? null : SqlNodeList.of(SqlParserPos.ZERO, orderIdentifiers);
  }

  private SqlNode getFetch() {
    return limit == null ? null : numericLiteralOf(limit.toString());
  }

  public static class Builder {

    final private List<QueryProjection> selectProjections = new ArrayList<>();
    final private List<String> freeTextSelectProjections = new ArrayList<>();

    private String database;
    private String table;

    private Period timeAggregationGranularity = null;
    private String timeAggregationColumnFormat = null;
    private String timeAggregationColumn = null;
    private boolean timeAggregationOrderBy = false;

    private Interval timeFilterInterval = null;
    private String timeFilterColumn = null;
    private String timeFilterColumnFormat = null;

    final private List<QueryPredicate> predicates = new ArrayList<>();
    private String freeTextPredicates;

    final private List<QueryProjection> groupByProjections = new ArrayList<>();
    final private List<String> freeTextGroupByProjection = new ArrayList<>();

    final private List<QueryProjection> orderByProjections = new ArrayList();
    final private List<String> freeTextOrderByProjections = new ArrayList();

    private Long limit;

    public Builder(final String database, final String table) {
      this.database = Objects.requireNonNull(database);
      this.table = Objects.requireNonNull(table);
    }

    public Builder withTimeAggregation(final Period timeAggregationGranularity,
        final String timeAggregationColumn,
        final String timeAggregationColumnFormat,
        final boolean timeAggregationOrderBy) {
      this.timeAggregationGranularity = Objects.requireNonNull(timeAggregationGranularity);
      this.timeAggregationColumn = Objects.requireNonNull(timeAggregationColumn);
      this.timeAggregationColumnFormat = Objects.requireNonNull(timeAggregationColumnFormat);
      this.timeAggregationOrderBy = timeAggregationOrderBy;
      return this;
    }

    public Builder withTimeFilter(final Interval timeFilterInterval, final String timeFilterColumn,
        final String timeFilterColumnFormat) {
      this.timeFilterInterval = Objects.requireNonNull(timeFilterInterval);
      this.timeFilterColumn = Objects.requireNonNull(timeFilterColumn);
      this.timeFilterColumnFormat = Objects.requireNonNull(timeFilterColumnFormat);

      return this;
    }

    public Builder addSelectProjection(final QueryProjection projection) {
      this.selectProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    public Builder addFreeTextSelectProjection(final String freeTextProjection) {
      this.freeTextSelectProjections.add(Objects.requireNonNull(freeTextProjection));
      return this;
    }

    public Builder addPredicate(final QueryPredicate predicate) {
      this.predicates.add(Objects.requireNonNull(predicate));
      return this;
    }

    public Builder withFreeTextPredicates(final String predicates) {
      this.freeTextPredicates = Objects.requireNonNull(predicates);
      return this;
    }

    public Builder addGroupByProjection(final QueryProjection projection) {
      this.groupByProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    public Builder addFreeTextGroupByProjection(final String projection) {
      this.freeTextGroupByProjection.add(projection);
      return this;
    }

    public Builder addOrderByProjection(final QueryProjection projection) {
      this.orderByProjections.add(Objects.requireNonNull(projection));
      return this;
    }

    public Builder addFreeTextOrderByProjection(final String projection) {
      this.freeTextGroupByProjection.add(projection);
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
