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
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.Enumerator;
import ai.startree.thirdeye.spi.detection.Enumerator.Context;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public class EnumeratorOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_INPUT_KEY = "input_Enumerator";
  public static final String DEFAULT_OUTPUT_KEY = "output_Enumerator";
  public static final String DEFAULT_ENUMERATOR_TYPE = "default";
  private DetectionRegistry detectionRegistry;
  private EnumerationItemDTO enumerationItemFromContext;

  public EnumeratorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    detectionRegistry = (DetectionRegistry) context.getProperties()
        .get(Constants.DETECTION_REGISTRY_REF_KEY);

    enumerationItemFromContext = context.getEnumerationItem();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void execute() throws Exception {
    if (enumerationItemFromContext != null) {
      /* Skip processing if context is set. happens when executing flow for a specific enumeration item */
      setOutput(DEFAULT_OUTPUT_KEY, new EnumeratorResult(List.of(enumerationItemFromContext)));
      return;
    }

    checkArgument(getPlanNode().getParams() != null,
        "Missing configuration parameters in EnumeratorOperator.");
    final Map<String, Object> paramsMap = getPlanNode().getParams().valueMap();

    final var params = new ObjectMapper().convertValue(paramsMap,
        EnumeratorParams.class);

    final String type = optional(params.getType()).orElse(DEFAULT_ENUMERATOR_TYPE);
    final Enumerator enumerator = detectionRegistry.buildEnumerator(type);
    final List<EnumerationItemDTO> items = enumerator.enumerate(new Context().setParams(paramsMap));

    /* Populate names if not present */
    final EnumerationItemNameGenerator generator = new EnumerationItemNameGenerator();
    items.stream()
        .filter(ei -> ei.getName() == null)
        .forEach(ei -> ei.setName(generator.generateName(ei)));

    setOutput(DEFAULT_OUTPUT_KEY, new EnumeratorResult(items));
  }

  @Override
  public String getOperatorName() {
    return "EnumeratorOperator";
  }

  public static class EnumeratorResult implements OperatorResult {

    private final List<EnumerationItemDTO> results;

    public EnumeratorResult(final List<EnumerationItemDTO> results) {
      this.results = results;
    }

    public List<EnumerationItemDTO> getResults() {
      return results;
    }
  }
}
