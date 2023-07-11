/*
 * Copyright 2023 StarTree Inc
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
import ai.startree.thirdeye.spi.api.TimeColumnApi;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunction;
import ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.DataProvider;
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

  // todo cyril can be removed once all users are migrated to the new configuration
  private static final DatasetConfigDTO LEGACY_DATASET_CONFIG_DTO = new DatasetConfigDTO().setDataset(
          TABLE_NAME)
      .setTimeColumn("defaultCol")
      .setTimeFormat(INPUT_TIME_COLUMN_FORMAT)
      .setTimeUnit(DATASET_CONFIG_EPOCH_UNIT);
  private static final DatasetConfigDTO DATASET_CONFIG_DTO = new DatasetConfigDTO().setDataset(
          TABLE_NAME)
      .setTimeColumn("defaultCol")
      .setTimeFormat("1:HOURS:EPOCH");

  public static final String SIMPLE_TIME_FORMAT = "dd-M-yyyy hh:mm:ss";
  public static final Period HOUR_PERIOD = Period.hours(1);

  private static void prepareRequestAndAssert(final String inputQuery,
      final Interval detectionInterval,
      final String expectedQuery,
      final Map<String, String> expectedProperties, final DatasetConfigDTO datasetConfigDTO) {
    final MacroEngine macroEngine = new MacroEngine(MOCK_SQL_LANGUAGE,
        MOCK_SQL_EXPRESSION_BUILDER,
        detectionInterval,
        datasetConfigDTO,
        inputQuery);
    final DataSourceRequest output = macroEngine.prepareRequest();
    assertThat(output.getTable()).isEqualTo(TABLE_NAME);
    assertThat(IntegrationTestUtils.cleanSql(output.getQuery())).isEqualTo(
        IntegrationTestUtils.cleanSql(
            expectedQuery));
    assertThat(output.getProperties()).isEqualTo(expectedProperties);
  }

  private static void prepareRequestAndAssert(final String inputQuery,
      final Interval detectionInterval,
      final String expectedQuery,
      final Map<String, String> expectedProperties) {
    prepareRequestAndAssert(inputQuery, detectionInterval, expectedQuery, expectedProperties,
        LEGACY_DATASET_CONFIG_DTO);
    prepareRequestAndAssert(inputQuery, detectionInterval, expectedQuery, expectedProperties,
        DATASET_CONFIG_DTO);
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
            "\"" + LEGACY_DATASET_CONFIG_DTO.getTimeColumn() + "\"",
            INPUT_INTERVAL,
            LEGACY_DATASET_CONFIG_DTO.getTimeFormat()));

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

  @DataProvider(name = "optimizationNotPossible")
  public Object[][] optimizationNotPossible() {
    return new Object[][]{
        {"dd-MM-yyyy hh:mm", Period.hours(1)},
        {"EPOCH_MILLIS", Period.days(1)},
        // optimization should not happen if the time format is epoch long
        {"EPOCH_HOURS", Period.hours(1)},
        {"1:HOURS:EPOCH", Period.hours(1)},
        {"yyyy-MM-dd", Period.days(2)},
    };
  }

  @Test(dataProvider = "optimizationNotPossible")
  public void testTimeGroupKeyMacroOptimizationNotPossible(final String timeFormat,
      final Period granularity) {
    final String inputQuery = String.format(
        "SELECT timeConvert(timeCol) AS ts, COUNT(*) from tableName GROUP BY __timeGroupKey(timeCol, '%s', '%s', ts)",
        timeFormat,
        granularity);

    // GROUP BY ts --> no optimization happens
    final String expectedQuery = "SELECT timeConvert(timeCol) AS ts, COUNT(*) FROM tableName GROUP BY ts";
    final Map<String, String> expectedProperties = ImmutableMap.of();

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @DataProvider(name = "optimizationPossible")
  public Object[][] optimizationPossible() {
    return new Object[][]{
        {"dd-MM-yyyy hh", Period.hours(1)},
        {"yyyy-MM-dd", Period.days(1)},
        {"dd-MM-yyyy", Period.days(1)},
        {"1:DAYS:SIMPLE_DATE_FORMAT:dd-MM-yyyy hh", Period.hours(1)},
        {"1:DAYS:SIMPLE_DATE_FORMAT:yyyy-MM-dd", Period.days(1)},
    };
  }

  @Test(dataProvider = "optimizationPossible")
  public void testTimeGroupKeyMacroOptimizationPossible(final String timeFormat,
      final Period granularity) {
    final String inputQuery = String.format(
        "SELECT timeConvert(timeCol) AS ts, COUNT(*) from tableName GROUP BY __timeGroupKey(timeCol, '%s', '%s', ts)",
        timeFormat,
        granularity);

    // GROUP BY timeCol --> optimization happens
    final String expectedQuery = "SELECT timeConvert(timeCol) AS ts, COUNT(*) FROM tableName GROUP BY timeCol";
    final Map<String, String> expectedProperties = ImmutableMap.of();
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

    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(),
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
            "\"" + LEGACY_DATASET_CONFIG_DTO.getTimeColumn() + "\"",
            LEGACY_DATASET_CONFIG_DTO.getTimeFormat(),
            HOUR_PERIOD,
            INPUT_INTERVAL.getChronology().getZone().toString()));

    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(),
        HOUR_PERIOD.toString());

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties);
  }

  @Test
  public void testTimeGroupMacroWithAutoTimeWithExactBucketInEpochMillis() {
    final String inputQuery = String.format("select __timeGroup(%s,'%s','%s') from tableName",
        MacroFunction.AUTO_TIME_CONFIG,
        "NOT_IMPORTANT_SHOULD_NOT_BE_USED",
        HOUR_PERIOD);
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(TABLE_NAME)
        .setTimeColumns(
            List.of(
                new TimeColumnApi().setGranularity(HOUR_PERIOD.toString()).setName("hourlyBuckets").setFormat("1:MILLISECONDS:EPOCH"))
        );
    // todo cyril can be optimized further to "hourlyBuckets" only - see todo in TimeGroupFunction
    final String expectedQuery = "SELECT DATETIMECONVERT(\"hourlyBuckets\", '1:MILLISECONDS:EPOCH', '1:MILLISECONDS:EPOCH', '1:HOURS') FROM tableName";

    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(),
        HOUR_PERIOD.toString());

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties,
        datasetConfigDTO);
  }

  @Test
  public void testTimeGroupMacroWithAutoTimeWithExactBucketInCustomTimeFormat() {
    final String inputQuery = String.format("select __timeGroup(%s,'%s','%s') from tableName",
        MacroFunction.AUTO_TIME_CONFIG,
        "NOT_IMPORTANT_SHOULD_NOT_BE_USED",
        HOUR_PERIOD);
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(TABLE_NAME)
        .setTimeColumns(
            List.of(
                new TimeColumnApi().setGranularity(HOUR_PERIOD.toString()).setName("hourlyBuckets")
                    // custom time format
                    .setFormat("SIMPLE_DATE_FORMAT:yyyy-MM-dd-hh"))
        );
    // todo cyril can be optimized further to "hourlyBuckets" only - see todo in TimeGroupFunction
    final String expectedQuery = "SELECT DATETIMECONVERT(\"hourlyBuckets\", '1:DAYS:SIMPLE_DATE_FORMAT:yyyy-MM-dd-hh', '1:MILLISECONDS:EPOCH', '1:HOURS') FROM tableName";

    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(),
        HOUR_PERIOD.toString());

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties,
        datasetConfigDTO);
  }

  @Test
  public void testTimeGroupKeyMacroWithAutoTimeWithExactBucket() {
    final String inputQuery = String.format(
        "select COUNT(*) from tableName GROUP BY __timeGroupKey(%s, %s, %s, %s)",
        MacroFunction.AUTO_TIME_CONFIG,
        "NOT_IMPORTANT_SHOULD_NOT_BE_USED",
        HOUR_PERIOD,
        "UNUSED_ALIAS");
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO()
        .setDataset(TABLE_NAME)
        .setTimeColumns(List.of(
            new TimeColumnApi().setGranularity(HOUR_PERIOD.toString()).setName("hourlyBuckets")));
    final String expectedQuery = "SELECT COUNT(*) FROM tableName GROUP BY \"hourlyBuckets\"";

    final Map<String, String> expectedProperties = ImmutableMap.of();

    prepareRequestAndAssert(inputQuery, INPUT_INTERVAL, expectedQuery, expectedProperties,
        datasetConfigDTO);
  }

  @Test
  public void testTimeGroupMacroWithQuotedLiterals() {
    // test if a macro with string literal params is parsed correctly
    final String timeColumnMacroArg = "timeCol";
    final String timeColumnFormatMacroArg = SIMPLE_TIME_FORMAT;
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
            INPUT_INTERVAL.getChronology().getZone().toString()));

    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(),
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

    final Map<String, String> expectedProperties = ImmutableMap.of(
        MacroMetadataKeys.GRANULARITY.toString(),
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

    final String expectedQuery = String.format("SELECT * FROM tableName WHERE %s",
        expectedNestedMacro);

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
