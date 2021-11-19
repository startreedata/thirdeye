package org.apache.pinot.thirdeye.detection.v2.plan;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.CalciteSqlExecutionOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class CalciteSqlExecutionPlanNode extends DetectionPipelinePlanNode {

  public CalciteSqlExecutionPlanNode() {
    super();
  }

  @Override
  void setNestedProperties(final Map<String, Object> properties) {
  }

  @Override
  public String getType() {
    return "CalciteSqlExecution";
  }

  @Override
  public Operator run() throws Exception {
    final CalciteSqlExecutionOperator sqlExecutionOperator = new CalciteSqlExecutionOperator();
    sqlExecutionOperator.init(new OperatorContext()
        .setPlanNode(planNodeBean)
        .setInputsMap(inputsMap)
    );
    return sqlExecutionOperator;
  }
}
