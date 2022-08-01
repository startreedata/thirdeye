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

import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnumeratorOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_INPUT_KEY = "input_Enumerator";
  public static final String DEFAULT_OUTPUT_KEY = "output_Enumerator";

  public EnumeratorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void execute() throws Exception {
    final Map<String, Templatable<Object>> params = getPlanNode().getParams();
    final List<Map<String, Object>> enumerationList =
        (List<Map<String, Object>>) Objects.requireNonNull(params.get("enumerationList")).value();
    setOutput(DEFAULT_OUTPUT_KEY, new EnumeratorResult(enumerationList));
  }

  @Override
  public String getOperatorName() {
    return "EnumeratorOperator";
  }

  public static class EnumeratorResult implements DetectionPipelineResult {

    private final List<Map<String, Object>> results;

    public EnumeratorResult(final List<Map<String, Object>> results) {
      this.results = results;
    }

    @Override
    public List<DetectionResult> getDetectionResults() {
      return null;
    }

    public List<Map<String, Object>> getResults() {
      return results;
    }
  }
}
