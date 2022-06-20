/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.sql.filter;

import static ai.startree.thirdeye.util.CalciteUtils.addPredicates;
import static ai.startree.thirdeye.util.CalciteUtils.nodeToQuery;
import static ai.startree.thirdeye.util.CalciteUtils.queryToNode;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlWith;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;

/**
 * Class responsible for injecting a List of TimeSeriesFilter in a query string.
 *
 * Notes:
 * Will break if a dimension name conflicts with a SQL function name.
 * Could be fixed using .withQuoteAllIdentifiers(true)
 * but would require making sure it's consistent with the MacroEngine
 */
public class FiltersEngine {

  private static final List<OPER> SUPPORTED_FILTER_OPERATIONS = List.of(OPER.EQ, OPER.NEQ, OPER.IN);
  public static final boolean QUOTE_IDENTIFIERS = false;

  private final SqlParser.Config sqlParserConfig;
  private final SqlDialect sqlDialect;
  private final String query;
  private final List<QueryPredicate> filters;

  public FiltersEngine(final SqlLanguage sqlLanguage, final String query,
      List<QueryPredicate> filters) {
    this.sqlParserConfig = SqlLanguageTranslator.translate(sqlLanguage.getSqlParserConfig());
    this.sqlDialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());
    this.query = query;
    this.filters = filters;
  }

  public String prepareQuery() throws SqlParseException {
    SqlNode rootNode = queryToNode(query, sqlParserConfig);
    SqlNode rootNodeWithFilters = rootNode.accept(new FilterVisitor());
    String preparedQuery = nodeToQuery(rootNodeWithFilters, sqlDialect, QUOTE_IDENTIFIERS);

    return preparedQuery;
  }

  private List<SqlNode> getCalcitePredicates() {
    return filters.stream()
        .peek(f -> checkArgument(SUPPORTED_FILTER_OPERATIONS.contains(f.getPredicate().getOper()),
            "Unsupported filter operation for filter injection: %s ", f.getPredicate().getOper()))
        .map(QueryPredicate::toSqlNode).collect(Collectors.toList());
  }

  private class FilterVisitor extends SqlShuttle {

    @Override
    public SqlNode visit(SqlCall call) {
      // only visit the top level node
      return injectPredicates(call);
    }

    /**
     * Simple filter injection.
     * Principle: find the WHERE clause of the main query. Add predicates.
     *
     * Notes:
     * Injecting in complex queries will require to change the logic here.
     * Current behavior:
     * - Support of queries with a WITH statement is limited.
     * For "with t as (...), t2 as (...),... SELECT ..."
     * Injection is only performed in the main query (the last select).
     * - Injected identifiers are prefixed by their table name.
     * This avoids column name collisions when there is a JOIN. See predicate creation.
     * - The method does not look at the table reference before injecting predicates.
     * It could do so to be more robust and find the parts of the SQL where injection is required.
     */
    private SqlNode injectPredicates(final SqlCall call) {
      SqlSelect selectNode;
      if (call.getClass() == SqlSelect.class) {
        selectNode = (SqlSelect) call;
      } else if (call.getClass() == SqlOrderBy.class) {
        final SqlOrderBy orderByNode = (SqlOrderBy) call;
        // element of index 0 is the select node
        selectNode = (SqlSelect) orderByNode.getOperandList().get(0);
      } else if (call.getClass() == SqlWith.class) {
        final SqlWith withNode = (SqlWith) call;
        // element of index 1 is the select node - in simple with + select queries
        selectNode = (SqlSelect) withNode.getOperandList().get(1);
      } else {
        throw new UnsupportedOperationException(
            String.format("Filter injection failed. Unknown SqlNode class: %s", call.getClass()));
      }

      SqlNode whereNode = Objects.requireNonNull(selectNode.getWhere());
      List<SqlNode> newPredicates = getCalcitePredicates();
      SqlNode whereNodeWithPredicates = addPredicates(whereNode, newPredicates);
      selectNode.setWhere(whereNodeWithPredicates);

      return call;
    }
  }
}
