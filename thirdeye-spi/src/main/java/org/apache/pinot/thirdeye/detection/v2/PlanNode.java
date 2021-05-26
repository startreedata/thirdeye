package org.apache.pinot.thirdeye.detection.v2;

import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.api.v2.DetectionPlanApi.InputApi;

/**
 * The <code>PlanNode</code> is a single execution plan node inside the Plan tree.
 */
public interface PlanNode {

  /**
   * Initialize PlanNode with Context.
   *
   * @param planNodeContext
   */
  void init(PlanNodeContext planNodeContext);

  /**
   *
   * @return unique PlanNode name
   */
  String getName();

  /**
   *
   * @return PlanNode type
   */
  String getType();

  /**
   * Set one Input from the other Operator
   *
   * @param key
   * @param obj
   */
  void setInput(String key, DetectionPipelineResult obj);

  /**
   *
   * @return All Inputs set
   */
  List<InputApi> getPlanNodeInputs();

  /**
   *
   * @return all params
   */
  Map<String, Object> getParams();

  /**
   * Get the execution operator associated with the PlanNode.
   *
   * @return execution operator.
   */
  Operator<? extends DetectionPipelineResult> run()
      throws Exception;

}
