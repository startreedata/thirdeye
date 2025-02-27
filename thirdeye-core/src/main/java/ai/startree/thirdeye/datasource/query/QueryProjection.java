/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.datasource.query;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_LOCALE;
import static ai.startree.thirdeye.spi.metric.MetricAggFunction.AVAILABLE_METRIC_AGG_FUNCTIONS_NAMES;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.CalciteUtils.addAlias;
import static ai.startree.thirdeye.util.CalciteUtils.addDesc;
import static ai.startree.thirdeye.util.CalciteUtils.expressionToNode;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.symbolLiteralOf;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlUnresolvedFunction;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * TODO spyne QueryProjection is used as a builder and implementor.
 *    This is an anti-pattern needs to be addressed
 */
public class QueryProjection {

  @Nullable
  final private String operator;
  final private List<String> operands;
  @Nullable
  final private String quantifier;
  @Nullable
  final private String alias;
  final private boolean isDescOrder;

  QueryProjection(@Nullable final String operator,
      final List<String> operands,
      @Nullable final String quantifier,
      @Nullable final String alias,
      final boolean isDescOrder) {
    this.operator = operator;
    this.operands = List.copyOf(operands);
    this.quantifier = quantifier;
    this.alias = alias;
    this.isDescOrder = isDescOrder;
  }

  /**
   * Not robust to columns with special characters that are not quoted.
   * Use for derived metric and sql snippets only, or with quoted column names.
   * For simple columns, prefer CalciteUtils.identifierOf().
   * --
   * TODO cyril remove this
   * - enforce operator not null in other constructors
   * - make it clear operands should be safe sql snippets - update tests
   */
  @Deprecated
  public static QueryProjection of(final String sqlSnippet) {
    return new QueryProjection(null, List.of(sqlSnippet), null, null, false);
  }

  // todo cyril see if it's possible to deprecrate aggregation column
  public static String getColName(final MetricConfigDTO metricConfigDTO) {
    return optional(metricConfigDTO.getAggregationColumn()).orElse(metricConfigDTO.getName());
  }

  public QueryProjection withAlias(@Nullable final String alias) {
    if (isDescOrder) {
      throw new IllegalStateException("isDescOrder is true. Cannot combine alias and desc order.");
    }
    return new QueryProjection(operator,
        operands,
        quantifier,
        alias,
        isDescOrder);
  }

  public QueryProjection withDescOrder() {
    if (alias != null) {
      throw new IllegalStateException("alias is not null. Cannot combine alias and desc order.");
    }
    return new QueryProjection(operator, operands, quantifier, alias, true);
  }

  private SqlNode toSqlNode(final Config sqlParserConfig) {
    if (operator != null) {
      return applySpecialOperators(new SqlBasicCall(
          new SqlUnresolvedFunction(identifierOf(operator),
              null,
              null,
              null,
              null,
              SqlFunctionCategory.NUMERIC),
          operandNodes(sqlParserConfig),
          SqlParserPos.ZERO,
          quantifier != null ? symbolLiteralOf(quantifier) : null));
    } else if (operands.size() == 1 && quantifier == null) {
      return applySpecialOperators(operandNodes(sqlParserConfig).getFirst());
    } else {
      throw new UnsupportedOperationException(String.format(
          "Unsupported combination for QueryProjection: %s",
          this));
    }
  }

  private List<SqlNode> operandNodes(final Config sqlParserConfig) {
    return operands.stream()
        .map(operand -> expressionToNode(operand, sqlParserConfig))
        .collect(Collectors.toCollection(() -> new ArrayList<>(operands.size())));
  }

  public SqlNode toDialectSpecificSqlNode(final Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder) {
    if (operator != null) {
      final String operatorUpper = operator.toUpperCase(DEFAULT_LOCALE);
      // 1. a datasource can customize any metricAggFunction based SQL
      if (AVAILABLE_METRIC_AGG_FUNCTIONS_NAMES.contains(operatorUpper)) {
        final MetricAggFunction metricAggFunction = MetricAggFunction.valueOf(operatorUpper);
        if (expressionBuilder.needsCustomDialect(metricAggFunction)) {
          final String customDialectSql = expressionBuilder.getCustomDialectSql(metricAggFunction,
              operands,
              quantifier);
          return applySpecialOperators(expressionToNode(customDialectSql, sqlParserConfig));
        }
      }
      // 2. COUNT DISTINCT is transformed in COUNT (DISTINCT ...)
      if (MetricAggFunction.COUNT_DISTINCT.name().equals(operator)) {
        return applySpecialOperators(new QueryProjection("COUNT",
            operands,
            "DISTINCT",
            null,
            false)
            .toDialectSpecificSqlNode(sqlParserConfig, expressionBuilder));
      }
    }
    // 3. default transformation - manages any well-formed projection
    return toSqlNode(sqlParserConfig);
  }

  private SqlNode applySpecialOperators(final SqlNode node) {
    if (alias != null) {
      return addAlias(node, alias);
    }
    if (isDescOrder) {
      return addDesc(node);
    }

    return node;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final QueryProjection that = (QueryProjection) o;
    return isDescOrder == that.isDescOrder && Objects.equals(operator, that.operator)
        && Objects.equals(operands, that.operands) && Objects.equals(quantifier,
        that.quantifier) && Objects.equals(alias, that.alias);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operator, operands, quantifier, alias, isDescOrder);
  }

  @Override
  public String toString() {
    return "QueryProjection{" +
        "operator='" + operator + '\'' +
        ", operands=" + operands +
        ", quantifier='" + quantifier + '\'' +
        ", alias='" + alias + '\'' +
        ", isDescOrder=" + isDescOrder +
        '}';
  }
}
