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
package ai.startree.thirdeye.detectionpipeline;

import static ai.startree.thirdeye.detectionpipeline.operator.DetectionPipelineOperator.getComponentSpec;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.PostProcessorSpec;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The anomaly PostProcessor registry.
 */
@Singleton
public class PostProcessorRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(PostProcessorRegistry.class);

  private final Map<String, AnomalyPostProcessorFactory> anomalyPostProcessorFactoryMap = new HashMap<>();
  private final  DatasetConfigManager datasetDao;
  private final MinMaxTimeLoader minMaxTimeLoader;

  @Inject
  public PostProcessorRegistry(final DatasetConfigManager datasetDao, final MinMaxTimeLoader minMaxTimeLoader) {
    this.datasetDao = datasetDao;
    this.minMaxTimeLoader = minMaxTimeLoader;
  }

  public void addAnomalyPostProcessorFactory(final AnomalyPostProcessorFactory f) {
    checkState(!anomalyPostProcessorFactoryMap.containsKey(f.name()),
        "Duplicate AnomalyPostProcessorFactory: " + f.name());

    anomalyPostProcessorFactoryMap.put(f.name(), f);
  }

  public AnomalyPostProcessor<PostProcessorSpec> build(final String factoryName, final Map<String, Object> nodeParams) {
    checkArgument(anomalyPostProcessorFactoryMap.containsKey(factoryName),
        String.format("Anomaly PostProcessor type not registered: %s. Available postProcessors: %s",
            factoryName,
            anomalyPostProcessorFactoryMap.keySet()));
    final AnomalyPostProcessor<PostProcessorSpec> postProcessor = anomalyPostProcessorFactoryMap.get(
        factoryName).build();
    final Map<String, Object> componentSpec = getComponentSpec(nodeParams);
    final PostProcessorSpec postProcessorSpec = AbstractSpec.fromProperties(componentSpec,
        postProcessor.specClass());
    postProcessorSpec.setDatasetConfigManager(datasetDao);
    postProcessorSpec.setMinMaxTimeLoader(minMaxTimeLoader);

    postProcessor.init(postProcessorSpec);

    return postProcessor;
  }
}
