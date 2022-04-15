package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.CalciteUtils.combinePredicates;
import static ai.startree.thirdeye.util.CalciteUtils.expressionToNode;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.nodeToQuery;
import static ai.startree.thirdeye.util.CalciteUtils.numericLiteralOf;
import static ai.startree.thirdeye.util.CalciteUtils.stringLiteralOf;
import static ai.startree.thirdeye.util.CalciteUtils.toCalcitePredicate;

import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.detection.v2.TimeseriesFilter;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.spi.util.SpiUtils;
import ai.startree.thirdeye.util.CalciteUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.calcite.sql.SqlAsOperator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlUnresolvedFunction;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Multiple metrics, single table.
 **/
// fixme need to implement more operator compatibility in filters engine only = and != implemented
// fixme cyril need to decide when a metric is numeric and when it's not --> it's implemented in the SQLUtils
// understand this considers the filters are parsed as IN correctly: not sure if it's the case for both heatmap and dim analysis
public class CalciteRequest {

  // todo cyril remove that
  private static final String PERCENTILE_TDIGEST_PREFIX = "percentileTDigest";

  public static final String TIME_AGGREGATION_ALIAS = "teTimeGroup";

  private static final Long DEFAULT_LIMIT = 100000L;

  // SELECT clause
  final private List<StructuredSqlStatement> structuredSqlProjections;
  final private List<String> freeTextProjections;

  // Used in SELECT, GROUP BY, ORDER BY?
  final private Period timeAggregationGranularity;
  final private String timeAggregationColumnFormat;
  final private String timeAggregationColumn;

  // FROM clause
  final private String database;
  final private String table;

  // WHERE clause
  final private Interval timeFilterInterval;
  final private String timeFilterColumn;
  // todo cyril add a partitionTimeFilterColumn with a period granularity - for partition constraint - important in Presto/BQ
  final private List<TimeseriesFilter> structuredPredicates;
  final private String freeTextPredicates;

  // GROUP BY clause
  // todo cyril use StructuredSqlStatement - function could be used
  final private List<String> groupByColumns;

  // ORDER BY clause
  final private List<String> orderByColumns;

  // LIMIT clause
  final private Long limit;

  // partitioning column for optimization: get from datasetConfigDTO
  // todo add aggregationFunction to string in expressionBuilder --> for percentileTdigest for instance
  // todo cyril work on this - put timespec to time formatters in the SqlExpressionBuilder interface
  //private TimeSpec dataTimeSpec;

  public CalciteRequest(
      final List<StructuredSqlStatement> structuredSqlProjections,
      final List<String> freeTextProjections, final Period timeAggregationGranularity,
      final String timeAggregationColumnFormat, final String timeAggregationColumn,
      final String database, final String table, final Interval timeFilterInterval,
      final String timeFilterColumn,
      final List<TimeseriesFilter> structuredPredicates, final String freeTextPredicates,
      final List<String> groupByColumns, final List<String> orderByColumns,
      final Long limit) {

    this.structuredSqlProjections = structuredSqlProjections;
    this.freeTextProjections = freeTextProjections;
    this.timeAggregationGranularity = timeAggregationGranularity;
    this.timeAggregationColumnFormat = timeAggregationColumnFormat;
    this.timeAggregationColumn = timeAggregationColumn;
    this.database = database;
    this.table = table;
    this.timeFilterInterval = timeFilterInterval;
    this.timeFilterColumn = timeFilterColumn;
    this.structuredPredicates = structuredPredicates;
    this.freeTextPredicates = freeTextPredicates;
    this.groupByColumns = groupByColumns;
    this.orderByColumns = orderByColumns;
    this.limit = limit;
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
        getGroupBy(),
        null,
        null,
        getOrderBy(),
        null,
        getFetch(),
        null
    );
  }

  private SqlNodeList getSelectList(final SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder)
      throws SqlParseException {
    List<SqlNode> nodes = new ArrayList<>(); //Arrays.stream(rhsValues).map(FiltersEngine::sqlStringLiteralOf).collect(Collectors.toList());
    groupByColumns.forEach(c -> nodes.add(new SqlIdentifier(List.of(c), SqlParserPos.ZERO)));
    if (timeAggregationColumn != null) {
      String timeGroupExpression = expressionBuilder.getTimeGroupExpression(
          timeAggregationColumn,
          Objects.requireNonNull(timeAggregationColumnFormat),
          Objects.requireNonNull(timeAggregationGranularity));
      SqlNode timeGroupNode = expressionToNode(timeGroupExpression, sqlParserConfig);
      List<SqlNode> aliasOperands = List.of(timeGroupNode, identifierOf(TIME_AGGREGATION_ALIAS));
      SqlNode timeGroupWithAlias = new SqlBasicCall(new SqlAsOperator(),
          aliasOperands.toArray(new SqlNode[0]),
          SqlParserPos.ZERO);
      nodes.add(timeGroupWithAlias);
    }
    structuredSqlProjections.forEach(p -> nodes.add(p.toSql()));
    for (String freeText : freeTextProjections) {
      if (StringUtils.isNotBlank(freeText)) {
        nodes.add(expressionToNode(freeText, sqlParserConfig));
      }
    }
    return SqlNodeList.of(SqlParserPos.ZERO, nodes);
  }

  private SqlNode getFrom() {
    List<String> identifiers = List.of(database, table);
    return new SqlIdentifier(identifiers, SqlParserPos.ZERO);
  }

  private SqlNode getWhere(SqlParser.Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) throws SqlParseException {
    List<SqlNode> predicates = new ArrayList<>();
    if (timeFilterInterval != null) {
      // fixme cyril implement better time format management - datetimeconvert may have bad performance
      String timeColumnConstraint = timeFilterColumn; // assumes time column is in epoch millis
      if (timeAggregationGranularity != null) {
        if (timeAggregationColumn.equals(timeFilterColumn)) {
        }
        // use the alias of the aggregated time column - in this case it's sure time is in epoch millis
        timeColumnConstraint = TIME_AGGREGATION_ALIAS;
      }
      String timeFilterExpression = expressionBuilder.getTimeFilterExpression(
          timeColumnConstraint,
          timeFilterInterval.getStartMillis(),
          timeFilterInterval.getEndMillis());
      SqlNode timeGroupNode = expressionToNode(timeFilterExpression, sqlParserConfig);
      predicates.add(timeGroupNode);
    }
    structuredPredicates.forEach(p -> predicates.add(toCalcitePredicate(p)));
    if (StringUtils.isNotBlank(freeTextPredicates)) {
      String cleanedFreePredicate = freeTextPredicates.replaceFirst("^ *[aA][nN][dD] +", "");
      predicates.add(expressionToNode(cleanedFreePredicate, sqlParserConfig));
    }

    return combinePredicates(predicates);
  }

  private SqlNodeList getGroupBy() {
    List<SqlNode> groupIdentifiers = new ArrayList<>();
    if (timeAggregationGranularity != null) {
      groupIdentifiers.add(identifierOf(TIME_AGGREGATION_ALIAS));
    }
    groupByColumns.stream()
        .filter(g -> !g.equals(timeAggregationColumn))
        .forEach(g -> groupIdentifiers.add(identifierOf(g)));

    return groupIdentifiers.isEmpty() ? null : SqlNodeList.of(SqlParserPos.ZERO, groupIdentifiers);
  }

  private SqlNodeList getOrderBy() {
    List<SqlNode> orderIdentifiers = new ArrayList<>();
    for (String orderByColumn : orderByColumns) {
      if (orderByColumn.equals(timeAggregationColumn)) {
        orderIdentifiers.add(identifierOf(TIME_AGGREGATION_ALIAS));
      } else {
        orderIdentifiers.add(identifierOf(orderByColumn));
      }
    }
    return orderIdentifiers.isEmpty() ? null : SqlNodeList.of(SqlParserPos.ZERO, orderIdentifiers);
  }

  // todo cyril see duplication with filter engine
  private SqlNode getFetch() {
    return numericLiteralOf(limit != null ? limit.toString() : DEFAULT_LIMIT.toString());
  }

  public static String getBetweenClause(DateTime start, DateTime endExclusive, TimeSpec timeSpec,
      final DatasetConfigDTO datasetConfig) {
    // todo cyril good resource  for between clause and time maangement ?
    TimeGranularity dataGranularity = timeSpec.getDataGranularity();
    long dataGranularityMillis = dataGranularity.toMillis();

    String timeField = timeSpec.getColumnName();
    String timeFormat = timeSpec.getFormat();

    // epoch case
    if (timeFormat == null || TimeSpec.SINCE_EPOCH_FORMAT.equals(timeFormat)) {
      long startUnits = (long) Math.ceil(start.getMillis() / (double) dataGranularityMillis);
      long endUnits = (long) Math.ceil(endExclusive.getMillis() / (double) dataGranularityMillis);

      // point query
      if (startUnits == endUnits) {
        return String.format(" %s = %d", timeField, startUnits);
      }

      return String.format(" %s >= %d AND %s < %d", timeField, startUnits, timeField, endUnits);
    }

    // NOTE:
    // this is crazy. epoch rounds up, but timeFormat down
    // we maintain this behavior for backward compatibility.

    DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(timeFormat)
        .withZone(SpiUtils.getDateTimeZone(datasetConfig));
    String startUnits = inputDataDateTimeFormatter.print(start);
    String endUnits = inputDataDateTimeFormatter.print(endExclusive);

    // point query
    if (Objects.equals(startUnits, endUnits)) {
      return String.format(" %s = %s", timeField, startUnits);
    }

    return String.format(" %s >= %s AND %s < %s", timeField, startUnits, timeField, endUnits);
  }

  private static String convertEpochToMinuteAggGranularity(String timeColumnName,
      TimeSpec timeSpec) {
    String groupByTimeColumnName = String
        .format("dateTimeConvert(%s,'%d:%s:%s','%d:%s:%s','1:MINUTES')", timeColumnName,
            timeSpec.getDataGranularity().getSize(), timeSpec.getDataGranularity().getUnit(),
            timeSpec.getFormat(),
            timeSpec.getDataGranularity().getSize(), timeSpec.getDataGranularity().getUnit(),
            timeSpec.getFormat());
    return groupByTimeColumnName;
  }

  // todo cyril good reference to manage time in Pinot
  private static String getTimeColumnQueryName(TimeGranularity aggregationGranularity,
      TimeSpec timeSpec) {
    String timeColumnName = timeSpec.getColumnName();
    if (aggregationGranularity != null) {
      // Convert the time column to 1 minute granularity if it is epoch.
      // E.g., dateTimeConvert(timestampInEpoch,'1:MILLISECONDS:EPOCH','1:MILLISECONDS:EPOCH','1:MINUTES')
      //if (timeSpec.getFormat().equals(DateTimeFieldSpec.TimeFormat.EPOCH.toString())
      //    && !timeSpec.getDataGranularity().equals(aggregationGranularity)) {
      //  return convertEpochToMinuteAggGranularity(timeColumnName, timeSpec);
      //}
    }
    return timeColumnName;
  }

  public static String getDataTimeRangeSql(String dataset, String timeColumnName) {
    return String.format("select min(%s), max(%s) from %s", timeColumnName, timeColumnName,
        dataset);
  }

  /**
   * Surrounds a value with appropriate quote characters.
   *
   * @param value value to be quoted
   * @return quoted value
   * @throws IllegalArgumentException if no unused quote char can be found
   */
  // todo cyril this is where the numeric vs other type of filter is applied --> should be outside the sql imo
  public static String quote(String value) {
    String quoteChar = "";
    if (!StringUtils.isNumeric(value)) {
      quoteChar = "\"";
      if (value.contains(quoteChar)) {
        quoteChar = "'";
      }
      if (value.contains(quoteChar)) {
        throw new IllegalArgumentException(
            String.format("Could not find quote char for expression: %s", value));
      }
    }
    return String.format("%s%s%s", quoteChar, value, quoteChar);
  }

  /**
   * Convert the name of the MetricAggFunction to the name expected by Pinot. See PQL Documentation
   * for details.
   *
   * @param aggFunction function enum to convert
   * @return a valid pinot function name
   */
  public static String convertAggFunction(MetricAggFunction aggFunction) {
    if (aggFunction.isPercentile()) {
      return aggFunction.name()
          .replaceFirst(MetricAggFunction.PERCENTILE_PREFIX, PERCENTILE_TDIGEST_PREFIX);
    }
    return aggFunction.name();
  }

  // todo cyril rename this to predicate
  public static class StructuredSqlStatement {

    final private String operator;
    final private List<String> operands;
    final private String quantifier;

    public StructuredSqlStatement(String operator, List<String> operands, String quantifier) {
      this.operator = operator;
      this.operands = operands;
      this.quantifier = quantifier;
    }

    private SqlNode toSql() {
      if (operator != null) {
        return new SqlBasicCall(
            new SqlUnresolvedFunction(identifierOf(operator),
                null,
                null,
                null,
                null,
                SqlFunctionCategory.NUMERIC),
            operands.stream().map(CalciteUtils::identifierOf).toArray(SqlNode[]::new),
            SqlParserPos.ZERO,
            quantifier != null ? stringLiteralOf(quantifier) : null);
      } else if (operands.size() == 1 && quantifier == null) {
        return identifierOf(operands.get(0));
      } else {
        throw new UnsupportedOperationException(String.format(
            "Unsupported combination for StructuredSqlStatement: %s",
            this));
      }
    }

    public static StructuredSqlStatement fromMetricConfig(MetricConfigDTO metricConfigDTO) {
      String operator;
      List<String> operands;
      String quantifier = null;
      if (metricConfigDTO.getName().equals("*")) {
        operands = List.of("*");
      } else {
        operands = List.of(optional(metricConfigDTO.getAggregationColumn()).orElse(metricConfigDTO.getName()));
      }
      if (metricConfigDTO.getDefaultAggFunction() == MetricAggFunction.COUNT_DISTINCT) {
        operator = MetricAggFunction.COUNT.name();
        quantifier = "DISTINCT";
      } else {
        // fixme cyril this function should depend on the datasource
        operator = convertAggFunction(metricConfigDTO.getDefaultAggFunction());
      }

      return new StructuredSqlStatement(operator, operands, quantifier);
    }
  }
}
