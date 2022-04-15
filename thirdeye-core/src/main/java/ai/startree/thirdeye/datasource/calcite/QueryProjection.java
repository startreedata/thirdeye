package ai.startree.thirdeye.datasource.calcite;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.CalciteUtils.identifierOf;
import static ai.startree.thirdeye.util.CalciteUtils.stringLiteralOf;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import ai.startree.thirdeye.util.CalciteUtils;
import java.util.List;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlUnresolvedFunction;
import org.apache.calcite.sql.parser.SqlParserPos;

// todo cyril rename this to predicate
public class QueryProjection {

  final private String operator;
  final private List<String> operands;
  final private String quantifier;

  private QueryProjection(String operator, List<String> operands, String quantifier) {
    this.operator = operator;
    this.operands = operands;
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

  public static QueryProjection fromMetricConfig(MetricConfigDTO metricConfigDTO) {
    String operator;
    List<String> operands;
    String quantifier = null;
    if (metricConfigDTO.getName().equals("*")) {
      operands = List.of("*");
    } else {
      operands = List.of(optional(metricConfigDTO.getAggregationColumn()).orElse(metricConfigDTO.getName()));
    }
    if (metricConfigDTO.getDefaultAggFunction() == MetricAggFunction.COUNT_DISTINCT) {
      operator = MetricAggFunction.COUNT.name();
      quantifier = "DISTINCT";
    } else {
      // fixme cyril this function should depend on the datasource
      operator = CalciteRequest.convertAggFunction(metricConfigDTO.getDefaultAggFunction());
    }

    return new QueryProjection(operator, operands, quantifier);
  }
}
