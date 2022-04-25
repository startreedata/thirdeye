/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.sql.filter;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.macro.ThirdEyeSqlParserConfig;
import ai.startree.thirdeye.spi.datasource.macro.ThirdeyeSqlDialect;
import ai.startree.thirdeye.spi.metric.DimensionType;
import ai.startree.thirdeye.testutils.SqlUtils;
import java.util.List;
import org.apache.calcite.sql.parser.SqlParseException;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class FilterEngineTest {

  private static final String IDENTIFIER_QUOTE_STRING = "\"";

  private static final QueryPredicate STRING_FILTER_EQUAL = QueryPredicate.of(
      new Predicate("browser", OPER.EQ, "chrome"),
      DimensionType.STRING,
      "tableName");
  private static final String STRING_FILTER_EQUAL_TO_STRING = " AND (tableName.browser = 'chrome')";

  private static final QueryPredicate STRING_FILTER_NOT_EQUAL = QueryPredicate.of(
      new Predicate("country", OPER.NEQ, "US"),
      DimensionType.STRING,
      "tableName");
  private static final String STRING_FILTER_NOT_EQUAL_TO_STRING = " AND (tableName.country <> 'US')";

  private static final QueryPredicate STRING_FILTER_IN = QueryPredicate.of(
      new Predicate("browser", OPER.IN, new String[]{"chrome", "safari"}),
          DimensionType.STRING,
          "tableName"
          );
  private static final String STRING_FILTER_IN_TO_STRING = " AND (tableName.browser IN ('chrome', 'safari'))";

  @Test
  public void testNoFilters() throws SqlParseException {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(), query, List.of());
    final String output = filtersEngine.prepareQuery();
    final String expected = query;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterEqualString() throws SqlParseException {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = query + STRING_FILTER_EQUAL_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterNotEqualString() throws SqlParseException {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_NOT_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = query + STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterInString() throws SqlParseException {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_IN));
    final String output = filtersEngine.prepareQuery();

    final String expected = query + STRING_FILTER_IN_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterAndMacros() throws SqlParseException {
    final String query = "SELECT __timeGroup(timeCol, yyyyMMdd, P5D) AS ts, metric AS met FROM tableName WHERE __timeFilter(ts)";
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = query + STRING_FILTER_EQUAL_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterAndEscapedFunction() throws SqlParseException {
    // "date" is a function that is escaped - here it's a column identifier
    final String query = "SELECT __timeGroup(\"date\", yyyyMMdd, P5D) AS ts, metric AS met FROM tableName WHERE __timeFilter(ts)";
    final QueryPredicate stringFilter = QueryPredicate.of(
        new Predicate("browser", OPER.EQ, "chrome"),
        DimensionType.STRING,
        "tableName");
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(stringFilter));
    final String output = filtersEngine.prepareQuery();

    final String expected = query + STRING_FILTER_EQUAL_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testMultipleFilters() throws SqlParseException {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = query + STRING_FILTER_EQUAL_TO_STRING + STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testWithOrderByKeyword() throws SqlParseException {
    final String baseQuery = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final String orderByStatement = " ORDER BY ts";
    final String query = baseQuery + orderByStatement;
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = baseQuery + STRING_FILTER_EQUAL_TO_STRING + STRING_FILTER_NOT_EQUAL_TO_STRING
        + orderByStatement;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testWithGroupByKeyword() throws SqlParseException {
    final String baseQuery = "SELECT timeCol AS ts, SUM(metric) AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final String groupByStatement = " GROUP BY ts";
    final String query = baseQuery + groupByStatement;
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = baseQuery + STRING_FILTER_EQUAL_TO_STRING + STRING_FILTER_NOT_EQUAL_TO_STRING
        + groupByStatement;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testWithHavingKeyword() throws SqlParseException {
    final String baseQuery = "SELECT timeCol AS ts, SUM(metric) AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final String groupByHavingStatement = " GROUP BY ts HAVING met > 10";
    final String query = baseQuery + groupByHavingStatement;
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = baseQuery + STRING_FILTER_EQUAL_TO_STRING + STRING_FILTER_NOT_EQUAL_TO_STRING
        + groupByHavingStatement;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testWithDistinctKeyword() throws SqlParseException {
    final String query = "SELECT DISTINCT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = query + STRING_FILTER_EQUAL_TO_STRING + STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testWithWithKeyword() throws SqlParseException {
    // only filter in main query
    final String query = "WITH t AS (SELECT a FROM tableName) (SELECT timeCol AS ts, t.a AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654)";
    final FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filtersEngine.prepareQuery();

    final String expected = query.substring(0, query.length()-1) + STRING_FILTER_EQUAL_TO_STRING + STRING_FILTER_NOT_EQUAL_TO_STRING + ")";

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testWithJoin() throws SqlParseException {
    String query = "SELECT tableName.a, b, c FROM tableName INNER JOIN otherTable ON tableName.a = otherTable.a WHERE ts >= 1232456765 AND ts < 5432987654";
    FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    String output = filtersEngine.prepareQuery();

    String expected = query + STRING_FILTER_EQUAL_TO_STRING + STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  @Test
  public void testWithSubQuery() throws SqlParseException {
    String query = "SELECT tableName.a, b, c FROM tableName INNER JOIN otherTable ON tableName.a = otherTable.a WHERE ts >= 1232456765 AND ts < 5432987654 AND d = (SELECT COUNT(*) FROM tableName)";
    FiltersEngine filtersEngine = new FiltersEngine(new TestSqlLanguage(),
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    String output = filtersEngine.prepareQuery();

    String expected = query + STRING_FILTER_EQUAL_TO_STRING + STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(SqlUtils.cleanSql(output)).isEqualTo(SqlUtils.cleanSql(expected));
  }

  private static class TestSqlLanguage implements SqlLanguage {

    @Override
    public ThirdEyeSqlParserConfig getSqlParserConfig() {
      return new ThirdEyeSqlParserConfig.Builder()
          .withLex("MYSQL_ANSI")
          .withConformance("DEFAULT")
          .withParserFactory("SqlBabelParserImpl")
          .build();
    }

    @Override
    public ThirdeyeSqlDialect getSqlDialect() {
      return new ThirdeyeSqlDialect.Builder()
          .withBaseDialect("AnsiSqlDialect")
          .withIdentifierQuoteString(IDENTIFIER_QUOTE_STRING)
          .build();
    }
  }
}
