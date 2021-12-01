package org.apache.pinot.thirdeye.detection.v2.plan;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.SqlExecutionOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class SqlExecutionPlanNode extends DetectionPipelinePlanNode {

  public SqlExecutionPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
  }

  @Override
  void setNestedProperties(final Map<String, Object> properties) {
  }

  @Override
  public String getType() {
    return "SqlExecution";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, Object> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public Operator buildOperator() throws Exception {
    final SqlExecutionOperator sqlExecutionOperator = new SqlExecutionOperator();
    sqlExecutionOperator.init(new OperatorContext()
        .setPlanNode(planNodeBean)
        .setInputsMap(inputsMap)
    );
    return sqlExecutionOperator;
  }
}
