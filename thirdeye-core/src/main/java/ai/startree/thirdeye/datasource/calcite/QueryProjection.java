package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.spi.metric.MetricAggFunction.AVAILABLE_METRIC_AGG_FUNCTIONS_NAMES;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.CalciteUtils.addAlias;
import static ai.startree.thirdeye.util.CalciteUtils.expressionToNode;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.symbolLiteralOf;

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

public class QueryProjection {

  final private String operator;
  final private List<String> operands;
  final private String quantifier;
  final private String alias;

  private QueryProjection(final String operator, final List<String> operands,
      final String quantifier, final String alias) {
    this.operator = operator;
    this.operands = List.copyOf(operands);
    this.quantifier = quantifier;
    this.alias = alias;
  }

  public static QueryProjection of(String operator, List<String> operands, String quantifier) {
    return new QueryProjection(operator, operands, quantifier, null);
  }

  public static QueryProjection of(String operator, List<String> operands) {
    return new QueryProjection(operator, operands, null, null);
  }

  public static QueryProjection of(String column) {
    return new QueryProjection(null, List.of(column), null, null);
  }

  public QueryProjection withAlias(String alias) {
    return new QueryProjection(this.operator, this.operands, this.quantifier, alias);
  }

  public SqlNode toSqlNode() {
    if (operator != null) {
      return applyAlias(new SqlBasicCall(
          new SqlUnresolvedFunction(identifierOf(operator),
              null,
              null,
              null,
              null,
              SqlFunctionCategory.NUMERIC),
          operands.stream().map(CalciteUtils::identifierOf).toArray(SqlNode[]::new),
          SqlParserPos.ZERO,
          quantifier != null ? symbolLiteralOf(quantifier) : null));
    } else if (operands.size() == 1 && quantifier == null) {
      return applyAlias(identifierOf(operands.get(0)));
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
      // 1. a datasource can customize any metricAggFunction based SQL
      if (AVAILABLE_METRIC_AGG_FUNCTIONS_NAMES.contains(operatorUpper)) {
        final MetricAggFunction metricAggFunction = MetricAggFunction.valueOf(operatorUpper);
        if (expressionBuilder.needsCustomDialect(metricAggFunction)) {
          String customDialectSql = expressionBuilder.getCustomDialectSql(metricAggFunction,
              operands,
              quantifier);
          return applyAlias(expressionToNode(customDialectSql, sqlParserConfig));
        }
      }
      // 2. COUNT DISTINCT is transformed in COUNT (DISTINCT ...)
      if (MetricAggFunction.COUNT_DISTINCT.name().equals(operator)) {
        return applyAlias(QueryProjection.of("COUNT", operands, "DISTINCT")
            .toDialectSpecificSqlNode(sqlParserConfig, expressionBuilder));
      }
    }
    // 3. default transformation - manages any well-formed projection
    return toSqlNode();
  }

  private SqlNode applyAlias(final SqlNode node) {
    if (alias == null) {
      return node;
    }
    return addAlias(node, alias);
  }

  /**
   * Creates an aggregation projection based on a metricConfig.
   */
  public static QueryProjection fromMetricConfig(MetricConfigDTO metricConfigDTO) {
    String aggFunction = Objects.requireNonNull(metricConfigDTO.getDefaultAggFunction());
    List<String> operands;
    // not sure why the logic below - kept it from legacy
    if (metricConfigDTO.getName().equals("*")) {
      operands = List.of("*");
    } else {
      operands = List.of(getFunctionName(metricConfigDTO));
    }

    return QueryProjection.of(aggFunction, operands);
  }

  // todo cyril see if it's possible to deprecrate aggregation column
  public static String getFunctionName(final MetricConfigDTO metricConfigDTO) {
    return optional(metricConfigDTO.getAggregationColumn()).orElse(metricConfigDTO.getName());
  }
}
