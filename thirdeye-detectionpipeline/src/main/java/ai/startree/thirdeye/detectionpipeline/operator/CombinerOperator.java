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
package ai.startree.thirdeye.detectionpipeline.operator;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.HashMap;
import java.util.Map;

public class CombinerOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_INPUT_KEY = "input_Combiner";
  public static final String DEFAULT_OUTPUT_KEY = "output_Combiner";

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
  }

  @Override
  public void execute() throws Exception {
    final ForkJoinResult forkJoinResult = (ForkJoinResult) requireNonNull(inputMap.get(
        DEFAULT_INPUT_KEY), "No input to combiner");
    final var forkJoinResults = forkJoinResult.getResults();

    final Map<String, OperatorResult> results = new HashMap<>();
    for (int i = 0; i < forkJoinResults.size(); i++) {
      final var result = forkJoinResults.get(i);
      final String prefix = i + ".";
      result.getResults().forEach((k, v) -> results.put(prefix + k, v));
    }
    setOutput(DEFAULT_OUTPUT_KEY, new CombinerResult(results));
  }

  @Override
  public String getOperatorName() {
    return "CombinerOperator";
  }
}
