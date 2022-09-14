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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.PostProcessorRegistry;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PostProcessorOperator extends DetectionPipelineOperator {

  private AnomalyPostProcessor<AbstractSpec> postProcessor;
  private final HashMap<String, Set<String>> combinerKeyToResultsKeys = new HashMap<>();

  public PostProcessorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    final PostProcessorRegistry postProcessorRegistry = (PostProcessorRegistry) context.getProperties()
        .get(Constants.POST_PROCESSOR_REGISTRY_REF_KEY);
    requireNonNull(postProcessorRegistry, "PostProcessorRegistry is not set");

    final Map<String, Object> nodeParams = optional(planNode.getParams()).map(TemplatableMap::valueMap)
        .orElseThrow(() -> new ThirdEyeException(ERR_MISSING_CONFIGURATION_FIELD,
            "'type' in " + getOperatorName() + "params"));

    final String type = ensureExists(MapUtils.getString(nodeParams, PROP_TYPE),
        ERR_MISSING_CONFIGURATION_FIELD,
        "'type' in " + getOperatorName() + "params");

    postProcessor = postProcessorRegistry.getAnomalyPostProcessor(type);
    final Map<String, Object> componentSpec = getComponentSpec(nodeParams);
    final AbstractSpec abstractSpec = AbstractSpec.fromProperties(componentSpec,
        postProcessor.specClass());
    postProcessor.init(abstractSpec);
  }

  @Override
  public void execute() throws Exception {
    // split combiner results - to add combinerResult from PostProcessor implementations
    final Map<String, OperatorResult> inputWithSplitCombinerResults = splitCombinerResults(inputMap);

    final Map<String, OperatorResult> outputsWithSplitCombinerResults = postProcessor.postProcess(
        detectionInterval,
        inputWithSplitCombinerResults);
    outputsWithSplitCombinerResults.values().forEach(this::enrichAnomalyLabels);

    // merge back combiner results
    final Map<String, OperatorResult> output = mergeCombinerResults(outputsWithSplitCombinerResults);

    resultMap.putAll(output);
  }

  private Map<String, OperatorResult> splitCombinerResults(Map<String, OperatorResult> resultMap) {
    final Map<String, OperatorResult> inputWithSplitCombinerResults = new HashMap<>(resultMap);
    for (final Map.Entry<String, OperatorResult> entry : resultMap.entrySet()) {
      if (entry.getValue() instanceof CombinerResult) {
        inputWithSplitCombinerResults.remove(entry.getKey());
        final Map<String, OperatorResult> combinedResults = ((CombinerResult) entry.getValue()).getResults();
        // potential key override - assumes there will not be 2 CombinerResult in the resultMap that have internal results with the same key - at implementation time this can't happen
        inputWithSplitCombinerResults.putAll(combinedResults);
        combinerKeyToResultsKeys.put(entry.getKey(), combinedResults.keySet());
      }
    }

    return inputWithSplitCombinerResults;
  }

  private Map<String, OperatorResult> mergeCombinerResults(
      final Map<String, OperatorResult> outputsWithSplitCombinerResults) {
    final Map<String, OperatorResult> output = new HashMap<>(outputsWithSplitCombinerResults);
    for (final Entry<String, Set<String>> entry : combinerKeyToResultsKeys.entrySet()) {
      final Map<String, OperatorResult> combinedResults = new HashMap<>();
      for (final String resultKey : entry.getValue()) {
        combinedResults.put(resultKey, outputsWithSplitCombinerResults.get(resultKey));
        output.remove(resultKey);
      }
      output.put(entry.getKey(), new CombinerResult(combinedResults));
    }
    return output;
  }

  private void enrichAnomalyLabels(final OperatorResult result) {
    final List<MergedAnomalyResultDTO> anomalies;
    // todo cyril default implementation of getAnomalies throws error - obliged to catch here
    try {
      anomalies = result.getAnomalies();
    } catch (final UnsupportedOperationException e) {
      // no anomalies
      return;
    }
    if (anomalies == null) {
      return;
    }

    for (final MergedAnomalyResultDTO anomaly : anomalies) {
      final @Nullable List<AnomalyLabelDTO> anomalyLabels = anomaly.getAnomalyLabels();
      if (anomalyLabels == null) {
        continue;
      }
      for (final AnomalyLabelDTO label : anomalyLabels) {
        label.setSourcePostProcessor(postProcessor.name());
        label.setSourceNodeName(planNode.getName());
      }
    }
  }

  @Override
  public String getOperatorName() {
    return "PostProcessorOperator";
  }
}
