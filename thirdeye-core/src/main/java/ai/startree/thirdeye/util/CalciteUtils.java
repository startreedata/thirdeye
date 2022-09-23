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
package ai.startree.thirdeye.util;

import static org.apache.calcite.sql.SqlOperator.MDX_PRECEDENCE;

import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.calcite.sql.SqlAsOperator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlPostfixOperator;
import org.apache.calcite.sql.parser.SqlAbstractParserImpl.Metadata;
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

  public static final SqlOperator DESC_OPERATOR = new SqlPostfixOperator("DESC",
      SqlKind.DESCENDING,
      MDX_PRECEDENCE,
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

  public static String nodeToQuery(final SqlNode node, final SqlDialect sqlDialect,
      final boolean quoteIdentifiers) {
    return node.toSqlString(c -> c.withDialect(sqlDialect)
        .withQuoteAllIdentifiers(quoteIdentifiers)).getSql();
  }

  public static SqlNode queryToNode(final String sql, final SqlParser.Config sqlParserConfig)
      throws SqlParseException {
    SqlParser sqlParser = SqlParser.create(sql, sqlParserConfig);
    return sqlParser.parseQuery();
  }

  public static SqlNode expressionToNode(final String sqlExpression,
      final SqlParser.Config sqlParserConfig) throws SqlParseException {
    // calcite parser cannot recognize * as a valid expression
    if (sqlExpression.equals("*")) {
      return identifierOf(sqlExpression);
    }
    SqlParser sqlParser = SqlParser.create(sqlExpression, sqlParserConfig);
    return sqlParser.parseExpression();
  }

  public static String quoteIdentifierIfReserved(final String identifier,
      final SqlParser.Config sqlParserConfig,
      final SqlDialect dialect) {
    Metadata metadata = SqlParser.create("", sqlParserConfig).getMetadata();
    String upperIdentifier = identifier.toUpperCase(Locale.ENGLISH);
    if (metadata.isReservedWord(upperIdentifier) || metadata.isSql92ReservedWord(upperIdentifier)
        || metadata.isReservedFunctionName(upperIdentifier)) {
      return dialect.quoteIdentifier(identifier);
    }
    return identifier;
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

  @NonNull
  public static SqlNode addAlias(final SqlNode node, @NonNull final String alias) {
    final List<SqlNode> aliasOperands = List.of(node, identifierOf(alias));
    return new SqlBasicCall(new SqlAsOperator(),
        aliasOperands,
        SqlParserPos.ZERO);
  }

  @NonNull
  public static SqlNode addDesc(final SqlNode node) {
    return new SqlBasicCall(CalciteUtils.DESC_OPERATOR, List.of(node), SqlParserPos.ZERO);
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
      List<SqlNode> whereOperands = List.of(whereNodeWithPredicates, newPredicate);
      whereNodeWithPredicates = new SqlBasicCall(AND_OPERATOR, whereOperands, SqlParserPos.ZERO);
    }
    return whereNodeWithPredicates;
  }
}
