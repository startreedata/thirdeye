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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.model.AnomalyDetectionResult;
import ai.startree.thirdeye.spi.detection.v2.DetectionResult;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CombinerOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_INPUT_KEY = "input_Combiner";
  public static final String DEFAULT_OUTPUT_KEY = "output_Combiner";
  private Map<String, Object> params;

  public CombinerOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    params = optional(getPlanNode().getParams()).map(TemplatableMap::valueMap).orElse(emptyMap());
  }

  @Override
  public void execute() throws Exception {
    final ForkJoinResult forkJoinResult = (ForkJoinResult) requireNonNull(inputMap.get(
        DEFAULT_INPUT_KEY), "No input to combiner");
    final var forkJoinResults = forkJoinResult.getResults();

    final Map<String, DetectionResult> results = new HashMap<>();
    for (int i = 0; i < forkJoinResults.size(); i++) {
      final var result = forkJoinResults.get(i);
      final String prefix = i + ".";
      result
          .getResults()
          .forEach((k, v) -> results.put(prefix + k, wrapIfReqd(result.getEnumerationItem(), v)));
    }
    setOutput(DEFAULT_OUTPUT_KEY, new CombinerResult(results));
  }

  private DetectionResult wrapIfReqd(final EnumerationItemDTO enumerationItem,
      final DetectionResult v) {
    return v instanceof AnomalyDetectionResult ? new WrappedAnomalyDetectionResult(enumerationItem,
        (AnomalyDetectionResult) v) : v;
  }

  @Override
  public String getOperatorName() {
    return "CombinerOperator";
  }

  public static class CombinerResult implements DetectionResult {

    private final Map<String, DetectionResult> results;

    public CombinerResult(final Map<String, DetectionResult> results) {
      this.results = results;
    }

    // fixme cyril add flatenning classes

    public List<DetectionResult> getDetectionResults() {
      return results.values().stream()
          // fixme cyril suvodeep not sure to understand why only WrappedAnomalyDetectionResult are returned
          .filter(r -> r instanceof WrappedAnomalyDetectionResult)
          .collect(Collectors.toList());
    }

    public Map<String, DetectionResult> getResults() {
      return results;
    }
  }
}
