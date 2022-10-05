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

import static ai.startree.thirdeye.datasource.calcite.CalciteRequest.TIME_AGGREGATION_ALIAS;
import static ai.startree.thirdeye.spi.Constants.UTC_LIKE_TIMEZONES;
import static ai.startree.thirdeye.util.CalciteUtils.EQUALS_OPERATOR;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.stringLiteralOf;
import static com.google.common.base.Preconditions.checkArgument;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import ai.startree.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;
import ai.startree.thirdeye.spi.metric.DimensionType;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.testutils.SqlUtils;
import com.google.common.annotations.VisibleForTesting;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class CalciteRequestTest {

  public static final String COMPLEX_SQL_PROJECTION_TEXT = "DATETIME(COMPLEX(UN_FN(col1, 3)))";
  public static final String COMPLEX_SQL_PROJECTION_TEXT_QUOTED_IDENTIFIERS = "\"DATETIME\"(\"COMPLEX\"(\"UN_FN\"(\"col1\", 3)))";
  private static final String DATABASE = "db1";
  private static final String TABLE = "table1";
  private static final String COLUMN_NAME_1 = "col1";
  public static final QueryProjection DIALECT_SPECIFIC_AGGREGATION_PROJECTION = QueryProjection.of(
      MetricAggFunction.PCT90.name(),
      List.of(COLUMN_NAME_1));
  public static final QueryProjection COUNT_DISTINCT_AGGREGATION_PROJECTION = QueryProjection.of(
      MetricAggFunction.COUNT_DISTINCT.name(),
      List.of(COLUMN_NAME_1));
  public static final QueryProjection STANDARD_AGGREGATION_PROJECTION = QueryProjection.of(
      MetricAggFunction.SUM.name(),
      List.of(COLUMN_NAME_1));
  public static final QueryProjection SIMPLE_PROJECTION = QueryProjection.of(COLUMN_NAME_1);
  private static final String COLUMN_NAME_2 = "col2";
  public static final QueryProjection UNKNOWN_FUNCTION_PROJECTION = QueryProjection.of("UNKNOWN_MOD",
      List.of(COLUMN_NAME_1, COLUMN_NAME_2));
  private static final String COLUMN_NAME_3 = "col3";
  public static final SqlNode SIMPLE_SQL_NODE_PROJECTION = identifierOf(COLUMN_NAME_3);

  private static final SqlLanguage SQL_LANGUAGE = new TestPinotLikeSqlLanguage();
  private static final SqlExpressionBuilder SQL_EXPRESSION_BUILDER = new TestPinotLikeSqlExpressionBuilder();

  private static void assertEquivalent(final String output, final String expected) {
    assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testGetSqlWithSimpleProjection() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION);

    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT \"%s\" FROM \"%s\".\"%s\"",
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithFormulaProjection() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(QueryProjection.of("5*col1 -col2"));

    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s FROM \"%s\".\"%s\"",
        "5 * \"col1\" - \"col2\"",
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithSimpleProjectionWithAlias() {
    final String alias = "alias1";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION.withAlias(alias));

    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT \"%s\" AS \"%s\" FROM \"%s\".\"%s\"",
        COLUMN_NAME_1,
        alias,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithStandardAggregationProjection() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT %s(\"%s\") FROM \"%s\".\"%s\"",
        MetricAggFunction.SUM.name(),
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithCountDistinctProjection() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(COUNT_DISTINCT_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT COUNT(DISTINCT \"%s\") FROM \"%s\".\"%s\"",
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithDialectSpecificAggregationProjection() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(DIALECT_SPECIFIC_AGGREGATION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT \"%s\"(\"%s\", 90) FROM \"%s\".\"%s\"",
        TestPinotLikeSqlExpressionBuilder.PERCENTILE_TDIGEST_PREFIX,
        COLUMN_NAME_1,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithUnknownFunction() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(UNKNOWN_FUNCTION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"UNKNOWN_MOD\"(\"%s\", \"%s\") FROM \"%s\".\"%s\"",
        COLUMN_NAME_1,
        COLUMN_NAME_2,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithMultipleOperandsProjection() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION)
        .addSelectProjection(COUNT_DISTINCT_AGGREGATION_PROJECTION)
        .addSelectProjection(DIALECT_SPECIFIC_AGGREGATION_PROJECTION)
        .addSelectProjection(UNKNOWN_FUNCTION_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"%s\", SUM(\"%s\"), COUNT(DISTINCT \"%s\"), \"%s\"(\"%s\", 90), \"UNKNOWN_MOD\"(\"%s\", \"%s\") FROM \"%s\".\"%s\"",
        COLUMN_NAME_1,
        COLUMN_NAME_1,
        COLUMN_NAME_1,
        TestPinotLikeSqlExpressionBuilder.PERCENTILE_TDIGEST_PREFIX,
        COLUMN_NAME_1,
        COLUMN_NAME_1,
        COLUMN_NAME_2,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithFreeTextProjection() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(COMPLEX_SQL_PROJECTION_TEXT);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s FROM \"%s\".\"%s\"",
        COMPLEX_SQL_PROJECTION_TEXT_QUOTED_IDENTIFIERS,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithFreeTextProjectionWithAlias() {
    final String alias = "alias1";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(COMPLEX_SQL_PROJECTION_TEXT + " as " + alias);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT %s AS \"%s\" FROM \"%s\".\"%s\"",
        COMPLEX_SQL_PROJECTION_TEXT_QUOTED_IDENTIFIERS,
        alias,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithSqlNodeProjection() {
    final CalciteRequest.Builder builder = CalciteRequest.newBuilder(TABLE)
        .withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_SQL_NODE_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT \"%s\" FROM \"%s\".\"%s\"",
        COLUMN_NAME_3,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextAndCalciteProjection() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addSelectProjection(COMPLEX_SQL_PROJECTION_TEXT)
        .addSelectProjection(SIMPLE_SQL_NODE_PROJECTION);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT \"%s\", %s, \"%s\" FROM \"%s\".\"%s\"",
        COLUMN_NAME_1,
        COMPLEX_SQL_PROJECTION_TEXT_QUOTED_IDENTIFIERS,
        COLUMN_NAME_3,
        DATABASE,
        TABLE);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithNumericPredicate() {
    final List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ, OPER.GE, OPER.GT, OPER.LE, OPER.LT);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
          .addSelectProjection(SIMPLE_PROJECTION)
          .addPredicate(QueryPredicate.of(new Predicate(COLUMN_NAME_2, oper, "3"),
              DimensionType.NUMERIC,
              TABLE));
      final CalciteRequest request = builder.build();
      final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
      // convert != in other standard format <>
      final String sqlOperator = oper.toString().equals("!=") ? "<>" : oper.toString();
      final String expected = String.format(
          "SELECT \"%s\" FROM \"%s\".\"%s\" WHERE (\"%s\".\"%s\" %s %s)",
          COLUMN_NAME_1,
          DATABASE,
          TABLE,
          TABLE,
          COLUMN_NAME_2,
          sqlOperator,
          3);

      assertEquivalent(output, expected);
    }
  }

  @Test
  public void testGetSqlWithStringPredicate() {
    List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
          .addSelectProjection(SIMPLE_PROJECTION)
          .addPredicate(QueryPredicate.of(new Predicate(COLUMN_NAME_2, oper, "myText"),
              DimensionType.STRING,
              TABLE));
      final CalciteRequest request = builder.build();
      final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
      // convert != in other standard format <>
      final String sqlOperator = oper.toString().equals("!=") ? "<>" : oper.toString();
      final String expected = String.format(
          "SELECT \"%s\" FROM \"%s\".\"%s\" WHERE (\"%s\".\"%s\" %s %s)",
          COLUMN_NAME_1,
          DATABASE,
          TABLE,
          TABLE,
          COLUMN_NAME_2,
          sqlOperator,
          "'myText'");

      assertEquivalent(output, expected);
    }
  }

  @Test
  public void testGetSqlWithBooleanPredicate() {
    List<OPER> binaryOpers = List.of(OPER.EQ, OPER.NEQ);
    for (OPER oper : binaryOpers) {
      final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
          .addSelectProjection(SIMPLE_PROJECTION)
          .addPredicate(QueryPredicate.of(new Predicate(COLUMN_NAME_2, oper, "true"),
              DimensionType.BOOLEAN,
              TABLE));
      final CalciteRequest request = builder.build();
      final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
      // convert != in other standard format <>
      final String sqlOperator = oper.toString().equals("!=") ? "<>" : oper.toString();
      final String expected = String.format(
          "SELECT \"%s\" FROM \"%s\".\"%s\" WHERE (\"%s\".\"%s\" %s %s)",
          COLUMN_NAME_1,
          DATABASE,
          TABLE,
          TABLE,
          COLUMN_NAME_2,
          sqlOperator,
          "TRUE");

      assertEquivalent(output, expected);
    }
  }

  @Test
  public void testGetSqlWithInPredicates() {
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(QueryPredicate.of(new Predicate(COLUMN_NAME_2,
            OPER.IN,
            new String[]{"val1", "val2"}), DimensionType.STRING, TABLE));
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format(
        "SELECT \"%s\" FROM \"%s\".\"%s\" WHERE (\"%s\".\"%s\" IN (%s, %s))",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        TABLE,
        COLUMN_NAME_2,
        "'val1'",
        "'val2'");

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithFreeTextPredicate() {
    final String complexWhere = "complexFunction(col2, col3, 10) >= 27";
    final String complexWhereQuotedIdentifiers = "\"complexFunction\"(\"col2\", \"col3\", 10) >= 27";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(complexWhere);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT \"%s\" FROM \"%s\".\"%s\" WHERE %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        complexWhereQuotedIdentifiers);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithFreeTextPredicateRemovingStartingAnd() {
    final String complexWhere = "complexFunction(col2, col3, 10) >= 27 OR colX = TRUE";
    final String complexWhereWithStartingAnd = "AND " + complexWhere;
    final String complexWhereQuotedIdentifiers = "\"complexFunction\"(\"col2\", \"col3\", 10) >= 27 OR \"colX\" = TRUE";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .addPredicate(complexWhereWithStartingAnd);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    final String expected = String.format("SELECT \"%s\" FROM \"%s\".\"%s\" WHERE %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        complexWhereQuotedIdentifiers);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextAndCalcitePredicateAndTimeFilter() {
    final String textPredicate = "col2 = 'test2'";
    final SqlNode sqlNodePredicate = new SqlBasicCall(EQUALS_OPERATOR,
        List.of(identifierOf("col3"), stringLiteralOf("test3")),
        SqlParserPos.ZERO);
    final Interval timeFilterInterval = new Interval(100L, 100000L, DateTimeZone.UTC);
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
        "SELECT \"%s\" FROM \"%s\".\"%s\" WHERE \"%s\" >= %s AND \"%s\" < %s AND (\"col1\" = 'test1') AND \"col2\" = 'test2' AND (\"col3\" = 'test3')",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        epoch_date,
        timeFilterInterval.getStartMillis(),
        epoch_date,
        timeFilterInterval.getEndMillis());

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithTimeAggregationOnEpochSeconds() {
    final String timeAggregationColumn = "date_epoch";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeAggregation(Period.days(7),
            timeAggregationColumn,
            "EPOCH",
            "SECONDS",
            false,
            null);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"%s\", \"DATETIMECONVERT\"(\"%s\", '1:SECONDS:EPOCH', '1:MILLISECONDS:EPOCH', '%s') AS \"%s\" FROM \"%s\".\"%s\" GROUP BY \"%s\"",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "7:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithTimeAggregationOnEpochMilliSeconds() {
    final String timeAggregationColumn = "date_epoch";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeAggregation(Period.days(7),
            timeAggregationColumn,
            "EPOCH",
            "MILLISECONDS",
            false,
            null);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"%s\", \"DATETIMECONVERT\"(\"%s\", '1:MILLISECONDS:EPOCH', '1:MILLISECONDS:EPOCH', '%s') AS \"%s\" FROM \"%s\".\"%s\" GROUP BY \"%s\"",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "7:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithTimeAggregationOnSimpleDateFormat() {
    final String timeAggregationColumn = "date_sdf";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeAggregation(Period.days(7),
            timeAggregationColumn,
            "SIMPLE_DATE_FORMAT:yyyyMMdd",
            null,
            false,
            null);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"%s\", \"DATETIMECONVERT\"(\"%s\", '1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd', '1:MILLISECONDS:EPOCH', '%s') AS \"%s\" FROM \"%s\".\"%s\" GROUP BY \"%s\"",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "7:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithTimeAggregationWithOrderOnTimeAggregation() {
    final String timeAggregationColumn = "date_epoch";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        // null timeFormat defaults to milliseconds
        .withTimeAggregation(Period.days(7),
            timeAggregationColumn,
            "EPOCH",
            "MILLISECONDS",
            true,
            null);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"%s\", \"DATETIMECONVERT\"(\"%s\", '1:MILLISECONDS:EPOCH', '1:MILLISECONDS:EPOCH', '%s') AS \"%s\" FROM \"%s\".\"%s\" GROUP BY \"%s\" ORDER BY \"%s\"",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "7:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS,
        TIME_AGGREGATION_ALIAS);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithTimeFilter() {
    final String timeAggregationColumn = "date_epoch";
    final Interval timeFilterInterval = new Interval(100L, 100000000L, DateTimeZone.UTC);
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeFilter(timeFilterInterval, timeAggregationColumn, "EPOCH", "MILLISECONDS");
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"%s\" FROM \"%s\".\"%s\" WHERE \"%s\" >= %s AND \"%s\" < %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        timeAggregationColumn,
        timeFilterInterval.getStartMillis(),
        timeAggregationColumn,
        timeFilterInterval.getEndMillis());

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithTimeFilterWithReservedKeyword() {
    final String reservedKeywordTimeAggregationColumn = "date";
    final String quotedTimeAggregationColumn =
        TestPinotLikeSqlLanguage.IDENTIFIER_QUOTE_STRING + reservedKeywordTimeAggregationColumn
            + TestPinotLikeSqlLanguage.IDENTIFIER_QUOTE_STRING;
    final Interval timeFilterInterval = new Interval(100L, 100000000L, DateTimeZone.UTC);
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeFilter(timeFilterInterval,
            reservedKeywordTimeAggregationColumn,
            "EPOCH",
            "MILLISECONDS");
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"%s\" FROM \"%s\".\"%s\" WHERE %s >= %s AND %s < %s",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        quotedTimeAggregationColumn,
        timeFilterInterval.getStartMillis(),
        quotedTimeAggregationColumn,
        timeFilterInterval.getEndMillis());

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithTimeFilterInSimpleDateFormat() {
    final String timeAggregationColumn = "date_sdf";
    final Interval timeFilterInterval = new Interval(new DateTime(2020,
        1,
        1,
        0,
        0,
        DateTimeZone.UTC), new DateTime(2021, 10, 10, 0, 0, DateTimeZone.UTC));
    final String simpleDateFormat = "yyyyMMdd";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withTimeFilter(timeFilterInterval, timeAggregationColumn, simpleDateFormat, null);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT \"%s\" FROM \"%s\".\"%s\" WHERE \"%s\" >= '%s' AND \"%s\" < '%s'",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        timeAggregationColumn,
        "20200101",
        timeAggregationColumn,
        "20211010");

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithTimeAggregationAndTimeFilter() {
    final String timeAggregationColumn = "date_sdf";
    final Interval timeFilterInterval = new Interval(100L,
        100000000L,
        DateTimeZone.UTC); // 19700101 - 19700102
    final String timeColumnFormat = "yyyyMMdd";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION)
        .withTimeAggregation(Period.days(1),
            timeAggregationColumn,
            timeColumnFormat,
            null,
            true,
            null)
        // timeFormat and unit is not used because the filtering will use the buckets in epoch millis
        .withTimeFilter(timeFilterInterval, timeAggregationColumn, timeColumnFormat, "DAYS");
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT SUM(\"%s\"), \"DATETIMECONVERT\"(\"%s\", '1:DAYS:SIMPLE_DATE_FORMAT:%s', '1:MILLISECONDS:EPOCH', '%s') AS \"%s\" FROM \"%s\".\"%s\" WHERE \"%s\" >= '%s' AND \"%s\" < '%s' GROUP BY \"%s\" ORDER BY \"%s\"",
        COLUMN_NAME_1,
        timeAggregationColumn,
        timeColumnFormat,
        "1:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        timeAggregationColumn,
        "19700101",
        timeAggregationColumn,
        "19700102",
        TIME_AGGREGATION_ALIAS,
        TIME_AGGREGATION_ALIAS);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextAndCalciteGroupByAndTimeAggregation() {
    // only one test for group by - test everything at once
    final String timeAggregationColumn = "date_epoch";
    final String timeColumnFormat = "yyyyMMdd";
    final String freeTextProjection = "MOD(" + COLUMN_NAME_1 + ", " + COLUMN_NAME_2 + ") AS mod1";
    final String freeTextProjectionQuotedIdentifiers =
        "MOD(\"" + COLUMN_NAME_1 + "\", \"" + COLUMN_NAME_2 + "\") AS \"mod1\"";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION)
        .addSelectProjection(QueryProjection.of(COLUMN_NAME_2))
        .addSelectProjection(freeTextProjection)
        .withTimeAggregation(Period.days(1),
            timeAggregationColumn,
            timeColumnFormat,
            null,
            true,
            null)
        // missing projection on col3 : because group by col3 but don't select it for test purpose
        .addGroupByProjection(QueryProjection.of(COLUMN_NAME_2))
        .addGroupByProjection(freeTextProjection)
        .addGroupByProjection(identifierOf(COLUMN_NAME_3));
    // timeFormat and unit is not used because the filtering will use the buckets in epoch millis
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT SUM(\"%s\"), \"%s\", %s, \"DATETIMECONVERT\"(\"%s\", '1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd', '1:MILLISECONDS:EPOCH', '%s') AS \"%s\" FROM \"%s\".\"%s\" GROUP BY \"%s\", %s, \"%s\", \"%s\" ORDER BY \"%s\"",
        COLUMN_NAME_1,
        COLUMN_NAME_2,
        freeTextProjectionQuotedIdentifiers,
        timeAggregationColumn,
        "1:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        COLUMN_NAME_2,
        freeTextProjectionQuotedIdentifiers,
        COLUMN_NAME_3,
        TIME_AGGREGATION_ALIAS,
        TIME_AGGREGATION_ALIAS);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGroupByWithHaving() {
    final CalciteRequest calciteRequest = CalciteRequest.newBuilder("table")
        .addSelectProjection("dimension")
        .addSelectProjection(QueryProjection.of("SUM", List.of("metric")).withAlias("agg"))
        .addGroupByProjection("dimension")
        .having(QueryPredicate.of(Predicate.GT("agg", "3"), DimensionType.NUMERIC))
        .build();
    final String sql = calciteRequest.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);
    assertEquivalent(sql,
        "SELECT SUM(\"metric\") AS \"agg\", \"dimension\" FROM \"table\" "
            + "GROUP BY \"dimension\" HAVING \"agg\" > 3"
    );
  }

  @Test
  public void testGetSqlWithStructuredAndFreeTextAndCalciteOrderByAndTimeAggregationOrderBy() {
    // only one test for group by - test everything at once
    final String timeAggregationColumn = "date_epoch";
    final String timeColumnFormat = "yyyyMMdd";
    final String freeTextProjection = "MOD(" + COLUMN_NAME_1 + ", " + COLUMN_NAME_2 + ")";
    final String freeTextProjectionQuotedIdentifiers =
        "MOD(\"" + COLUMN_NAME_1 + "\", \"" + COLUMN_NAME_2 + "\")";
    final String notProjectedColumn = "not_projected_column";
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(STANDARD_AGGREGATION_PROJECTION)
        .withTimeAggregation(Period.days(1),
            timeAggregationColumn,
            timeColumnFormat,
            null,
            true,
            null)
        // missing projection on col3 : because group by col3 but don't select it for test purpose
        .addOrderByProjection(QueryProjection.of(COLUMN_NAME_2))
        // test desc order
        .addOrderByProjection(QueryProjection.of(notProjectedColumn).withDescOrder())
        .addOrderByProjection(freeTextProjection)
        .addOrderByProjection(identifierOf(COLUMN_NAME_3));
    // timeFormat and unit is not used because the filtering will use the buckets in epoch millis
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format(
        "SELECT SUM(\"%s\"), \"DATETIMECONVERT\"(\"%s\", '1:DAYS:SIMPLE_DATE_FORMAT:yyyyMMdd', '1:MILLISECONDS:EPOCH', '%s') AS \"%s\" FROM \"%s\".\"%s\" GROUP BY \"%s\" ORDER BY \"%s\", \"%s\" DESC, %s, \"%s\", \"%s\"",
        COLUMN_NAME_1,
        timeAggregationColumn,
        "1:DAYS",
        TIME_AGGREGATION_ALIAS,
        DATABASE,
        TABLE,
        TIME_AGGREGATION_ALIAS,
        COLUMN_NAME_2,
        notProjectedColumn,
        freeTextProjectionQuotedIdentifiers,
        COLUMN_NAME_3,
        TIME_AGGREGATION_ALIAS);

    assertEquivalent(output, expected);
  }

  @Test
  public void testGetSqlWithLimit() {
    final int limit = 1000;
    final CalciteRequest.Builder builder = new CalciteRequest.Builder(TABLE).withDatabase(DATABASE)
        .addSelectProjection(SIMPLE_PROJECTION)
        .withLimit(limit);
    final CalciteRequest request = builder.build();
    final String output = request.getSql(SQL_LANGUAGE, SQL_EXPRESSION_BUILDER);

    final String expected = String.format("SELECT \"%s\" FROM \"%s\".\"%s\" FETCH NEXT %s ROWS ONLY",
        COLUMN_NAME_1,
        DATABASE,
        TABLE,
        limit);

    assertEquivalent(output, expected);
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

    private static final ThirdEyeSqlParserConfig SQL_PARSER_CONFIG = new ThirdEyeSqlParserConfig.Builder().withLex(
        "MYSQL_ANSI").withConformance("BABEL").withParserFactory("SqlBabelParserImpl").build();

    private static final ThirdeyeSqlDialect SQL_DIALECT = new ThirdeyeSqlDialect.Builder().withBaseDialect(
            "AnsiSqlDialect")
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

    public static final long SECOND_MILLIS = 1000; // number of milliseconds in a second
    public static final long MINUTE_MILLIS =
        60 * SECOND_MILLIS; // number of milliseconds in a minute
    public static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    public static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    public static final String STRING_LITERAL_QUOTE = "'";
    public static final String ESCAPED_STRING_LITERAL_QUOTE = "''";
    private static final Map<Period, String> DATE_TRUNC_COMPATIBLE_PERIOD_TO_DATE_TRUNC_STRING = Map.of(
        Period.years(1),
        "year",
        Period.months(1),
        "month",
        Period.weeks(1),
        "week",
        Period.days(1),
        "day",
        Period.hours(1),
        "hour",
        Period.minutes(1),
        "minute",
        Period.seconds(1),
        "second",
        Period.millis(1),
        "millisecond");
    private static final List<Period> DATE_TRUNC_COMPATIBLE_PERIODS = List.copyOf(
        DATE_TRUNC_COMPATIBLE_PERIOD_TO_DATE_TRUNC_STRING.keySet());
    private static final String PERCENTILE_TDIGEST_PREFIX = "PERCENTILETDigest";

    private static String escapeLiteralQuote(String s) {
      return s.replace(STRING_LITERAL_QUOTE, ESCAPED_STRING_LITERAL_QUOTE);
    }

    @NonNull
    @VisibleForTesting
    protected static String removeSimpleDateFormatPrefix(final String timeColumnFormat) {
      // remove (1:DAYS:)SIMPLE_DATE_FORMAT:
      return timeColumnFormat.replaceFirst("^([0-9]:[A-Z]+:)?SIMPLE_DATE_FORMAT:", "");
    }

    @Override
    public String getTimeFilterExpression(final String timeColumn, final Interval filterInterval,
        @NonNull final String timeColumnFormat) {
      final TimeFormat timeFormat = new TimeFormat(timeColumnFormat);

      return String.format("%s >= %s AND %s < %s",
          timeColumn,
          timeFormat.timeFormatter.apply(filterInterval.getStart()),
          timeColumn,
          timeFormat.timeFormatter.apply(filterInterval.getEnd()));
    }

    @Override
    public String getTimeFilterExpression(final String timeColumn, final Interval filterInterval,
        @Nullable final String timeFormat, @Nullable final String timeUnit) {
      if (timeFormat == null) {
        return getTimeFilterExpression(timeColumn, filterInterval, "EPOCH_MILLIS");
      }
      if ("EPOCH".equals(timeFormat)) {
        Objects.requireNonNull(timeUnit);
        return getTimeFilterExpression(timeColumn, filterInterval, "1:" + timeUnit + ":EPOCH");
      }
      // case simple date format
      return getTimeFilterExpression(timeColumn, filterInterval, timeFormat);
    }

    @Override
    public String getTimeGroupExpression(final String timeColumn, final @Nullable String timeFormat,
        final Period granularity, final @Nullable String timeUnit,
        @Nullable final String timezone) {
      if (timeFormat == null) {
        return getTimeGroupExpression(timeColumn, "EPOCH_MILLIS", granularity, timezone);
      }
      if ("EPOCH".equals(timeFormat)) {
        Objects.requireNonNull(timeUnit);
        return getTimeGroupExpression(timeColumn,
            "1:" + timeUnit + ":EPOCH",
            granularity,
            timezone);
      }
      // case simple date format
      return getTimeGroupExpression(timeColumn, timeFormat, granularity, timezone);
    }

    @Override
    public String getTimeGroupExpression(String timeColumn, @NonNull String timeColumnFormat,
        Period granularity, @Nullable final String timezone) {
      final TimeFormat timeFormat = new TimeFormat(timeColumnFormat);
      if (timezone == null || UTC_LIKE_TIMEZONES.contains(timezone)) {
        return String.format(" DATETIMECONVERT(%s, '%s', '1:MILLISECONDS:EPOCH', '%s') ",
            timeColumn,
            escapeLiteralQuote(timeFormat.dateTimeConvertString),
            periodToDateTimeConvertFormat(granularity));
      }

      if (timeFormat.isEpochFormat && DATE_TRUNC_COMPATIBLE_PERIODS.contains(granularity)) {
        // optimized expression for client use case - can be removed once https://github.com/apache/pinot/issues/8581 is closed
        return String.format(" DATETRUNC('%s', %s, '%s', '%s', 'MILLISECONDS') ",
            DATE_TRUNC_COMPATIBLE_PERIOD_TO_DATE_TRUNC_STRING.get(granularity),
            timeColumn,
            timeFormat.dateTruncString,
            timezone);
      }

      // workaround to bucket with a custom timezone - see https://github.com/apache/pinot/issues/8581
      return String.format(
          "FromDateTime(DATETIMECONVERT(%s, '%s', '1:DAYS:SIMPLE_DATE_FORMAT:yyyy-MM-dd HH:mm:ss.SSSZ tz(%s)', '%s'), 'yyyy-MM-dd HH:mm:ss.SSSZ') ",
          timeColumn,
          escapeLiteralQuote(timeFormat.dateTimeConvertString),
          timezone,
          periodToDateTimeConvertFormat(granularity));
    }

    private String periodToDateTimeConvertFormat(final Period period) {
      // see https://docs.pinot.apache.org/configuration-reference/functions/datetimeconvert
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
    public boolean needsCustomDialect(final MetricAggFunction metricAggFunction) {
      return SqlExpressionBuilder.super.needsCustomDialect(metricAggFunction);
    }

    @Override
    public String getCustomDialectSql(final MetricAggFunction metricAggFunction,
        final List<String> operands, final String quantifier) {
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
          return new StringBuilder().append(PERCENTILE_TDIGEST_PREFIX)
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

    /**
     * Class that allows to match multiple user facing time format strings to a given time format.
     * Can return the timeformat for different Pinot functions.
     */
    private static class TimeFormat {

      private final String dateTimeConvertString;
      private final String dateTruncString;
      private final boolean isEpochFormat;
      private final Function<DateTime, String> timeFormatter;

      TimeFormat(String userFacingTimeColumnFormat) {
        switch (userFacingTimeColumnFormat) {
          case "EPOCH_MILLIS":
          case "1:MILLISECONDS:EPOCH":
            dateTimeConvertString = "1:MILLISECONDS:EPOCH";
            dateTruncString = "MILLISECONDS";
            isEpochFormat = true;
            timeFormatter = d -> String.valueOf(d.getMillis());
            break;
          case "EPOCH":
          case "1:SECONDS:EPOCH":
            dateTimeConvertString = "1:SECONDS:EPOCH";
            dateTruncString = "SECONDS";
            isEpochFormat = true;
            timeFormatter = d -> String.valueOf(d.getMillis() / SECOND_MILLIS);
            break;
          case "EPOCH_MINUTES":
          case "1:MINUTES:EPOCH":
            dateTimeConvertString = "1:MINUTES:EPOCH";
            dateTruncString = "MINUTES";
            isEpochFormat = true;
            timeFormatter = d -> String.valueOf(d.getMillis() / MINUTE_MILLIS);
            break;
          case "EPOCH_HOURS":
          case "1:HOURS:EPOCH":
            dateTimeConvertString = "1:HOURS:EPOCH";
            dateTruncString = "HOURS";
            isEpochFormat = true;
            timeFormatter = d -> String.valueOf(d.getMillis() / HOUR_MILLIS);
            break;
          case "EPOCH_DAYS":
          case "1:DAYS:EPOCH":
            dateTimeConvertString = "1:DAYS:EPOCH";
            dateTruncString = "DAYS";
            isEpochFormat = true;
            timeFormatter = d -> String.valueOf(d.getMillis() / DAY_MILLIS);
            break;
          default:
            // assume simple date format
            final String cleanSimpleDateFormat = removeSimpleDateFormatPrefix(
                userFacingTimeColumnFormat);
            // fail if invalid format
            new SimpleDateFormat(cleanSimpleDateFormat);
            dateTimeConvertString = String.format("1:DAYS:SIMPLE_DATE_FORMAT:%s",
                cleanSimpleDateFormat);
            dateTruncString = null;
            isEpochFormat = false;
            timeFormatter = d -> {
              final DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(
                  cleanSimpleDateFormat).withChronology(d.getChronology());
              return STRING_LITERAL_QUOTE + inputDataDateTimeFormatter.print(d)
                  + STRING_LITERAL_QUOTE;
            };
        }
      }
    }
  }
}
