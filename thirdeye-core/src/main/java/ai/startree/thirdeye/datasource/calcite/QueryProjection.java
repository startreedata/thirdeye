package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.spi.metric.MetricAggFunction.AVAILABLE_METRIC_AGG_FUNCTIONS_NAMES;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.CalciteUtils.expressionToNode;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.stringLiteralOf;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.util.CalciteUtils;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlUnresolvedFunction;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.SqlParserPos;

// todo cyril rename this to predicate
public class QueryProjection {

  final private String operator;
  final private List<String> operands;
  final private String quantifier;

  private QueryProjection(String operator, List<String> operands, String quantifier) {
    this.operator = operator;
    this.operands = List.copyOf(operands);
    this.quantifier = quantifier;
  }

  public static QueryProjection of(String operator, List<String> operands, String quantifier) {
    return new QueryProjection(operator, operands, quantifier);
  }

  public static QueryProjection of(String operator, List<String> operands) {
    return new QueryProjection(operator, operands, null);
  }

  public static QueryProjection of(String column) {
    return new QueryProjection(null, List.of(column), null);
  }

  public String getOperator() {
    return operator;
  }

  public List<String> getOperands() {
    return operands;
  }

  public String getQuantifier() {
    return quantifier;
  }

  public SqlNode toSqlNode() {
    if (operator != null) {
      return new SqlBasicCall(
          new SqlUnresolvedFunction(identifierOf(operator),
              null,
              null,
              null,
              null,
              SqlFunctionCategory.NUMERIC),
          operands.stream().map(CalciteUtils::identifierOf).toArray(SqlNode[]::new),
          SqlParserPos.ZERO,
          quantifier != null ? stringLiteralOf(quantifier) : null);
    } else if (operands.size() == 1 && quantifier == null) {
      return identifierOf(operands.get(0));
    } else {
      throw new UnsupportedOperationException(String.format(
          "Unsupported combination for QueryProjection: %s",
          this));
    }
  }

  public SqlNode toDialectSpecificSqlNode(final Config sqlParserConfig,
      final SqlExpressionBuilder expressionBuilder)
      throws SqlParseException {
    if (operator != null) {
      final String operatorUpper = operator.toUpperCase(Locale.ENGLISH);
      // 1. a datasource can customize any metricAggFunction based SQL - metricAggFunction acts like a macro
      if (AVAILABLE_METRIC_AGG_FUNCTIONS_NAMES.contains(operatorUpper)) {
        final MetricAggFunction metricAggFunction = MetricAggFunction.valueOf(operatorUpper);
        if (expressionBuilder.needsCustomDialect(metricAggFunction)) {
          String customDialectSql = expressionBuilder.getCustomDialectSql(metricAggFunction,
              operands,
              quantifier);
          return expressionToNode(customDialectSql, sqlParserConfig);
        }
      }
      // 2. COUNT DISTINCT is transformed in COUNT (DISTINCT ...) --- acts like to a macro
      if (MetricAggFunction.COUNT_DISTINCT.name().equals(operator)) {
        return QueryProjection.of("COUNT", operands, "DISTINCT")
            .toDialectSpecificSqlNode(sqlParserConfig, expressionBuilder);
      }
    }
    // 3. default transformation - manages any well-formed projection
    return toSqlNode();
  }

  public static QueryProjection fromMetricConfig(MetricConfigDTO metricConfigDTO) {
    MetricAggFunction aggFunction = Objects.requireNonNull(metricConfigDTO.getDefaultAggFunction());
    List<String> operands;
    // not sure why the logic below - kept it from legacy
    if (metricConfigDTO.getName().equals("*")) {
      operands = List.of("*");
    } else {
      operands = List.of(optional(metricConfigDTO.getAggregationColumn()).orElse(metricConfigDTO.getName()));
    }

    return QueryProjection.of(aggFunction.name(), operands);
  }
}
