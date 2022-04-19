package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.detectionpipeline.sql.filter.FilterEngineTest.assertThatQueriesAreTheSame;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.datasource.calcite.QueryPredicate.DimensionType;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import ai.startree.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.calcite.sql.parser.SqlParseException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class CalciteRequestTest {

  private static final String DATABASE = "db1";
  private static final String TABLE = "table1";
  private static final String COLUMN_NAME_1 = "col1";
  public static final QueryProjection DIALECT_SPECIFIC_AGGREGATION_PROJECTION = QueryProjection.of(
      MetricAggFunction.PCT90.name(),
      List.of(COLUMN_NAME_1));
  private static final String COLUMN_NAME_2 = "col2";
  public static final QueryProjection UNKNOWN_FUNCTION_PROJECTION = QueryProjection.of("UNKNOWN_MOD",
      List.of(COLUMN_NAME_1, COLUMN_NAME_2));
  public static final QueryProjection COUNT_DISTINCT_AGGREGATION_PROJECTION = QueryProjection.of(
      MetricAggFunction.COUNT_DISTINCT.name(),
      List.of(COLUMN_NAME_1));
  public static final QueryProjection STANDARD_AGGREGATION_PROJECTION = QueryProjection.of(
      MetricAggFunction.SUM.name(),
      List.of(COLUMN_NAME_1));
  public static final QueryProjection SIMPLE_PROJECTION = QueryProjection.of(
      COLUMN_NAME_1);

  private static final SqlLanguage SQL_LANGUAGE = new PinotSqlLanguage();
  public static final String COMPLEX_SQL_PROJECTION_TEXT = "DATETIME(COMPLEX(UN_FN(col1, 3)))";
  SqlExpressionBuilder SQL_EXPRESSION_BUILDER = new PinotSqlExpressionBuilder();

  // todo cyril add a few tests for builder pre conditions

  @Test
  public void testGetSqlWithSimpleProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(SIMPLE_PROJECTION);

    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s FROM %s.%s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithStandardAggregationProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s(%s) FROM %s.%s",
        MetricAggFunction.SUM.name(),
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithCountDistinctProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(COUNT_DISTINCT_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT COUNT(DISTINCT %s) FROM %s.%s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithDialectSpecificAggregationProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(DIALECT_SPECIFIC_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s(%s, 90) FROM %s.%s",
        PinotSqlExpressionBuilder.DIALECT_SPECIFIC_PERCENTILE_FN_NAME,
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithUnknownFunction() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(UNKNOWN_FUNCTION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT UNKNOWN_MOD(%s, %s) FROM %s.%s",
        COLUMN_NAME_1,
        COLUMN_NAME_2,
        DATABASE,
        TABLE);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithMultipleOperandsProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION)
        .addSelectProjection(COUNT_DISTINCT_AGGREGATION_PROJECTION)
        .addSelectProjection(DIALECT_SPECIFIC_AGGREGATION_PROJECTION)
        .addSelectProjection(UNKNOWN_FUNCTION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT %s, SUM(%s), COUNT(DISTINCT %s), %s(%s, 90), UNKNOWN_MOD(%s, %s) FROM %s.%s",
        COLUMN_NAME_1,
        COLUMN_NAME_1,
        COLUMN_NAME_1,
        PinotSqlExpressionBuilder.DIALECT_SPECIFIC_PERCENTILE_FN_NAME,
        COLUMN_NAME_1,
        COLUMN_NAME_1,
        COLUMN_NAME_2,
        DATABASE,
        TABLE);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithFreeTextProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(COMPLEX_SQL_PROJECTION_TEXT);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM %s.%s",
        COMPLEX_SQL_PROJECTION_TEXT,
        DATABASE,
        TABLE);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addSelectProjection(COMPLEX_SQL_PROJECTION_TEXT);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s, %s FROM %s.%s",
        COLUMN_NAME_1,
        COMPLEX_SQL_PROJECTION_TEXT,
        DATABASE,
        TABLE);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithNumericPredicate() throws SqlParseException {
    final List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ, OPER.GE, OPER.GT, OPER.LE, OPER.LT);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
          .addSelectProjection(SIMPLE_PROJECTION)
          .addPredicate(QueryPredicate.of(
              new Predicate(COLUMN_NAME_2, oper, "3"),
              DimensionType.NUMERIC,
              TABLE
          ));
      final CalciteRequest request = builder.build();
      final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
      // convert != in other standard format <>
      final String sqlOperator = oper.toString().equals("!=") ? "<>" : oper.toString();
      final String expected = String.format("SELECT %s FROM %s.%s WHERE (%s.%s %s %s)",
          COLUMN_NAME_1,
          DATABASE,
          TABLE,
          TABLE,
          COLUMN_NAME_2,
          sqlOperator,
          3);

      assertThatQueriesAreTheSame(output, expected);
    }
  }

  @Test
  public void testGetSqlWithStringPredicate() throws SqlParseException {
    List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
          .addSelectProjection(SIMPLE_PROJECTION)
          .addPredicate(QueryPredicate.of(
              new Predicate(COLUMN_NAME_2, oper, "myText"),
              DimensionType.STRING,
              TABLE
          ));
      final CalciteRequest request = builder.build();
      final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
      // convert != in other standard format <>
      final String sqlOperator = oper.toString().equals("!=") ? "<>" : oper.toString();
      final String expected = String.format("SELECT %s FROM %s.%s WHERE (%s.%s %s %s)",
          COLUMN_NAME_1,
          DATABASE,
          TABLE,
          TABLE,
          COLUMN_NAME_2,
          sqlOperator,
          "'myText'");

      assertThatQueriesAreTheSame(output, expected);
    }
  }

  @Test
  public void testGetSqlWithBooleanPredicate() throws SqlParseException {
    List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
          .addSelectProjection(SIMPLE_PROJECTION)
          .addPredicate(QueryPredicate.of(
              new Predicate(COLUMN_NAME_2, oper, "true"),
              DimensionType.BOOLEAN,
              TABLE
          ));
      final CalciteRequest request = builder.build();
      final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
      // convert != in other standard format <>
      final String sqlOperator = oper.toString().equals("!=") ? "<>" : oper.toString();
      final String expected = String.format("SELECT %s FROM %s.%s WHERE (%s.%s %s %s)",
          COLUMN_NAME_1,
          DATABASE,
          TABLE,
          TABLE,
          COLUMN_NAME_2,
          sqlOperator,
          "TRUE");

      assertThatQueriesAreTheSame(output, expected);
    }
  }

  @Test
  public void testGetSqlWithInPredicates() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(QueryPredicate.of(
            new Predicate(COLUMN_NAME_2, OPER.IN, new String[]{"val1", "val2"}),
            DimensionType.STRING,
            TABLE
        ));
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM %s.%s WHERE (%s.%s IN (%s, %s))",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        TABLE,
        COLUMN_NAME_2,
        "'val1'",
        "'val2'");

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithFreeTextPredicate() throws SqlParseException {
    String complexWhere = "complexFunction(col2, col3, 10) >= 27";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(complexWhere);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM %s.%s WHERE %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        complexWhere);

    assertThatQueriesAreTheSame(output, expected);
  }

  @Test
  public void testGetSqlWithFreeTextPredicateRemovingStartingAnd() throws SqlParseException {
    String complexWhere = "complexFunction(col2, col3, 10) >= 27 OR colX = TRUE";
    String complexWhereWithStartingAnd = "AND " + complexWhere;
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(complexWhereWithStartingAnd);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM %s.%s WHERE %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        complexWhere);

    assertThatQueriesAreTheSame(output, expected);
  }

  // test multi predicates
  @Test
  public void testGetSqlWithMultiplePredicates() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(DATABASE, TABLE)
        .addSelectProjection(SIMPLE_PROJECTION);
        //.add
  }

  // with Predicate =
  // with !=
  // free

  // test datetime aggregation
  // test datetime filter edge case
  // test gtoup by
  // test prder by
  // test limit

  // test freeSqlText injection


  // TODO cyril - should be easy to express:
  //  a timeseries --> with timegrouping
  //  a breakdown on a time interval, with the standard format of the time column (no datetimeconvert)
  //  a percentileFunction --> DONE
  // and also: a distinct operator, a custom limit, a order by, a group by
  // a custom select string
  // a custom where string
  // a custom group by string
  // layer that makes the following decision: which dimension is STRING/NUMERIC/BOOLEAN
  // layer that converts MetricAggFunction to a proper format STRING: is this even possible? --> no because arguments are need
  // query projection can have two types: MetricAggFunction or String??

  // TODO cyril use a test language rather than the pinot language
  // todo use the same testLanguage everywhere -->
  private static class PinotSqlLanguage implements SqlLanguage {

    private static final ThirdEyeSqlParserConfig SQL_PARSER_CONFIG = new ThirdEyeSqlParserConfig.Builder()
        .withLex("MYSQL_ANSI")
        .withConformance("BABEL")
        .withParserFactory("SqlBabelParserImpl")
        .build();

    private static final ThirdeyeSqlDialect SQL_DIALECT = new ThirdeyeSqlDialect.Builder()
        .withBaseDialect("AnsiSqlDialect")
        .withIdentifierQuoteString("\"")
        .withIdentifierEscapedQuoteString("")
        .build();

    @Override
    public ThirdEyeSqlParserConfig getSqlParserConfig() {
      return SQL_PARSER_CONFIG;
    }

    @Override
    public ThirdeyeSqlDialect getSqlDialect() {
      return SQL_DIALECT;
    }
  }

  private static class PinotSqlExpressionBuilder implements SqlExpressionBuilder {

    public static final String DIALECT_SPECIFIC_PERCENTILE_FN_NAME = "PERCENTILE_TDIGEST";

    @Override
    public String getTimeFilterExpression(final String timeColumn, final long minTimeMillisIncluded,
        long maxTimeMillisExcluded,
        @Nullable final String timeFormat) {
      if (timeFormat == null || "EPOCH".equals(timeFormat)) {
        return String.format(
            "%s >= %s AND %s < %s",
            timeColumn,
            minTimeMillisIncluded,
            timeColumn,
            maxTimeMillisExcluded);
      } else if ("SIMPLE_DATE_FORMAT".equals(timeFormat)) {
        final DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(timeFormat);
        final String startUnits = inputDataDateTimeFormatter.print(minTimeMillisIncluded);
        final String endUnits = inputDataDateTimeFormatter.print(maxTimeMillisExcluded);
        return String.format(" %s >= %s AND %s < %s", timeColumn, startUnits, timeColumn, endUnits);
      } else {
        throw new UnsupportedOperationException(
            String.format("Unknown timeFormat for Pinot datasource: %s ", timeFormat));
      }
    }

    @Override
    public String getTimeGroupExpression(String timeColumn, String timeColumnFormat,
        Period granularity) {
      return String.format(" DATETIMECONVERT(%s,'%s', '1:MILLISECONDS:EPOCH', '%s') ",
          timeColumn,
          timeColumnFormatToPinotFormat(timeColumnFormat),
          periodToPinotFormat(granularity)
      );
    }

    private String timeColumnFormatToPinotFormat(String timeColumnFormat) {
      switch (timeColumnFormat) {
        case "EPOCH_MILLIS":
        case "1:MILLISECONDS:EPOCH":
          return "1:MILLISECONDS:EPOCH";
        case "EPOCH":
        case "1:SECONDS:EPOCH":
          return "1:SECONDS:EPOCH";
        case "EPOCH_HOURS":
        case "1:HOURS:EPOCH":
          return "1:HOURS:EPOCH";
        default:
          new SimpleDateFormat(timeColumnFormat);
          return String.format("1:DAYS:SIMPLE_DATE_FORMAT:%s", timeColumnFormat);
      }
    }

    private String periodToPinotFormat(final Period period) {
      if (period.getYears() > 0) {
        throw new RuntimeException(String.format(
            "Pinot datasource cannot round to yearly granularity: %s",
            period));
      } else if (period.getMonths() > 0) {
        throw new RuntimeException(String.format(
            "Pinot datasource cannot round to monthly granularity: %s",
            period));
      } else if (period.getWeeks() > 0) {
        throw new RuntimeException(String.format(
            "Pinot datasource cannot round to weekly granularity: %s",
            period));
      } else if (period.getDays() > 0) {
        return String.format("%s:DAYS", period.getDays());
      } else if (period.getHours() > 0) {
        return String.format("%s:HOURS", period.getHours());
      } else if (period.getMinutes() > 0) {
        return String.format("%s:MINUTES", period.getMinutes());
      } else if (period.getSeconds() > 0) {
        return String.format("%s:SECONDS", period.getSeconds());
      } else if (period.getMillis() > 0) {
        return String.format("%s:MILLISECONDS", period.getMillis());
      }
      throw new RuntimeException(String.format("Could not translate Period to Pinot granularity: %s",
          period));
    }

    @Override
    public String getCustomDialectSql(final MetricAggFunction metricAggFunction,
        final List<String> operands,
        final String quantifier) {
      switch (metricAggFunction) {
        case PCT50:
        case PCT90:
        case PCT95:
        case PCT99:
          String aggFunctionString = metricAggFunction.name();
          int percentile = Integer.parseInt(aggFunctionString.substring(3));
          checkArgument(operands.size() == 1,
              "Incorrect number of operands for percentile sql generation. Expected: 1. Got: %s",
              operands.size());
          return new StringBuilder().append(DIALECT_SPECIFIC_PERCENTILE_FN_NAME)
              .append("(")
              .append(operands.get(0))
              .append(",")
              .append(percentile)
              .append(")")
              .toString();
        default:
          throw new UnsupportedOperationException();
      }
    }
  }
}
