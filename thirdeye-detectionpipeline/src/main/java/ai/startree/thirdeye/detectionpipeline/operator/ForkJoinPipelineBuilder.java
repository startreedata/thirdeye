/*
 * Copyright 2022 StarTree Inc
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

package ai.startree.thirdeye.detectionpipeline.operator;

import ai.startree.thirdeye.detectionpipeline.PlanNode;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.PlanNodeFactory;
import ai.startree.thirdeye.mapper.PlanNodeMapper;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.util.StringTemplateUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ForkJoinPipelineBuilder {

  public Map<String, PlanNode> clonePipelinePlanNodes(
      final Map<String, PlanNode> pipelinePlanNodes,
      final EnumerationItemDTO enumerationItem) {
    final Map<String, PlanNode> clonedPipelinePlanNodes = new HashMap<>();
    for (final Map.Entry<String, PlanNode> key : pipelinePlanNodes.entrySet()) {
      final PlanNode planNode = deepCloneWithNewContext(key.getValue(),
          enumerationItem.getParams(),
          clonedPipelinePlanNodes);
      clonedPipelinePlanNodes.put(key.getKey(), planNode);
    }
    return clonedPipelinePlanNodes;
  }

  private PlanNode deepCloneWithNewContext(final PlanNode sourceNode,
      final Map<String, Object> templateProperties,
      final Map<String, PlanNode> clonedPipelinePlanNodes) {
    try {
      /* Cloned context should contain the new nodes */
      final PlanNodeContext context = sourceNode.getContext();
      final PlanNodeContext clonedContext = PlanNodeContext.copy(context)
          .setPlanNodeBean(clonePlanNodeBean(templateProperties, context.getPlanNodeBean()))
          .setPipelinePlanNodes(clonedPipelinePlanNodes);

      return PlanNodeFactory.build(sourceNode.getClass(), clonedContext);
    } catch (final ReflectiveOperationException e) {
      throw new RuntimeException("Failed to clone PlanNode: " + sourceNode.getName(), e);
    }
  }

  private PlanNodeBean clonePlanNodeBean(final Map<String, Object> templateProperties,
      final PlanNodeBean n) {
    final TemplatableMap<String, Object> params = applyTemplatePropertiesOnParams(n.getParams(),
        templateProperties);
    return PlanNodeMapper.INSTANCE.clone(n).setParams(params);
  }

  private TemplatableMap<String, Object> applyTemplatePropertiesOnParams(
      final TemplatableMap<String, Object> params, final Map<String, Object> templateProperties) {
    if (params == null) {
      return null;
    }
    try {
      return new TemplatableMap<>(StringTemplateUtils.applyContext(params, templateProperties));
    } catch (final IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
