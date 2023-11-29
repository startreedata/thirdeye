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
package ai.startree.thirdeye.datasource.query;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.CalciteUtils.FILTER_PREDICATE_OPER_TO_CALCITE;
import static ai.startree.thirdeye.util.CalciteUtils.booleanLiteralOf;
import static ai.startree.thirdeye.util.CalciteUtils.numericLiteralOf;
import static ai.startree.thirdeye.util.CalciteUtils.stringLiteralOf;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.metric.DimensionType;
import ai.startree.thirdeye.util.CalciteUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.checkerframework.checker.nullness.qual.NonNull;

public class QueryPredicate {

  private final Predicate predicate;
  private final DimensionType metricType;
  private final String dataset;

  private QueryPredicate(final Predicate predicate,
      final DimensionType metricType, final String dataset) {
    this.predicate = predicate;
    this.metricType = metricType;
    this.dataset = dataset;
  }

  public static QueryPredicate of(final Predicate predicate, final DimensionType metricType,
      final String dataset) {
    return new QueryPredicate(predicate, metricType, dataset);
  }

  public static QueryPredicate of(final Predicate predicate, final DimensionType metricType) {
    return new QueryPredicate(predicate, metricType, null);
  }

  public Predicate getPredicate() {
    return predicate;
  }

  public DimensionType getMetricType() {
    return metricType;
  }

  public String getDataset() {
    return dataset;
  }

  public SqlNode toSqlNode() {
    final SqlIdentifier leftOperand = prepareLeftOperand();
    final SqlNode rightOperand = prepareRightOperand();
    final List<SqlNode> operands = List.of(leftOperand, rightOperand);

    SqlOperator operator = optional(FILTER_PREDICATE_OPER_TO_CALCITE.get(predicate.getOper()))
        .orElseThrow();

    return new SqlBasicCall(operator, operands, SqlParserPos.ZERO);
  }

  private SqlNode prepareRightOperand() {
    switch (predicate.getOper()) {
      case IN:
        return getRightOperandForListPredicate();
      case EQ:
      case NEQ:
      case GE:
      case GT:
      case LE:
      case LT:
        return getRightOperandForSimpleBinaryPredicate();
      default:
        throw new UnsupportedOperationException(String.format(
            "Operator to Calcite not implemented for operator: %s",
            predicate.getOper()));
    }
  }

  @NonNull
  private SqlNode getRightOperandForSimpleBinaryPredicate() {
    final Object rhs = predicate.getRhs();
    switch (metricType) {
      case STRING:
        return stringLiteralOf((String) rhs);
      case NUMERIC:
        return numericLiteralOf((String) rhs);
      case BOOLEAN:
        return booleanLiteralOf((String) rhs);
      // time not implemented
      default:
        throw new UnsupportedOperationException(String.format("Unsupported DimensionType: %s",
            metricType));
    }
  }

  @NonNull
  private SqlIdentifier prepareLeftOperand() {
    List<String> identifiers = new ArrayList<>();
    optional(dataset).ifPresent(identifiers::add);
    identifiers.add(predicate.getLhs());
    return new SqlIdentifier(identifiers, SqlParserPos.ZERO);
  }

  private SqlNode getRightOperandForListPredicate() {
    Function<String, SqlNode> literalOf;
    if (metricType == DimensionType.STRING) {
      literalOf = CalciteUtils::stringLiteralOf;
    } else if (metricType == DimensionType.NUMERIC) {
      literalOf = CalciteUtils::numericLiteralOf;
    } else {
      throw new UnsupportedOperationException(String.format(
          "Unsupported DimensionType for list rhs predicate: %s", metricType));
    }
    String[] rhsStrings = (String[]) predicate.getRhs();
    List<SqlNode> stringNodes = Arrays.stream(rhsStrings)
        .map(literalOf)
        .collect(Collectors.toList());
    return SqlNodeList.of(SqlParserPos.ZERO, stringNodes);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final QueryPredicate that = (QueryPredicate) o;
    return Objects.equals(predicate, that.predicate) && metricType == that.metricType
        && Objects.equals(dataset, that.dataset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(predicate, metricType, dataset);
  }

  @Override
  public String toString() {
    return "QueryPredicate{" +
        "predicate=" + predicate +
        ", metricType=" + metricType +
        ", dataset='" + dataset + '\'' +
        '}';
  }
}
