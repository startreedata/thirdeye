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

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.IntegrationTestUtils;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotSqlExpressionBuilder;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotSqlLanguage;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunction;
import ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.Test;

/**
 * Test if macro replacements are performed correctly.
 * Does not focus on final SQL correctness, only on parsing and replacement in tree.
 * Correct SQL is still required for initial AST parsing.
 * Correct SQL is not required in TestMacroManager expression generation methods, as long as the
 * result looks like a function call.
 *
 * Tests are performed with Pinot language implementation.
 */
public class MacroEngineTest {

  private static final String TABLE_NAME = "table";
  private static final SqlLanguage MOCK_SQL_LANGUAGE = new PinotSqlLanguage();
  private static final SqlExpressionBuilder MOCK_SQL_EXPRESSION_BUILDER = new PinotSqlExpressionBuilder();
  private static final long INPUT_START_TIME = 11111111L;
  private static final long INPUT_END_TIME = 22222222L;
  private static final Interval INPUT_INTERVAL = new Interval(INPUT_START_TIME,
      INPUT_END_TIME,
      DateTimeZone.UTC);
  private static final String INPUT_TIME_COLUMN_FORMAT = "EPOCH";
  private static final String IDENTIFIER_QUOTE_STRING = "\"";
  private static final String LITERAL_QUOTE_STRING = "'";

  private static final TimeUnit DATASET_CONFIG_EPOCH_UNIT = TimeUnit.HOURS;

  private static final DatasetConfigDTO DATASET_CONFIG_DTO = new DatasetConfigDTO().setDataset(
          TABLE_NAME)
      .setTimeColumn("defaultCol")
      .setTimeFormat(INPUT_TIME_COLUMN_FORMAT)
      .setTimeUnit(DATASET_CONFIG_EPOCH_UNIT);
  public static final String SIMPLE_TIME_FORMAT = "dd-M-yyyy hh:mm:ss";
  public static final Period HOUR_PERIOD = Period.hours(1);

  private void prepareRequestAndAssert(final String inputQuery, final Interval detectionInterval,
      final String expectedQuery,
      final Map<String, String> expectedProperties) {
    final MacroEngine macroEngine = new MacroEngine(MOCK_SQL_LANGUAGE,
        MOCK_SQL_EXPRESSION_BUILDER,
        detectionInterval,
        DATASET_CONFIG_DTO,
        inputQuery);
    final DataSourceRequest output = macroEngine.prepareRequest();
    assertThat(TABLE_NAME).isEqualTo(output.getTable());
    assertThat(IntegrationTestUtils.cleanSql(output.getQuery())).isEqualTo(
        IntegrationTestUtils.cleanSql(
            expectedQuery));
    assertThat(expectedProperties).isEqualTo(output.getProperties());
  }

  @Test
  public void testTimeFilterMacro() {
    // test if a simple macro works - eg: __timeFilter(timeCol, 'EPOCH')
    final String macroArgument = "timeCol";
    final String inputQuery = String.format("select * from tableName where __timeFilter(%s, '%s')",
        macroArgument,
        INPUT_TIME_COLUMN_FORMAT);

    final String expectedQuery = String.format("SELECT * FROM tableName WHERE %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_INTERVAL,
            INPUT_TIME_COLUMN_FORMAT));
    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(),
        String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(),
        String.valueOf(INPUT_END_TIME));

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeFilterMacroWithAutoTimeConfig() {
    final String inputQuery = String.format("select * from tableName where __timeFilter(%s, '%s')",
        MacroFunction.AUTO_TIME_CONFIG,
        "NOT_IMPORTANT_SHOULD_NOT_BE_USED");

    final String expectedQuery = String.format("SELECT * FROM tableName WHERE %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(
            "\"" + DATASET_CONFIG_DTO.getTimeColumn() + "\"",
            INPUT_INTERVAL,
            DATASET_CONFIG_DTO.getTimeFormat(),
            DATASET_CONFIG_DTO.getTimeUnit().toString()));

    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(),
        String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(),
        String.valueOf(INPUT_END_TIME));

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeFilterMacroWithFunctionCallInArgument() {
    // test if a function call inside a macro works - eg: __timeFilter(unixTimestamp(timeCol), 'EPOCH')
    final String macroArgument = "unixTimestamp(timeCol)";

    final String inputQuery = String.format("select * from tableName where __timeFilter(%s, '%s')",
        macroArgument,
        INPUT_TIME_COLUMN_FORMAT);

    final String expectedQuery = String.format("SELECT * FROM tableName WHERE %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_INTERVAL,
            INPUT_TIME_COLUMN_FORMAT));
    final Map<String, String> expectedProperties = ImmutableMap.of(
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
    final String timeColumnMacroArg = "timeCol";
    final String timeColumnFormatMacroArg = SIMPLE_TIME_FORMAT;
    final Period granularityMacroArg = HOUR_PERIOD;

    final String inputQuery = String.format("select __timeGroup(%s,'%s','%s') from tableName",
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg);

    final String expectedQuery = String.format("SELECT %s FROM tableName",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(timeColumnMacroArg,
            timeColumnFormatMacroArg,
            granularityMacroArg,
            INPUT_INTERVAL.getChronology().getZone().toString()));

    final Map<String, String> expectedProperties = ImmutableMap.of(MacroMetadataKeys.GRANULARITY.toString(),
        HOUR_PERIOD.toString());

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacroWithAutoTimeConfig() {
    final String inputQuery = String.format("select __timeGroup(%s,'%s','%s') from tableName",
        MacroFunction.AUTO_TIME_CONFIG,
        "NOT_IMPORTANT_SHOULD_NOT_BE_USED",
        HOUR_PERIOD);

    final String expectedQuery = String.format("SELECT %s FROM tableName",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(
            "\"" + DATASET_CONFIG_DTO.getTimeColumn() + "\"",
            DATASET_CONFIG_DTO.getTimeFormat(),
            HOUR_PERIOD,
            DATASET_CONFIG_DTO.getTimeUnit().toString(),
            INPUT_INTERVAL.getChronology().getZone().toString()));

    final Map<String, String> expectedProperties = ImmutableMap.of(MacroMetadataKeys.GRANULARITY.toString(),
        HOUR_PERIOD.toString());

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacroWithQuotedLiterals() {
    // test if a macro with string literal params is parsed correctly
    final String timeColumnMacroArg = "timeCol";
    final String timeColumnFormatMacroArg = SIMPLE_TIME_FORMAT;
    final String timeColumnFormatMacroArgQuoted =
        LITERAL_QUOTE_STRING + timeColumnFormatMacroArg + LITERAL_QUOTE_STRING;
    final Period granularityMacroArg = HOUR_PERIOD;
    final String granularityMacroArgQuoted = LITERAL_QUOTE_STRING + HOUR_PERIOD + LITERAL_QUOTE_STRING;

    final String inputQuery = String.format("select __timeGroup(%s,%s,%s) FROM tableName",
        timeColumnMacroArg,
        timeColumnFormatMacroArgQuoted,
        granularityMacroArgQuoted);

    final String expectedQuery = String.format("SELECT %s FROM tableName",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(timeColumnMacroArg,
            timeColumnFormatMacroArg,
            granularityMacroArg,
            INPUT_INTERVAL.getChronology().getZone().toString()));

    final Map<String, String> expectedProperties = ImmutableMap.of(MacroMetadataKeys.GRANULARITY.toString(),
        HOUR_PERIOD.toString());

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacroWithCustomTimeZone() {
    // test if a macro with string literal params is parsed correctly
    final String timeColumnMacroArg = "timeCol";
    final String timeColumnFormatMacroArg = SIMPLE_TIME_FORMAT;
    final String timeZone = "Europe/Amsterdam";
    final String timeColumnFormatMacroArgQuoted =
        LITERAL_QUOTE_STRING + timeColumnFormatMacroArg + LITERAL_QUOTE_STRING;
    final Period granularityMacroArg = HOUR_PERIOD;
    final String granularityMacroArgQuoted =
        LITERAL_QUOTE_STRING + HOUR_PERIOD + LITERAL_QUOTE_STRING;

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
        HOUR_PERIOD.toString());

    prepareRequestAndAssert(inputQuery,
        INPUT_INTERVAL.withChronology(INPUT_INTERVAL.getChronology()
            .withZone(DateTimeZone.forID(timeZone))),
        expectedQuery,
        expectedProperties);
  }

  @Test
  public void testNestedMacro() {
    // test if nested macros work - eg: __timeFilter(__timeGroup(timeCol, myTestFormat, P0D), 'EPOCH')
    final String timeColumnMacroArg = "timeCol";
    final String timeColumnFormatMacroArg = SIMPLE_TIME_FORMAT;
    final Period granularityMacroArg = HOUR_PERIOD;

    final String inputQuery = String.format(
        "select * from tableName where __timeFilter(__timeGroup(%s,'%s','%s'), '%s')",
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg,
        INPUT_TIME_COLUMN_FORMAT);

    final String expectedTimeGroupMacro = MOCK_SQL_EXPRESSION_BUILDER.getTimeGroupExpression(
        timeColumnMacroArg,
        timeColumnFormatMacroArg,
        granularityMacroArg,
        INPUT_INTERVAL.getChronology().getZone().toString());
    final String expectedNestedMacro = MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(
        expectedTimeGroupMacro,
        INPUT_INTERVAL, INPUT_TIME_COLUMN_FORMAT);
    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(),
        String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(),
        String.valueOf(INPUT_END_TIME),
        MacroMetadataKeys.GRANULARITY.toString(),
        HOUR_PERIOD.toString());

    final String expectedQuery = String.format("SELECT * FROM tableName WHERE %s", expectedNestedMacro);

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testIdentifierQuotesAreConserved() {
    // test if escaping quotes are kept, eg: __timeFilter("date", 'EPOCH') returns "date" >= ...
    final String macroArgument = IDENTIFIER_QUOTE_STRING + "date" + IDENTIFIER_QUOTE_STRING;
    final String inputQuery = String.format("select * from tableName where __timeFilter(%s, '%s')",
        macroArgument,
        INPUT_TIME_COLUMN_FORMAT);

    final String expectedQuery = String.format("SELECT * FROM tableName WHERE %s",
        MOCK_SQL_EXPRESSION_BUILDER.getTimeFilterExpression(macroArgument,
            INPUT_INTERVAL,
            INPUT_TIME_COLUMN_FORMAT));
    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.MIN_TIME_MILLIS.toString(),
        String.valueOf(INPUT_START_TIME),
        MacroMetadataKeys.MAX_TIME_MILLIS.toString(),
        String.valueOf(INPUT_END_TIME));

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }
}
