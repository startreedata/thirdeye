package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import java.util.List;
import java.util.Map;

/**
 * The <code>PlanNode</code> is a single execution plan node inside the Plan tree.
 */
public interface PlanNode {

  /**
   * Initialize PlanNode with Context.
   */
  void init(PlanNodeContext planNodeContext);

  /**
   * @return PlanNodeContext context using which this object was created.
   */
  PlanNodeContext getContext();

  /**
   * @return unique PlanNode name
   */
  String getName();

  /**
   * @return PlanNode type
   */
  String getType();

  /**
   * Set one Input from the other Operator
   */
  void setInput(String key, DetectionPipelineResult obj);

  /**
   * @return All Inputs set
   */
  List<InputBean> getPlanNodeInputs();

  /**
   * @return all params
   */
  Map<String, Object> getParams();

  /**
   * Get the execution operator associated with the PlanNode.
   *
   * @return execution operator.
   */
  Operator buildOperator() throws Exception;
}
