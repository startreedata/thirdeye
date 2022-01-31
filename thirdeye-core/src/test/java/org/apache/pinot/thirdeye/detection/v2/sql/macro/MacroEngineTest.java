package org.apache.pinot.thirdeye.detection.v2.sql.macro;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequestV2;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroMetadataKeys;
import org.apache.pinot.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import org.apache.pinot.thirdeye.spi.datasource.macro.SqlLanguage;
import org.apache.pinot.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import org.apache.pinot.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;
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
  private static final Interval INPUT_INTERVAL = new Interval(INPUT_START_TIME, INPUT_END_TIME);
  private static final String IDENTIFIER_QUOTE_STRING = "\"";
  private static final String LITERAL_QUOTE_STRING = "'";

  private String clean(String sql) {
    return sql
        .toLowerCase(Locale.ENGLISH)
        .trim()
        .replaceAll("[\\n\\t\\r]+", " ")
        .replaceAll(" +", " ")
        ;
  }

  private void prepareRequestAndAssert(String inputQuery, String expectedQuery,
      Map<String, String> expectedProperties) {
    MacroEngine macroEngine = new MacroEngine(MOCK_SQL_LANGUAGE,
        MOCK_SQL_EXPRESSION_BUILDER,
        INPUT_INTERVAL,
        TABLE_NAME,
        inputQuery);
    try {
      ThirdEyeRequestV2 output = macroEngine.prepareRequest();
      Assert.assertEquals(TABLE_NAME, output.getTable());
      Assert.assertEquals(clean(expectedQuery), clean(output.getQuery()));
      Assert.assertEquals(expectedProperties, output.getProperties());
    } catch (SqlParseException e) {
      Assert.fail("SQL query parsing failed: " + e);
    }
  }

  @Test
  public void testTimeFilterMacro() {
    // test if a simple macro works - eg: __timeFilter(timeCol)
    String macroArgument = "timeCol";
    String inputQuery = String.format("select * from tableName where __timeFilter(%s)",
        macroArgument);

    String expectedQuery = String.format("select * from tableName where %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_START_TIME,
            INPUT_END_TIME));
    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.TIME_COLUMN.toString(), macroArgument,
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), String.valueOf(INPUT_END_TIME)
    );

    prepareRequestAndAssert(inputQuery, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeFilterMacroWithFunctionCallInArgument() {
    // test if a function call inside a macro works - eg: __timeFilter(unixTimestamp(timeCol))
    String macroArgument = "unixTimestamp(timeCol)";

    String inputQuery = String.format("select * from tableName where __timeFilter(%s)",
        macroArgument);

    String expectedQuery = String.format("select * from tableName where %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_START_TIME,
            INPUT_END_TIME));
    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.TIME_COLUMN.toString(), macroArgument,
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), String.valueOf(INPUT_END_TIME)
    );

    prepareRequestAndAssert(inputQuery, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacro() {
    // test if a macro with multiple arguments work - eg: __timeGroup(timeCol, myTestFormat, P0D)
    String timeColumnMacroArg = "timeCol";
    String timeColumnFormatMacroArg = "myTestFormat";
    Period granularityMacroArg = Period.ZERO;

    String inputQuery = String.format("select __timeGroup(%s,%s,%s) from tableName",
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg);

    String expectedQuery = String.format("select %s from tableName",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(timeColumnMacroArg,
            timeColumnFormatMacroArg,
            granularityMacroArg));

    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(), Period.ZERO.toString()
    );

    prepareRequestAndAssert(inputQuery, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacroWithQuotedLiterals() {
    // test if a macro with string literal params is parsed correctly
    String timeColumnMacroArg = "timeCol";
    String timeColumnFormatMacroArg = "myTestFormat";
    String timeColumnFormatMacroArgQuoted =
        LITERAL_QUOTE_STRING + timeColumnFormatMacroArg + LITERAL_QUOTE_STRING;
    Period granularityMacroArg = Period.ZERO;
    String granularityMacroArgQuoted =
        LITERAL_QUOTE_STRING + Period.ZERO + LITERAL_QUOTE_STRING;

    String inputQuery = String.format("select __timeGroup(%s,%s,%s) from tableName",
        timeColumnMacroArg,
        timeColumnFormatMacroArgQuoted,
        granularityMacroArgQuoted);

    String expectedQuery = String.format("select %s from tableName",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(timeColumnMacroArg,
            timeColumnFormatMacroArg,
            granularityMacroArg));

    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(), Period.ZERO.toString()
    );

    prepareRequestAndAssert(inputQuery, expectedQuery, expectedProperties);
  }

  @Test
  public void testNestedMacro() {
    // test if nested macros work - eg: __timeFilter(__timeGroup(timeCol, myTestFormat, P0D))
    String timeColumnMacroArg = "timeCol";
    String timeColumnFormatMacroArg = "myTestFormat";
    Period granularityMacroArg = Period.ZERO;

    String inputQuery = String.format(
        "select * from tableName where __timeFilter(__timeGroup(%s,%s,%s))",
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg);

    String expectedTimeGroupMacro = MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg);
    String expectedNestedMacro = MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(
        expectedTimeGroupMacro,
        INPUT_START_TIME,
        INPUT_END_TIME);
    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.TIME_COLUMN.toString(), expectedTimeGroupMacro,
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), String.valueOf(INPUT_END_TIME),
        MacroMetadataKeys.GRANULARITY.toString(), Period.ZERO.toString()
    );

    String expectedQuery = String.format("select * from tableName where %s", expectedNestedMacro);

    prepareRequestAndAssert(inputQuery, expectedQuery, expectedProperties);
  }

  @Test
  public void testIdentifierQuotesAreConserved() {
    // test if escaping quotes are kept, eg: __timeFilter("date") returns "date" >= ...
    String macroArgument = IDENTIFIER_QUOTE_STRING + "date" + IDENTIFIER_QUOTE_STRING;
    String inputQuery = String.format("select * from tableName where __timeFilter(%s)",
        macroArgument);

    String expectedQuery = String.format("select * from tableName where %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_START_TIME,
            INPUT_END_TIME));
    Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.TIME_COLUMN.toString(), macroArgument,
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(), String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(), String.valueOf(INPUT_END_TIME)
    );

    prepareRequestAndAssert(inputQuery, expectedQuery, expectedProperties);
  }

  private static class TestSqlLanguage implements SqlLanguage {

    @Override
    public ThirdEyeSqlParserConfig getSqlParserConfig() {
      return new ThirdEyeSqlParserConfig.Builder()
          .withLex("MYSQL_ANSI")
          .withConformance("DEFAULT")
          .withParserFactory("SqlParserImpl")
          .build();
    }

    @Override
    public ThirdeyeSqlDialect getSqlDialect() {
      return new ThirdeyeSqlDialect.Builder()
          .withBaseDialect("AnsiSqlDialect")
          .withIdentifierQuoteString(IDENTIFIER_QUOTE_STRING)
          .withLiteralQuoteString(LITERAL_QUOTE_STRING)
          .build();
    }
  }

  private static class TestSqlExpressionBuilder implements SqlExpressionBuilder {

    public static final String TIME_GROUP_MOCK = "TIMEGROUP_MACRO_EXPANDED";
    public static final String TIME_FILTER_MOCK = "TIME_FILTER_MACRO_EXPANDED";

    @Override
    public String getTimeFilterExpression(final String column, final long minTimeMillisIncluded,
        final long maxTimeMillisExcluded) {
      return String.format("%s(%s, %s, %s)",
          TIME_FILTER_MOCK,
          column,
          minTimeMillisIncluded,
          maxTimeMillisExcluded);
    }

    @Override
    public String getTimeGroupExpression(final String timeColumn, final String timeColumnFormat,
        final Period granularity) {
      return String.format("%s(%s, %s, %s)",
          TIME_GROUP_MOCK,
          timeColumn,
          timeColumnFormat,
          granularity);
    }
  }
}
