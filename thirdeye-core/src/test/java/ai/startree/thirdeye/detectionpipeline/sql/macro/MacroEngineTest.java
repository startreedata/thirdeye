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
package ai.startree.thirdeye.detectionpipeline.sql.macro;

import ai.startree.thirdeye.spi.datasource.ThirdEyeRequestV2;
import ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import ai.startree.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;
import ai.startree.thirdeye.testutils.SqlUtils;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.calcite.sql.parser.SqlParseException;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * Test if macro replacements are performed correctly.
 * Does not focus on final SQL correctness, only on parsing and replacement in tree.
 * Correct SQL is still required for initial AST parsing.
 * Correct SQL is not required in TestMacroManager expression generation methods, as long as the
 * result looks like a function call.
 */
public class MacroEngineTest {

  private static final String TABLE_NAME = "table";
  private static final SqlLanguage MOCK_SQL_LANGUAGE = new TestSqlLanguage();
  private static final SqlExpressionBuilder MOCK_SQL_EXPRESSION_BUILDER = new TestSqlExpressionBuilder();
  private static final long INPUT_START_TIME = 11111111L;
  private static final long INPUT_END_TIME = 22222222L;
  private static final Interval INPUT_INTERVAL = new Interval(INPUT_START_TIME,
      INPUT_END_TIME,
      DateTimeZone.UTC);
  private static final String INPUT_TIME_COLUMN_FORMAT = "EPOCH";
  private static final String IDENTIFIER_QUOTE_STRING = "\"";
  private static final String LITERAL_QUOTE_STRING = "'";

  private void prepareRequestAndAssert(final String inputQuery, final Interval detectionInterval,
      final String expectedQuery,
      final Map<String, String> expectedProperties) {
    MacroEngine macroEngine = new MacroEngine(MOCK_SQL_LANGUAGE,
        MOCK_SQL_EXPRESSION_BUILDER,
        detectionInterval,
        TABLE_NAME,
        inputQuery);
    try {
      ThirdEyeRequestV2 output = macroEngine.prepareRequest();
      Assert.assertEquals(TABLE_NAME, output.getTable());
      Assertions.assertThat(SqlUtils.cleanSql(output.getQuery())).isEqualTo(SqlUtils.cleanSql(
          expectedQuery));
      Assert.assertEquals(expectedProperties, output.getProperties());
    } catch (SqlParseException e) {
      Assert.fail("SQL query parsing failed: " + e);
    }
  }

  @Test
  public void testTimeFilterMacro() {
    // test if a simple macro works - eg: __timeFilter(timeCol, 'EPOCH')
    String macroArgument = "timeCol";
    String inputQuery = String.format("select * from tableName where __timeFilter(%s, '%s')",
        macroArgument,
        INPUT_TIME_COLUMN_FORMAT);

    String expectedQuery = String.format("SELECT * FROM tableName WHERE %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_INTERVAL,
            INPUT_TIME_COLUMN_FORMAT));
    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(),
        String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(),
        String.valueOf(INPUT_END_TIME));

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeFilterMacroWithFunctionCallInArgument() {
    // test if a function call inside a macro works - eg: __timeFilter(unixTimestamp(timeCol), 'EPOCH')
    String macroArgument = "unixTimestamp(timeCol)";

    String inputQuery = String.format("select * from tableName where __timeFilter(%s, '%s')",
        macroArgument,
        INPUT_TIME_COLUMN_FORMAT);

    String expectedQuery = String.format("SELECT * FROM tableName WHERE %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_INTERVAL,
            INPUT_TIME_COLUMN_FORMAT));
    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(),
        String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(),
        String.valueOf(INPUT_END_TIME));

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacro() {
    // test if a macro with unquoted arguments work - eg: __timeGroup(timeCol, myTestFormat, P0D)
    // support for unquoted arguments is not mandatory and not documented - drop this test if need be
    String timeColumnMacroArg = "timeCol";
    String timeColumnFormatMacroArg = "myTestFormat";
    Period granularityMacroArg = Period.ZERO;

    String inputQuery = String.format("select __timeGroup(%s,%s,%s) from tableName",
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg);

    String expectedQuery = String.format("SELECT %s FROM tableName",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(timeColumnMacroArg,
            timeColumnFormatMacroArg,
            granularityMacroArg,
            INPUT_INTERVAL.getChronology().getZone().toString()));

    Map<String, String> expectedProperties = ImmutableMap.of(MacroMetadataKeys.GRANULARITY.toString(),
        Period.ZERO.toString());

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacroWithQuotedLiterals() {
    // test if a macro with string literal params is parsed correctly
    String timeColumnMacroArg = "timeCol";
    String timeColumnFormatMacroArg = "myTestFormat";
    String timeColumnFormatMacroArgQuoted =
        LITERAL_QUOTE_STRING + timeColumnFormatMacroArg + LITERAL_QUOTE_STRING;
    Period granularityMacroArg = Period.ZERO;
    String granularityMacroArgQuoted = LITERAL_QUOTE_STRING + Period.ZERO + LITERAL_QUOTE_STRING;

    String inputQuery = String.format("select __timeGroup(%s,%s,%s) FROM tableName",
        timeColumnMacroArg,
        timeColumnFormatMacroArgQuoted,
        granularityMacroArgQuoted);

    String expectedQuery = String.format("SELECT %s FROM tableName",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(timeColumnMacroArg,
            timeColumnFormatMacroArg,
            granularityMacroArg,
            INPUT_INTERVAL.getChronology().getZone().toString()));

    Map<String, String> expectedProperties = ImmutableMap.of(MacroMetadataKeys.GRANULARITY.toString(),
        Period.ZERO.toString());

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacroWithCustomTimeZone() {
    // test if a macro with string literal params is parsed correctly
    final String timeColumnMacroArg = "timeCol";
    final String timeColumnFormatMacroArg = "myTestFormat";
    final String timeZone = "Europe/Amsterdam";
    final String timeColumnFormatMacroArgQuoted =
        LITERAL_QUOTE_STRING + timeColumnFormatMacroArg + LITERAL_QUOTE_STRING;
    final Period granularityMacroArg = Period.ZERO;
    final String granularityMacroArgQuoted =
        LITERAL_QUOTE_STRING + Period.ZERO + LITERAL_QUOTE_STRING;

    final String inputQuery = String.format("select __timeGroup(%s,%s,%s) FROM tableName",
        timeColumnMacroArg,
        timeColumnFormatMacroArgQuoted,
        granularityMacroArgQuoted);

    final String expectedQuery = String.format("SELECT %s FROM tableName",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(timeColumnMacroArg,
            timeColumnFormatMacroArg,
            granularityMacroArg,
            timeZone));

    final Map<String, String> expectedProperties = ImmutableMap.of(MacroMetadataKeys.GRANULARITY.toString(),
        Period.ZERO.toString());

    prepareRequestAndAssert(inputQuery,
        INPUT_INTERVAL.withChronology(INPUT_INTERVAL.getChronology()
            .withZone(DateTimeZone.forID(timeZone))),
        expectedQuery,
        expectedProperties);
  }

  @Test
  public void testNestedMacro() {
    // test if nested macros work - eg: __timeFilter(__timeGroup(timeCol, myTestFormat, P0D), 'EPOCH')
    String timeColumnMacroArg = "timeCol";
    String timeColumnFormatMacroArg = "myTestFormat";
    Period granularityMacroArg = Period.ZERO;

    String inputQuery = String.format(
        "select * from tableName where __timeFilter(__timeGroup(%s,%s,%s), '%s')",
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg,
        INPUT_TIME_COLUMN_FORMAT);

    String expectedTimeGroupMacro = MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg,
        INPUT_INTERVAL.getChronology().getZone().toString());
    String expectedNestedMacro = MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(
        expectedTimeGroupMacro,
        INPUT_INTERVAL, INPUT_TIME_COLUMN_FORMAT);
    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(),
        String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(),
        String.valueOf(INPUT_END_TIME),
        MacroMetadataKeys.GRANULARITY.toString(),
        Period.ZERO.toString());

    String expectedQuery = String.format("SELECT * FROM tableName WHERE %s", expectedNestedMacro);

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testIdentifierQuotesAreConserved() {
    // test if escaping quotes are kept, eg: __timeFilter("date", 'EPOCH') returns "date" >= ...
    String macroArgument = IDENTIFIER_QUOTE_STRING + "date" + IDENTIFIER_QUOTE_STRING;
    String inputQuery = String.format("select * from tableName where __timeFilter(%s, '%s')",
        macroArgument,
        INPUT_TIME_COLUMN_FORMAT);

    String expectedQuery = String.format("SELECT * FROM tableName WHERE %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_INTERVAL,
            INPUT_TIME_COLUMN_FORMAT));
    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(),
        String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(),
        String.valueOf(INPUT_END_TIME));

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  private static class TestSqlLanguage implements SqlLanguage {

    @Override
    public ThirdEyeSqlParserConfig getSqlParserConfig() {
      return new ThirdEyeSqlParserConfig.Builder().withLex("MYSQL_ANSI")
          .withConformance("DEFAULT")
          .withParserFactory("SqlParserImpl")
          .build();
    }

    @Override
    public ThirdeyeSqlDialect getSqlDialect() {
      return new ThirdeyeSqlDialect.Builder().withBaseDialect("AnsiSqlDialect")
          .withIdentifierQuoteString(IDENTIFIER_QUOTE_STRING)
          .withLiteralQuoteString(LITERAL_QUOTE_STRING)
          .build();
    }
  }

  private static class TestSqlExpressionBuilder implements SqlExpressionBuilder {

    public static final String TIME_GROUP_MOCK = "TIMEGROUP_MACRO_EXPANDED";
    public static final String TIME_FILTER_MOCK = "TIME_FILTER_MACRO_EXPANDED";

    @Override
    public String getTimeFilterExpression(final String column, final Interval filterInterval,
        final String timeColumnFormat) {
      return String.format("%s(%s, %s, %s, %s)",
          TIME_FILTER_MOCK,
          column,
          timeColumnFormat,
          filterInterval.getStartMillis(),
          filterInterval.getEndMillis());
    }

    @Override
    public String getTimeGroupExpression(final String timeColumn, final String timeColumnFormat,
        final Period granularity, final String timezone) {
      if (timezone == null) {
        return String.format("%s(%s, '%s', '%s')",
            TIME_GROUP_MOCK,
            timeColumn,
            timeColumnFormat,
            granularity);
      }
      return String.format("%s(%s, '%s', '%s', '%s')",
          TIME_GROUP_MOCK,
          timeColumn,
          timeColumnFormat,
          granularity,
          timezone);
    }
  }
}
