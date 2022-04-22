package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import java.util.List;
import java.util.Map;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeName;
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
  public static SqlNode stringLiteralOf(final String name) {
    return SqlLiteral.createCharString(name, SqlParserPos.ZERO);
  }

  @NonNull
  public static SqlLiteral symbolLiteralOf(final SqlTypeName name) {
    return SqlLiteral.createSymbol(name, SqlParserPos.ZERO);
  }

  @NonNull
  public static SqlLiteral symbolLiteralOf(final String name) {
    return symbolLiteralOf(SqlTypeName.get(name));
  }

  @NonNull
  public static SqlLiteral numericLiteralOf(final String numeric) {
    return SqlLiteral.createExactNumeric(numeric, SqlParserPos.ZERO);
  }

  @NonNull
  public static SqlLiteral numericLiteralOf(final Number numeric) {
    return numericLiteralOf(String.valueOf(numeric));
  }

  @NonNull
  public static SqlLiteral booleanLiteralOf(final boolean bool) {
    return SqlLiteral.createBoolean(bool, SqlParserPos.ZERO);
  }

  @NonNull
  public static SqlLiteral booleanLiteralOf(final String bool) {
    return booleanLiteralOf(Boolean.parseBoolean(bool));
  }

  @NonNull
  public static SqlIdentifier identifierOf(String name) {
    return new SqlIdentifier(name, SqlParserPos.ZERO);
  }

  /**
   * Combine a list of predicates with the AND operator.
   */
  public static SqlNode combinePredicates(final List<SqlNode> predicates) {
    if (predicates.isEmpty()) {
      return null;
    } else if (predicates.size() == 1) {
      return predicates.get(0);
    }
    return addPredicates(predicates.get(0), predicates.subList(1, predicates.size()));
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
}
