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
package ai.startree.thirdeye.detectionpipeline;

import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
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
  void setInput(String key, OperatorResult obj);

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
