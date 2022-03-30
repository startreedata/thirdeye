/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.sql.filter;

import ai.startree.thirdeye.detectionpipeline.sql.SqlLanguageTranslator;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.detection.v2.TimeseriesFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlCharStringLiteral;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlWith;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.util.SqlShuttle;
import org.jetbrains.annotations.NotNull;

/**
 * Class responsible for injecting a List of TimeSeriesFilter in a query string.
 *
 * Notes:
 * Will break if a dimension name conflicts with a SQL function name.
 * Could be fixed using .withQuoteAllIdentifiers(true)
 * but would require making sure it's consistent with the MacroEngine
 */
public class FiltersEngine {

  private static final SqlOperator AND_OPERATOR = new SqlBinaryOperator(SqlKind.AND.sql,
      SqlKind.AND,
      0,
      true,
      null,
      null,
      null);
  private static final SqlOperator EQUALS_OPERATOR = new SqlBinaryOperator(SqlKind.EQUALS.sql,
      SqlKind.EQUALS,
      0,
      true,
      null,
      null,
      null);

  private static final SqlOperator NOT_EQUALS_OPERATOR = new SqlBinaryOperator(SqlKind.NOT_EQUALS.sql,
      SqlKind.NOT_EQUALS,
      0,
      true,
      null,
      null,
      null);

  private static final SqlOperator IN_OPERATOR = new SqlBinaryOperator(SqlKind.IN.sql,
      SqlKind.IN,
      0,
      true,
      null,
      null,
      null);

  public static final Map<OPER, SqlOperator> PREDICATE_OPER_TO_CALCITE_OPER = Map.of(
      OPER.EQ, EQUALS_OPERATOR,
      OPER.NEQ, NOT_EQUALS_OPERATOR,
      OPER.IN, IN_OPERATOR
      // other predicates not supported for the moment
  );

  private final SqlParser.Config sqlParserConfig;
  private final SqlDialect sqlDialect;
  private final String query;
  private final List<TimeseriesFilter> filters;

  public FiltersEngine(final SqlLanguage sqlLanguage, final String query,
      List<TimeseriesFilter> filters) {
    this.sqlParserConfig = SqlLanguageTranslator.translate(sqlLanguage.getSqlParserConfig());
    this.sqlDialect = SqlLanguageTranslator.translate(sqlLanguage.getSqlDialect());
    this.query = query;
    this.filters = filters;
  }

  private SqlNode queryToNode(final String sql) throws SqlParseException {
    SqlParser sqlParser = SqlParser.create(sql, sqlParserConfig);
    return sqlParser.parseQuery();
  }

  private String nodeToQuery(final SqlNode node) {
    return node.toSqlString(
        c -> c.withDialect(sqlDialect)
            .withQuoteAllIdentifiers(false)
    ).getSql();
  }

  public String prepareQuery() throws SqlParseException {
    SqlNode rootNode = queryToNode(query);
    SqlNode rootNodeWithFilters = rootNode.accept(new FilterVisitor());
    String preparedQuery = nodeToQuery(rootNodeWithFilters);

    return preparedQuery;
  }

  private List<SqlBasicCall> getCalcitePredicates() {
    return filters.stream().map(FiltersEngine::timeseriesFilterToCalcitePredicate).collect(
        Collectors.toList());
  }

  //fixme cyril I'm around here and this will get difficult
  private static SqlBasicCall timeseriesFilterToCalcitePredicate(final TimeseriesFilter filter) {
    SqlIdentifier leftOperand = prepareLeftOperand(filter);
    SqlNode rightOperand = prepareRightOperand(filter);
    SqlNode[] operands = List.of(leftOperand, rightOperand).toArray(new SqlNode[0]);

    SqlOperator operator = Optional.ofNullable(PREDICATE_OPER_TO_CALCITE_OPER.get(
        filter.getPredicate().getOper())).orElseThrow();

    return new SqlBasicCall(operator, operands, SqlParserPos.ZERO);
  }

  private static SqlNode prepareRightOperand(final TimeseriesFilter filter) {
    switch (filter.getPredicate().getOper()) {
      case IN:
        return getRightOperandForListPredicate(filter);
      case EQ: case NEQ: case GE: case GT: case LE: case LT:
        return getRightOperandForSimpleBinaryPredicate(filter);
      default:
        throw new UnsupportedOperationException(String.format("Operator to Calcite not implemented for operator: %s", filter.getPredicate().getOper()));
    }

  }

  private static SqlNode getRightOperandForListPredicate(final TimeseriesFilter filter) {
    switch (filter.getMetricType()) {
      case STRING:
        String[] rhsValues = (String[]) filter.getPredicate().getRhs();
        List<SqlNode> nodes = Arrays.stream(rhsValues).map(FiltersEngine::sqlStringLiteralOf).collect(Collectors.toList());
        return SqlNodeList.of(SqlParserPos.ZERO, nodes);
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported DimensionType: %s", filter.getMetricType()));
    }
  }

  @NotNull
  private static SqlNode getRightOperandForSimpleBinaryPredicate(final TimeseriesFilter filter) {
    switch (filter.getMetricType()) {
      case STRING:
        final String rhsValue = (String) filter.getPredicate().getRhs();
        return sqlStringLiteralOf(rhsValue);
      // eg for numeric:
      // SqlLiteral.createExactNumeric(...)
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported DimensionType: %s", filter.getMetricType()));
    }
  }

  @NotNull
  private static SqlCharStringLiteral sqlStringLiteralOf(final String rhsValue) {
    return SqlLiteral.createCharString(rhsValue, SqlParserPos.ZERO);
  }

  @NotNull
  private static SqlIdentifier prepareLeftOperand(final TimeseriesFilter filter) {
    List<String> identifiers = List.of(filter.getDataset(), filter.getPredicate().getLhs());
    return new SqlIdentifier(identifiers, SqlParserPos.ZERO);
  }

  private class FilterVisitor extends SqlShuttle {

    @Override
    public SqlNode visit(SqlCall call) {
      // only visit the top level node
      return addPredicates(call);
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
    private SqlNode addPredicates(final SqlCall call) {
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
      List<SqlBasicCall> newPredicates = getCalcitePredicates();
      SqlNode whereNodeWithPredicates = addPredicates(whereNode, newPredicates);
      selectNode.setWhere(whereNodeWithPredicates);

      return call;
    }

    private SqlNode addPredicates(SqlNode whereNode, final List<SqlBasicCall> predicates) {
      SqlNode whereNodeWithPredicates = whereNode.clone(SqlParserPos.ZERO);
      for (SqlBasicCall newPredicate : predicates) {
        SqlNode[] whereOperands = List.of(whereNodeWithPredicates, newPredicate)
            .toArray(new SqlNode[0]);
        whereNodeWithPredicates = new SqlBasicCall(AND_OPERATOR, whereOperands, SqlParserPos.ZERO);
      }
      return whereNodeWithPredicates;
    }
  }
}
