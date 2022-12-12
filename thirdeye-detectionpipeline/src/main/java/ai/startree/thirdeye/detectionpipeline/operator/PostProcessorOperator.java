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

import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.PostProcessorRegistry;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PostProcessorOperator extends DetectionPipelineOperator {

  private AnomalyPostProcessor postProcessor;
  private final HashMap<String, Set<String>> combinerKeyToResultsKeys = new HashMap<>();

  public PostProcessorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    final PostProcessorRegistry postProcessorRegistry = context
        .getPlanNodeContext()
        .getApplicationContext()
        .getPostProcessorRegistry();

    final Map<String, Object> nodeParams = optional(planNode.getParams()).map(
            TemplatableMap::valueMap)
        .orElseThrow(() -> new ThirdEyeException(ERR_MISSING_CONFIGURATION_FIELD,
            "'params' in " + getOperatorName() + " " + planNode.getName()));

    final String type = optional(MapUtils.getString(nodeParams, PROP_TYPE))
        .orElseThrow(() -> new ThirdEyeException(ERR_MISSING_CONFIGURATION_FIELD,
            "'type' in 'params' of " + getOperatorName() + " " + planNode.getName()));

    postProcessor = postProcessorRegistry.build(type, nodeParams, context);
  }

  @Override
  public void execute() throws Exception {
    // split combiner results - to hide CombinerResult from PostProcessor implementations
    final Map<String, OperatorResult> inputWithCombinerResultsSplit = splitCombinerResults(
        inputMap);

    final Map<String, OperatorResult> outputsWithCombinerResultsSplit = postProcessor.postProcess(
        detectionInterval,
        inputWithCombinerResultsSplit);
    outputsWithCombinerResultsSplit.values().forEach(this::enrichAnomalyLabels);

    // merge back combiner results
    final Map<String, OperatorResult> output = mergeCombinerResults(
        outputsWithCombinerResultsSplit);

    resultMap.putAll(output);
  }

  private Map<String, OperatorResult> splitCombinerResults(Map<String, OperatorResult> resultMap) {
    final Map<String, OperatorResult> inputWithSplitCombinerResults = new HashMap<>(resultMap);
    for (final Map.Entry<String, OperatorResult> entry : resultMap.entrySet()) {
      if (entry.getValue() instanceof CombinerResult) {
        inputWithSplitCombinerResults.remove(entry.getKey());
        final Map<String, OperatorResult> combinedResults = ((CombinerResult) entry.getValue()).getResults();
        // potential key override - assumes there will not be 2 CombinerResult in the resultMap that have internal results with the same key
        inputWithSplitCombinerResults.putAll(combinedResults);
        combinerKeyToResultsKeys.put(entry.getKey(), combinedResults.keySet());
      }
    }

    return inputWithSplitCombinerResults;
  }

  private Map<String, OperatorResult> mergeCombinerResults(
      final Map<String, OperatorResult> outputsWithCombinerResultsSplit) {
    final Map<String, OperatorResult> output = new HashMap<>(outputsWithCombinerResultsSplit);
    for (final Entry<String, Set<String>> entry : combinerKeyToResultsKeys.entrySet()) {
      final Map<String, OperatorResult> combinedResults = new HashMap<>();
      for (final String resultKey : entry.getValue()) {
        combinedResults.put(resultKey, outputsWithCombinerResultsSplit.get(resultKey));
        output.remove(resultKey);
      }
      output.put(entry.getKey(), new CombinerResult(combinedResults));
    }
    return output;
  }

  private void enrichAnomalyLabels(final OperatorResult result) {
    final List<MergedAnomalyResultDTO> anomalies = result.getAnomalies();
    if (anomalies == null) {
      return;
    }

    // fixme cyril asap this is broken - all labels are overriden by downstream postProcessors - move down to each postProcessor or do a set operation
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
