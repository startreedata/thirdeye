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
package ai.startree.thirdeye.detectionpipeline.sql.filter;

import ai.startree.thirdeye.IntegrationTestUtils;
import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.plugins.datasource.pinot.PinotSqlLanguage;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.metric.DimensionType;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**
 * Tests are performed with Pinot language implementation.
 */
public class FilterEngineTest {

  private static final QueryPredicate STRING_FILTER_EQUAL = QueryPredicate.of(new Predicate(
      "browser",
      OPER.EQ,
      "chrome"), DimensionType.STRING, "tableName");
  private static final String QUOTED_STRING_FILTER_EQUAL_TO_STRING = " AND (\"tableName\".\"browser\" = 'chrome')";

  private static final QueryPredicate STRING_FILTER_NOT_EQUAL = QueryPredicate.of(new Predicate(
      "country",
      OPER.NEQ,
      "US"), DimensionType.STRING, "tableName");
  private static final String QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING = " AND (\"tableName\".\"country\" <> 'US')";

  private static final QueryPredicate STRING_FILTER_IN = QueryPredicate.of(new Predicate("browser",
      OPER.IN,
      new String[]{"chrome", "safari"}), DimensionType.STRING, "tableName");
  private static final String QUOTED_STRING_FILTER_IN_TO_STRING = " AND (\"tableName\".\"browser\" IN ('chrome', 'safari'))";
  public static final SqlLanguage TEST_SQL_LANGUAGE = new PinotSqlLanguage();

  @Test
  public void testNoFilters() {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE, query, List.of());
    final String output = filterEngine.prepareQuery();
    final String expected = "SELECT \"timeCol\" AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterEqualString() {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedQuery = "SELECT \"timeCol\" AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String expected = quotedQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterNotEqualString() {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_NOT_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedQuery = "SELECT \"timeCol\" AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String expected = quotedQuery + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterInString() {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_IN));
    final String output = filterEngine.prepareQuery();

    final String quotedQuery = "SELECT \"timeCol\" AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String expected = quotedQuery + QUOTED_STRING_FILTER_IN_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterAndMacros() {
    final String query = "SELECT __timeGroup(timeCol, 'yyyyMMdd', 'P5D') AS ts, metric AS met FROM tableName WHERE __timeFilter(ts, 'EPOCH')";
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedQuery = "SELECT \"__timeGroup\"(\"timeCol\", 'yyyyMMdd', 'P5D') AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"__timeFilter\"(\"ts\", 'EPOCH')";
    final String expected = quotedQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testSingleFilterAndEscapedFunction() {
    // "date" is a function that is escaped - here it's a column identifier
    final String query = "SELECT __timeGroup(\"date\", 'yyyyMMdd', 'P5D') AS ts, metric AS met FROM tableName WHERE __timeFilter(ts, 'EPOCH')";
    final QueryPredicate stringFilter = QueryPredicate.of(new Predicate("browser",
        OPER.EQ,
        "chrome"), DimensionType.STRING, "tableName");
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(stringFilter));
    final String output = filterEngine.prepareQuery();

    final String quotedQuery = "SELECT \"__timeGroup\"(\"date\", 'yyyyMMdd', 'P5D') AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"__timeFilter\"(\"ts\", 'EPOCH')";
    final String expected = quotedQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testMultipleFilters() {
    final String query = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedQuery = "SELECT \"timeCol\" AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String expected = quotedQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING
        + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testWithOrderByKeyword() {
    final String baseQuery = "SELECT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final String orderByStatement = " ORDER BY ts";
    final String query = baseQuery + orderByStatement;
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedBaseQuery = "SELECT \"timeCol\" AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String quotedOrderByStatement = " ORDER BY \"ts\"";
    final String expected = quotedBaseQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING
        + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING + quotedOrderByStatement;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testWithGroupByKeyword() {
    final String baseQuery = "SELECT timeCol AS ts, SUM(metric) AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final String groupByStatement = " GROUP BY ts";
    final String query = baseQuery + groupByStatement;
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedBaseQuery = "SELECT \"timeCol\" AS \"ts\", SUM(\"metric\") AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String quotedGroupByStatement = " GROUP BY \"ts\"";
    final String expected = quotedBaseQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING
        + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING + quotedGroupByStatement;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testWithHavingKeyword() {
    final String baseQuery = "SELECT timeCol AS ts, SUM(metric) AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final String groupByHavingStatement = " GROUP BY ts HAVING met > 10";
    final String query = baseQuery + groupByHavingStatement;
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedBaseQuery = "SELECT \"timeCol\" AS \"ts\", SUM(\"metric\") AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String quotedGroupByHavingStatement = " GROUP BY \"ts\" HAVING \"met\" > 10";
    final String expected = quotedBaseQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING
        + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING + quotedGroupByHavingStatement;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testWithDistinctKeyword() {
    final String query = "SELECT DISTINCT timeCol AS ts, metric AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654";
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedQuery = "SELECT DISTINCT \"timeCol\" AS \"ts\", \"metric\" AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String expected = quotedQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING
        + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testWithWithKeyword() {
    // only filter in main query
    final String query = "WITH t AS (SELECT a FROM tableName) (SELECT timeCol AS ts, t.a AS met FROM tableName WHERE ts >= 1232456765 AND ts < 5432987654)";
    final FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    final String output = filterEngine.prepareQuery();

    final String quotedQuery = "WITH \"t\" AS (SELECT \"a\" FROM \"tableName\") (SELECT \"timeCol\" AS \"ts\", \"t\".\"a\" AS \"met\" FROM \"tableName\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654)";
    final String expected =
        quotedQuery.substring(0, quotedQuery.length() - 1) + QUOTED_STRING_FILTER_EQUAL_TO_STRING
            + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING + ")";

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testWithJoin() {
    String query = "SELECT tableName.a, b, c FROM tableName INNER JOIN otherTable ON tableName.a = otherTable.a WHERE ts >= 1232456765 AND ts < 5432987654";
    FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    String output = filterEngine.prepareQuery();

    final String quotedQuery = "SELECT \"tableName\".\"a\", \"b\", \"c\" FROM \"tableName\" INNER JOIN \"otherTable\" ON \"tableName\".\"a\" = \"otherTable\".\"a\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654";
    final String expected = quotedQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING
        + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }

  @Test
  public void testWithSubQuery() {
    String query = "SELECT tableName.a, b, c FROM tableName INNER JOIN otherTable ON tableName.a = otherTable.a WHERE ts >= 1232456765 AND ts < 5432987654 AND d = (SELECT COUNT(*) FROM tableName)";
    FilterEngine filterEngine = new FilterEngine(TEST_SQL_LANGUAGE,
        query,
        List.of(STRING_FILTER_EQUAL, STRING_FILTER_NOT_EQUAL));
    String output = filterEngine.prepareQuery();

    String quotedQuery = "SELECT \"tableName\".\"a\", \"b\", \"c\" FROM \"tableName\" INNER JOIN \"otherTable\" ON \"tableName\".\"a\" = \"otherTable\".\"a\" WHERE \"ts\" >= 1232456765 AND \"ts\" < 5432987654 AND \"d\" = (SELECT COUNT(*) FROM \"tableName\")";
    String expected = quotedQuery + QUOTED_STRING_FILTER_EQUAL_TO_STRING
        + QUOTED_STRING_FILTER_NOT_EQUAL_TO_STRING;

    Assertions.assertThat(IntegrationTestUtils.cleanSql(output))
        .isEqualTo(IntegrationTestUtils.cleanSql(expected));
  }
}
