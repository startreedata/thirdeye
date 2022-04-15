package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlCharStringLiteral;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CalciteUtils {

  public static final SqlOperator AND_OPERATOR = new SqlBinaryOperator(SqlKind.AND.sql,
      SqlKind.AND,
      0,
      true,
      null,
      null,
      null);
  public static final SqlOperator EQUALS_OPERATOR = new SqlBinaryOperator(SqlKind.EQUALS.sql,
      SqlKind.EQUALS,
      0,
      true,
      null,
      null,
      null);

  public static final SqlOperator NOT_EQUALS_OPERATOR = new SqlBinaryOperator(SqlKind.NOT_EQUALS.sql,
      SqlKind.NOT_EQUALS,
      0,
      true,
      null,
      null,
      null);

  public static final SqlOperator IN_OPERATOR = new SqlBinaryOperator(SqlKind.IN.sql,
      SqlKind.IN,
      0,
      true,
      null,
      null,
      null);

  public static final SqlOperator GT_OPERATOR = new SqlBinaryOperator(SqlKind.GREATER_THAN.sql,
      SqlKind.GREATER_THAN,
      0,
      true,
      null,
      null,
      null);

  public static final SqlOperator GTE_OPERATOR = new SqlBinaryOperator(SqlKind.GREATER_THAN_OR_EQUAL.sql,
      SqlKind.GREATER_THAN_OR_EQUAL,
      0,
      true,
      null,
      null,
      null);

  public static final SqlOperator LT_OPERATOR = new SqlBinaryOperator(SqlKind.LESS_THAN.sql,
      SqlKind.LESS_THAN,
      0,
      true,
      null,
      null,
      null);

  public static final SqlOperator LTE_OPERATOR = new SqlBinaryOperator(SqlKind.LESS_THAN_OR_EQUAL.sql,
      SqlKind.LESS_THAN_OR_EQUAL,
      0,
      true,
      null,
      null,
      null);

  public static final Map<OPER, SqlOperator> FILTER_PREDICATE_OPER_TO_CALCITE = Map.of(
      OPER.EQ, EQUALS_OPERATOR,
      OPER.NEQ, NOT_EQUALS_OPERATOR,
      OPER.IN, IN_OPERATOR,
      OPER.GT, GT_OPERATOR,
      OPER.GE, GTE_OPERATOR,
      OPER.LT, LT_OPERATOR,
      OPER.LE, LTE_OPERATOR
      // other predicates not supported for filters for the moment
  );

  public static String nodeToQuery(final SqlNode node, final SqlDialect sqlDialect) {
    // maybe quote all identifiers
    return node.toSqlString(c -> c.withDialect(sqlDialect).withQuoteAllIdentifiers(false)).getSql();
  }

  public static SqlNode queryToNode(final String sql, final SqlParser.Config sqlParserConfig)
      throws SqlParseException {
    SqlParser sqlParser = SqlParser.create(sql, sqlParserConfig);
    return sqlParser.parseQuery();
  }

  public static SqlNode expressionToNode(final String sqlExpression,
      final SqlParser.Config sqlParserConfig) throws SqlParseException {
    SqlParser sqlParser = SqlParser.create(sqlExpression, sqlParserConfig);
    return sqlParser.parseExpression();
  }

  @NonNull
  public static SqlCharStringLiteral stringLiteralOf(final String name) {
    return SqlLiteral.createCharString(name, SqlParserPos.ZERO);
  }

  @NonNull
  public static SqlNode numericLiteralOf(final String numeric) {
    return SqlLiteral.createExactNumeric(numeric, SqlParserPos.ZERO);
  }

  @NonNull
  public static SqlNode numericLiteralOf(final Number numeric) {
    return numericLiteralOf(String.valueOf(numeric));
  }

  @NonNull
  public static SqlNode booleanLiteralOf(final boolean bool) {
    return SqlLiteral.createBoolean(bool, SqlParserPos.ZERO);
  }

  @NonNull
  public static SqlNode booleanLiteralOf(final String bool) {
    return booleanLiteralOf(Boolean.parseBoolean(bool));
  }

  @NonNull
  public static SqlIdentifier identifierOf(String name) {
    return new SqlIdentifier(name, SqlParserPos.ZERO);
  }

  public static SqlBasicCall toCalcitePredicate(final QueryPredicate filter) {
    SqlIdentifier leftOperand = prepareLeftOperand(filter);
    SqlNode rightOperand = prepareRightOperand(filter);
    SqlNode[] operands = List.of(leftOperand, rightOperand).toArray(new SqlNode[0]);

    SqlOperator operator = Optional.ofNullable(FILTER_PREDICATE_OPER_TO_CALCITE.get(filter.getPredicate()
        .getOper())).orElseThrow();

    return new SqlBasicCall(operator, operands, SqlParserPos.ZERO);
  }

  /**
   * Combine a list of predicates with the AND operator.
   */
  public static SqlNode combinePredicates(final List<SqlNode> predicates) {
    if (predicates.size() == 0) {
      return null;
    }
    return addPredicates(predicates.get(0), predicates.subList(1, predicates.size() - 1));
  }

  /**
   * Add predicates to a basePredicate with the AND operator.
   */
  public static SqlNode addPredicates(SqlNode basePredicate, final List<SqlNode> predicates) {
    SqlNode whereNodeWithPredicates = basePredicate.clone(SqlParserPos.ZERO);
    if (predicates.size() == 0) {
      return whereNodeWithPredicates;
    }
    for (SqlNode newPredicate : predicates) {
      SqlNode[] whereOperands = List.of(whereNodeWithPredicates, newPredicate)
          .toArray(new SqlNode[0]);
      whereNodeWithPredicates = new SqlBasicCall(AND_OPERATOR, whereOperands, SqlParserPos.ZERO);
    }
    return whereNodeWithPredicates;
  }

  private static SqlNode prepareRightOperand(final QueryPredicate filter) {
    switch (filter.getPredicate().getOper()) {
      case IN:
        return getRightOperandForListPredicate(filter);
      case EQ:
      case NEQ:
      case GE:
      case GT:
      case LE:
      case LT:
        return getRightOperandForSimpleBinaryPredicate(filter);
      default:
        throw new UnsupportedOperationException(String.format(
            "Operator to Calcite not implemented for operator: %s",
            filter.getPredicate().getOper()));
    }
  }

  private static SqlNode getRightOperandForListPredicate(final QueryPredicate filter) {
    switch (filter.getMetricType()) {
      case STRING:
        String[] rhsValues = (String[]) filter.getPredicate().getRhs();
        List<SqlNode> nodes = Arrays.stream(rhsValues)
            .map(CalciteUtils::stringLiteralOf)
            .collect(Collectors.toList());
        return SqlNodeList.of(SqlParserPos.ZERO, nodes);
      default:
        throw new UnsupportedOperationException(String.format("Unsupported DimensionType: %s",
            filter.getMetricType()));
    }
  }

  @NonNull
  private static SqlNode getRightOperandForSimpleBinaryPredicate(final QueryPredicate filter) {
    final Object rhs = filter.getPredicate().getRhs();
    switch (filter.getMetricType()) {
      case STRING:
        return stringLiteralOf((String) rhs);
      case NUMERIC:
        return numericLiteralOf((String) rhs);
      case BOOLEAN:
        return booleanLiteralOf((String) rhs);
      // time not implemented
      default:
        throw new UnsupportedOperationException(String.format("Unsupported DimensionType: %s",
            filter.getMetricType()));
    }
  }

  @NonNull
  private static SqlIdentifier prepareLeftOperand(final QueryPredicate filter) {
    List<String> identifiers = new ArrayList<>();
    Optional.ofNullable(filter.getDataset()).ifPresent(identifiers::add);
    identifiers.add(filter.getPredicate().getLhs());
    return new SqlIdentifier(identifiers, SqlParserPos.ZERO);
  }
}
