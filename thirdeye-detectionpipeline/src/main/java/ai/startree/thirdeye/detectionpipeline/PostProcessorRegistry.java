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

import static ai.startree.thirdeye.detectionpipeline.operator.DetectionPipelineOperator.getComponentSpec;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datasource.loader.MinMaxTimeLoader;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import ai.startree.thirdeye.spi.detection.postprocessing.PostProcessingContext;
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
  private final DatasetConfigManager datasetDao;
  private final MinMaxTimeLoader minMaxTimeLoader;
  private final AnomalyManager anomalyDao;

  @Inject
  public PostProcessorRegistry(final DatasetConfigManager datasetDao,
      final MinMaxTimeLoader minMaxTimeLoader, final AnomalyManager anomalyDao) {
    this.datasetDao = datasetDao;
    this.minMaxTimeLoader = minMaxTimeLoader;
    this.anomalyDao = anomalyDao;
  }

  public void addAnomalyPostProcessorFactory(final AnomalyPostProcessorFactory f) {
    checkState(!anomalyPostProcessorFactoryMap.containsKey(f.name()),
        "Duplicate AnomalyPostProcessorFactory: " + f.name());

    anomalyPostProcessorFactoryMap.put(f.name(), f);
  }

  public AnomalyPostProcessor build(final String factoryName, final Map<String, Object> nodeParams,
      final OperatorContext context) {
    checkArgument(anomalyPostProcessorFactoryMap.containsKey(factoryName),
        String.format("Anomaly PostProcessor type not registered: %s. Available postProcessors: %s",
            factoryName,
            anomalyPostProcessorFactoryMap.keySet()));
    final Map<String, Object> componentSpec = getComponentSpec(nodeParams);

    final DetectionPipelineContext detectionPipelineContext = context.getPlanNodeContext()
        .getDetectionPipelineContext();
    final PostProcessingContext postProcessingContext = new PostProcessingContext(datasetDao,
        minMaxTimeLoader, anomalyDao,
        detectionPipelineContext.getAlertId(),
        requireNonNull(detectionPipelineContext.getUsage(), "Detection pipeline usage is not set"),
        detectionPipelineContext.getEnumerationItem()
    );
    return anomalyPostProcessorFactoryMap.get(factoryName)
        .build(componentSpec, postProcessingContext);
  }
}
