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
package ai.startree.thirdeye.detectionpipeline.plan;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.PostProcessorRegistry;
import ai.startree.thirdeye.detectionpipeline.operator.PostProcessorOperator;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class PostProcessorPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "PostProcessor";
  private PostProcessorRegistry postProcessorRegistry;
  private DatasetConfigManager datasetDao;
  private MinMaxTimeLoader minMaxTimeLoader;

  public PostProcessorPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    postProcessorRegistry = (PostProcessorRegistry) planNodeContext.getProperties()
        .get(Constants.POST_PROCESSOR_REGISTRY_REF_KEY);
    datasetDao = (DatasetConfigManager) planNodeContext.getProperties()
        .get(Constants.DATASET_DAO_REF_KEY);
    minMaxTimeLoader = (MinMaxTimeLoader) planNodeContext.getProperties()
        .get(Constants.MIN_MAX_TIME_LOADER_REF_KEY);

    requireNonNull(postProcessorRegistry, "PostProcessorRegistry is not set");
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Map<String, Object> getParams() {
    return optional(planNodeBean.getParams()).map(TemplatableMap::valueMap).orElse(null);
  }

  @Override
  public Operator buildOperator() throws Exception {
    final PostProcessorOperator postProcessorOperator = new PostProcessorOperator();
    postProcessorOperator.init(new OperatorContext().setDetectionInterval(this.detectionInterval)
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
        .setProperties(ImmutableMap.<String, Object>builder()
            .put(Constants.POST_PROCESSOR_REGISTRY_REF_KEY, postProcessorRegistry)
            .put(Constants.DATASET_DAO_REF_KEY, datasetDao)
            .put(Constants.MIN_MAX_TIME_LOADER_REF_KEY, minMaxTimeLoader)
            .build()));

    return postProcessorOperator;
  }
}
