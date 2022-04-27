package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.datasource.calcite.CalciteRequest.TIME_AGGREGATION_ALIAS;
import static ai.startree.thirdeye.spi.Constants.UTC_LIKE_TIMEZONES;
import static ai.startree.thirdeye.util.CalciteUtils.EQUALS_OPERATOR;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.stringLiteralOf;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import ai.startree.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;
import ai.startree.thirdeye.spi.metric.DimensionType;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.testutils.SqlUtils;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class CalciteRequestTest {

  private static final String DATABASE = "db1";
  private static final String TABLE = "table1";
  private static final String COLUMN_NAME_1 = "col1";
  private static final String COLUMN_NAME_2 = "col2";
  private static final String COLUMN_NAME_3 = "col3";
  public static final QueryProjection DIALECT_SPECIFIC_AGGREGATION_PROJECTION = QueryProjection.of(
      MetricAggFunction.PCT90.name(),
      List.of(COLUMN_NAME_1));
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
  public static final String COMPLEX_SQL_PROJECTION_TEXT = "DATETIME(COMPLEX(UN_FN(col1, 3)))";
  public static final SqlNode SIMPLE_SQL_NODE_PROJECTION = identifierOf(COLUMN_NAME_3);

  private static final SqlLanguage SQL_LANGUAGE = new TestPinotLikeSqlLanguage();
  SqlExpressionBuilder SQL_EXPRESSION_BUILDER = new TestPinotLikeSqlExpressionBuilder();

  @Test
  public void testGetSqlWithSimpleProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION);

    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s FROM %s.%s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGeSqlWithSimpleProjectionWithAlias() throws SqlParseException {
    final String alias = "alias1";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION.withAlias(alias));

    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s AS %s FROM %s.%s",
        COLUMN_NAME_1,
        alias,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithStandardAggregationProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s(%s) FROM %s.%s",
        MetricAggFunction.SUM.name(),
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithCountDistinctProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(COUNT_DISTINCT_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT COUNT(DISTINCT %s) FROM %s.%s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithDialectSpecificAggregationProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(DIALECT_SPECIFIC_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s(%s, 90) FROM %s.%s",
        TestPinotLikeSqlExpressionBuilder.DIALECT_SPECIFIC_PERCENTILE_FN_NAME,
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithUnknownFunction() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(UNKNOWN_FUNCTION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT UNKNOWN_MOD(%s, %s) FROM %s.%s",
        COLUMN_NAME_1,
        COLUMN_NAME_2,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithMultipleOperandsProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
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
        TestPinotLikeSqlExpressionBuilder.DIALECT_SPECIFIC_PERCENTILE_FN_NAME,
        COLUMN_NAME_1,
        COLUMN_NAME_1,
        COLUMN_NAME_2,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithFreeTextProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(COMPLEX_SQL_PROJECTION_TEXT);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM %s.%s",
        COMPLEX_SQL_PROJECTION_TEXT,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithFreeTextProjectionWithAlias() throws SqlParseException {
    final String alias = "alias1";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(COMPLEX_SQL_PROJECTION_TEXT + " as " + alias) ;
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s AS %s FROM %s.%s",
        COMPLEX_SQL_PROJECTION_TEXT,
        alias,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithSqlNodeProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = CalciteRequest.newBuilder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_SQL_NODE_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM %s.%s",
        COLUMN_NAME_3,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextAndCalciteProjection() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addSelectProjection(COMPLEX_SQL_PROJECTION_TEXT)
        .addSelectProjection(SIMPLE_SQL_NODE_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s, %s, %s FROM %s.%s",
        COLUMN_NAME_1,
        COMPLEX_SQL_PROJECTION_TEXT,
        COLUMN_NAME_3,
        DATABASE,
        TABLE);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithNumericPredicate() throws SqlParseException {
    final List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ, OPER.GE, OPER.GT, OPER.LE, OPER.LT);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
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

      Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
    }
  }

  @Test
  public void testGetSqlWithStringPredicate() throws SqlParseException {
    List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
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

      Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
    }
  }

  @Test
  public void testGetSqlWithBooleanPredicate() throws SqlParseException {
    List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
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

      Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
    }
  }

  @Test
  public void testGetSqlWithInPredicates() throws SqlParseException {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
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

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithFreeTextPredicate() throws SqlParseException {
    String complexWhere = "complexFunction(col2, col3, 10) >= 27";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(complexWhere);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM %s.%s WHERE %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        complexWhere);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithFreeTextPredicateRemovingStartingAnd() throws SqlParseException {
    String complexWhere = "complexFunction(col2, col3, 10) >= 27 OR colX = TRUE";
    String complexWhereWithStartingAnd = "AND " + complexWhere;
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(complexWhereWithStartingAnd);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM %s.%s WHERE %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        complexWhere);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextAndCalcitePredicateAndTimeFilter()
      throws SqlParseException {
    final String textPredicate = "col2 = 'test2'";
    final SqlNode sqlNodePredicate = new SqlBasicCall(EQUALS_OPERATOR,
        new SqlNode[]{identifierOf("col3"), stringLiteralOf("test3")},
        SqlParserPos.ZERO);
    final Interval timeFilterInterval = new Interval(100L, 100000L);
    final String epoch_date = "epoch_date";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(QueryPredicate.of(new Predicate(COLUMN_NAME_1, OPER.EQ, "test1"),
            DimensionType.STRING))
        .addPredicate(textPredicate)
        .addPredicate(sqlNodePredicate)
        .withTimeFilter(timeFilterInterval, epoch_date, "EPOCH", "MILLISECONDS");
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format(
        "SELECT %s FROM %s.%s WHERE %s >= %s AND %s < %s AND (col1 = 'test1') AND col2 = 'test2' AND (col3 = 'test3')",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        epoch_date,
        timeFilterInterval.getStartMillis(),
        epoch_date,
        timeFilterInterval.getEndMillis());

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithTimeAggregationOnEpochSeconds() throws SqlParseException {
    final String timeAggregationColumn = "date_epoch";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeAggregation(Period.days(7), timeAggregationColumn, "EPOCH", "SECONDS", false);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT %s, DATETIMECONVERT(%s, '1:SECONDS:EPOCH', '1:MILLISECONDS:EPOCH', '%s') AS %s FROM %s.%s GROUP BY %s",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "7:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithTimeAggregationOnEpochMilliSeconds() throws SqlParseException {
    final String timeAggregationColumn = "date_epoch";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeAggregation(Period.days(7), timeAggregationColumn, "EPOCH", "MILLISECONDS", false);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT %s, DATETIMECONVERT(%s, '1:MILLISECONDS:EPOCH', '1:MILLISECONDS:EPOCH', '%s') AS %s FROM %s.%s GROUP BY %s",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "7:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithTimeAggregationOnSimpleDateFormat() throws SqlParseException {
    final String timeAggregationColumn = "date_sdf";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeAggregation(Period.days(7),
            timeAggregationColumn,
            "SIMPLE_DATE_FORMAT:yyyyMMdd",
            null,
            false);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT %s, DATETIMECONVERT(%s, '1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd', '1:MILLISECONDS:EPOCH', '%s') AS %s FROM %s.%s GROUP BY %s",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "7:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithTimeAggregationWithOrderOnTimeAggregation() throws SqlParseException {
    final String timeAggregationColumn = "date_epoch";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        // null timeFormat defaults to milliseconds
        .withTimeAggregation(Period.days(7), timeAggregationColumn, "EPOCH", "MILLISECONDS", true);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT %s, DATETIMECONVERT(%s, '1:MILLISECONDS:EPOCH', '1:MILLISECONDS:EPOCH', '%s') AS %s FROM %s.%s GROUP BY %s ORDER BY %s",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "7:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS,
        TIME_AGGREGATION_ALIAS);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithTimeFilter() throws SqlParseException {
    final String timeAggregationColumn = "date_epoch";
    final Interval timeFilterInterval = new Interval(100L, 100000000L);
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeFilter(timeFilterInterval, timeAggregationColumn, "EPOCH", "MILLISECONDS");
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT %s FROM %s.%s WHERE %s >= %s AND %s < %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        timeAggregationColumn,
        timeFilterInterval.getStartMillis(),
        timeAggregationColumn,
        timeFilterInterval.getEndMillis()
    );

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithTimeFilterWithReservedKeyword() throws SqlParseException {
    final String reservedKeywordTimeAggregationColumn = "date";
    final String quotedTimeAggregationColumn = TestPinotLikeSqlLanguage.IDENTIFIER_QUOTE_STRING + reservedKeywordTimeAggregationColumn + TestPinotLikeSqlLanguage.IDENTIFIER_QUOTE_STRING;
    final Interval timeFilterInterval = new Interval(100L, 100000000L);
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeFilter(timeFilterInterval, reservedKeywordTimeAggregationColumn, "EPOCH", "MILLISECONDS");
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT %s FROM %s.%s WHERE %s >= %s AND %s < %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        quotedTimeAggregationColumn,
        timeFilterInterval.getStartMillis(),
        quotedTimeAggregationColumn,
        timeFilterInterval.getEndMillis()
    );

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithTimeFilterInSimpleDateFormat() throws SqlParseException {
    final String timeAggregationColumn = "date_sdf";
    final Interval timeFilterInterval = new Interval(new DateTime(2020, 1, 1, 0, 0),
        new DateTime(2021, 10, 10, 0, 0));
    final String simpleDateFormat = "yyyyMMdd";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeFilter(timeFilterInterval, timeAggregationColumn, simpleDateFormat, null);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT %s FROM %s.%s WHERE %s >= %s AND %s < %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        timeAggregationColumn,
        "20200101",
        timeAggregationColumn,
        "20211010"
    );

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithTimeAggregationAndTimeFilter() throws SqlParseException {
    // test that the filter is applied on the bucketed column - important - this is the purpose of the high level withTimeAggregation and withTimeFilter
    final String timeAggregationColumn = "date_sdf";
    final Interval timeFilterInterval = new Interval(100L, 100000000L);
    final String timeColumnFormat = "yyyyMMdd";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION)
        .withTimeAggregation(Period.days(1), timeAggregationColumn, timeColumnFormat, null, true)
        // timeFormat and unit is not used because the filtering will use the buckets in epoch millis
        .withTimeFilter(timeFilterInterval, timeAggregationColumn, timeColumnFormat, "DAYS");
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT SUM(%s), DATETIMECONVERT(%s, '1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd', '1:MILLISECONDS:EPOCH', '%s') AS %s FROM %s.%s WHERE %s >= %s AND %s < %s GROUP BY %s ORDER BY %s",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "1:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS,
        timeFilterInterval.getStartMillis(),
        TIME_AGGREGATION_ALIAS,
        timeFilterInterval.getEndMillis(),
        TIME_AGGREGATION_ALIAS,
        TIME_AGGREGATION_ALIAS
    );

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextAndCalciteGroupByAndTimeAggregation()
      throws SqlParseException {
    // only one test for group by - test everything at once
    final String timeAggregationColumn = "date_epoch";
    final String timeColumnFormat = "yyyyMMdd";
    final String freeTextProjection = "MOD(" + COLUMN_NAME_1 + ", " + COLUMN_NAME_2 + ") AS mod1";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION)
        .addSelectProjection(QueryProjection.of(COLUMN_NAME_2))
        .addSelectProjection(freeTextProjection)
        .withTimeAggregation(Period.days(1), timeAggregationColumn, timeColumnFormat, null, true)
        // missing projection on col3 : because group by col3 but don't select it for test purpose
        .addGroupByProjection(QueryProjection.of(COLUMN_NAME_2))
        .addGroupByProjection(freeTextProjection)
        .addGroupByProjection(identifierOf(COLUMN_NAME_3));
    // timeFormat and unit is not used because the filtering will use the buckets in epoch millis
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT SUM(%s), %s, %s, DATETIMECONVERT(%s, '1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd', '1:MILLISECONDS:EPOCH', '%s') AS %s FROM %s.%s GROUP BY %s, %s, %s, %s ORDER BY %s",
        COLUMN_NAME_1,
        COLUMN_NAME_2,
        freeTextProjection,
        timeAggregationColumn,
        "1:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        COLUMN_NAME_2,
        freeTextProjection,
        COLUMN_NAME_3,
        TIME_AGGREGATION_ALIAS,
        TIME_AGGREGATION_ALIAS
    );

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextAndCalciteOrderByAndTimeAggregationOrderBy()
      throws SqlParseException {
    // only one test for group by - test everything at once
    final String timeAggregationColumn = "date_epoch";
    final String timeColumnFormat = "yyyyMMdd";
    final String freeTextProjection = "MOD(" + COLUMN_NAME_1 + ", " + COLUMN_NAME_2 + ")";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION)
        .withTimeAggregation(Period.days(1), timeAggregationColumn, timeColumnFormat, null, true)
        // missing projection on col3 : because group by col3 but don't select it for test purpose
        .addOrderByProjection(QueryProjection.of(COLUMN_NAME_2))
        .addOrderByProjection(freeTextProjection)
        .addOrderByProjection(identifierOf(COLUMN_NAME_3));
    // timeFormat and unit is not used because the filtering will use the buckets in epoch millis
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT SUM(%s), DATETIMECONVERT(%s, '1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd', '1:MILLISECONDS:EPOCH', '%s') AS %s FROM %s.%s GROUP BY %s ORDER BY %s, %s, %s, %s",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "1:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS,
        COLUMN_NAME_2,
        freeTextProjection,
        COLUMN_NAME_3,
        TIME_AGGREGATION_ALIAS
    );

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithLimit() throws SqlParseException {
    final int limit = 1000;
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withLimit(limit);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s FROM %s.%s FETCH NEXT %s ROWS ONLY",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        limit);

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

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
  private static class TestPinotLikeSqlLanguage implements SqlLanguage {

    public static final String IDENTIFIER_QUOTE_STRING = "\"";

    private static final ThirdEyeSqlParserConfig SQL_PARSER_CONFIG = new ThirdEyeSqlParserConfig.Builder()
        .withLex("MYSQL_ANSI")
        .withConformance("BABEL")
        .withParserFactory("SqlBabelParserImpl")
        .build();

    private static final ThirdeyeSqlDialect SQL_DIALECT = new ThirdeyeSqlDialect.Builder()
        .withBaseDialect("AnsiSqlDialect")
        .withIdentifierQuoteString(IDENTIFIER_QUOTE_STRING)
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

  private static class TestPinotLikeSqlExpressionBuilder implements SqlExpressionBuilder {

    public static final String DIALECT_SPECIFIC_PERCENTILE_FN_NAME = "PERCENTILE_TDIGEST";
    public static final long SECOND_SCALE = 1000; // number of second in milliseconds
    public static final long MINUTE_SCALE = 60 * SECOND_SCALE; // number of second in milliseconds
    public static final long HOUR_SCALE = 60 * MINUTE_SCALE;
    public static final long DAY_SCALE = 24 * HOUR_SCALE;

    @Override
    public String getTimeFilterExpression(final String timeColumn, final long minTimeMillisIncluded,
        final long maxTimeMillisExcluded) {
      return String.format("%s >= %s AND %s < %s",
          timeColumn,
          minTimeMillisIncluded,
          timeColumn,
          maxTimeMillisExcluded);
    }

    @Override
    public String getTimeFilterExpression(final String timeColumn, final long minTimeMillisIncluded,
        final long maxTimeMillisExcluded,
        @Nullable final String timeFormat,
        @Nullable final String timeUnit) {
      if (timeFormat == null) {
        return getTimeFilterExpression(timeColumn, minTimeMillisIncluded, maxTimeMillisExcluded);
      }
      String lowerBound;
      String upperBound;
      if ("EPOCH".equals(timeFormat)) {
        if (TimeUnit.MILLISECONDS.toString().equals(timeUnit)) {
          lowerBound = String.valueOf(minTimeMillisIncluded);
          upperBound = String.valueOf(maxTimeMillisExcluded);
        } else if (timeUnit == null || TimeUnit.SECONDS.toString().equals(timeUnit)) {
          lowerBound = String.valueOf(minTimeMillisIncluded / SECOND_SCALE);
          upperBound = String.valueOf(maxTimeMillisExcluded / SECOND_SCALE);
        } else if (TimeUnit.MINUTES.toString().equals(timeUnit)) {
          lowerBound = String.valueOf(minTimeMillisIncluded / MINUTE_SCALE);
          upperBound = String.valueOf(maxTimeMillisExcluded / MINUTE_SCALE);
        } else if (TimeUnit.HOURS.toString().equals(timeUnit)) {
          lowerBound = String.valueOf(minTimeMillisIncluded / HOUR_SCALE);
          upperBound = String.valueOf(maxTimeMillisExcluded / HOUR_SCALE);
        } else if (TimeUnit.DAYS.toString().equals(timeUnit)) {
          lowerBound = String.valueOf(minTimeMillisIncluded / DAY_SCALE);
          upperBound = String.valueOf(maxTimeMillisExcluded / DAY_SCALE);
        } else {
          throw new UnsupportedOperationException(String.format(
              "Unsupported TimeUnit for filter expression: %s",
              timeUnit));
        }
      } else {
        // case SIMPLE_DATE_FORMAT
        String simpleDateFormatString = removeSimpleDateFormatPrefix(timeFormat);
        final DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(
            simpleDateFormatString);
        lowerBound = inputDataDateTimeFormatter.print(minTimeMillisIncluded);
        upperBound = inputDataDateTimeFormatter.print(maxTimeMillisExcluded);
      }

      return String.format("%s >= %s AND %s < %s", timeColumn, lowerBound, timeColumn, upperBound);
    }

    @NonNull
    private String removeSimpleDateFormatPrefix(final String timeColumnFormat) {
      // remove (1:DAYS:)SIMPLE_DATE_FORMAT:
      return timeColumnFormat.replaceFirst("^([0-9]:[A-Z]+:)?SIMPLE_DATE_FORMAT:", "");
    }

    @Override
    public String getTimeGroupExpression(final String timeColumn, final @Nullable String timeFormat,
        final Period granularity, final @Nullable String timeUnit, @Nullable final String timezone) {
      if (timeFormat == null) {
        return getTimeGroupExpression(timeColumn, "EPOCH_MILLIS", granularity, timezone);
      }
      if ("EPOCH".equals(timeFormat)) {
        return getTimeGroupExpression(timeColumn, "1:" + timeUnit + ":EPOCH", granularity, timezone);
      }
      // case simple date format
      return getTimeGroupExpression(timeColumn, timeFormat, granularity, timezone);
    }

    @Override
    public String getTimeGroupExpression(String timeColumn, String timeColumnFormat,
        Period granularity, @Nullable final String timezone) {
      if (timezone == null || UTC_LIKE_TIMEZONES.contains(timezone)) {
        return String.format(" DATETIMECONVERT(%s,'%s', '1:MILLISECONDS:EPOCH', '%s') ",
            timeColumn,
            timeColumnFormatToPinotFormat(timeColumnFormat),
            periodToPinotFormat(granularity)
        );
      }
      // workaround to bucket with a custom timezone - see https://github.com/apache/pinot/issues/8581
      return String.format(
          "FromDateTime(DATETIMECONVERT(%s, '%s', '1:DAYS:SIMPLE_DATE_FORMAT:yyyy-MM-dd HH:mm:ss.SSSZ tz(%s)', '%s'), 'yyyy-MM-dd HH:mm:ss.SSSZ') ",
          timeColumn,
          timeColumnFormatToPinotFormat(timeColumnFormat),
          timezone,
          periodToPinotFormat(granularity));
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
          final String simpleDateFormatString = removeSimpleDateFormatPrefix(timeColumnFormat);
          new SimpleDateFormat(simpleDateFormatString);
          return String.format("1:DAYS:SIMPLE_DATE_FORMAT:%s", simpleDateFormatString);
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
