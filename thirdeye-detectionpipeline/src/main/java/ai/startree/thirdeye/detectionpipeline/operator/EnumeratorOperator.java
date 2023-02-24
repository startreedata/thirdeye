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

import static ai.startree.thirdeye.spi.detection.DetectionPipelineUsage.DETECTION;
import static ai.startree.thirdeye.spi.detection.DetectionPipelineUsage.EVALUATION;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.Enumerator;
import ai.startree.thirdeye.spi.detection.Enumerator.Context;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumeratorOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_INPUT_KEY = "input_Enumerator";
  public static final String DEFAULT_OUTPUT_KEY = "output_Enumerator";
  public static final String DEFAULT_ENUMERATOR_TYPE = "default";
  private DetectionRegistry detectionRegistry;
  private EnumerationItemDTO enumerationItemFromContext;
  private DetectionPipelineContext detectionPipelineContext;

  public EnumeratorOperator() {
    super();
  }

  private static AlertDTO newAlert(final Long alertId) {
    final AlertDTO alert = new AlertDTO();
    alert.setId(alertId);
    return alert;
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    final PlanNodeContext nodeContext = context.getPlanNodeContext();
    detectionRegistry = requireNonNull(nodeContext.getApplicationContext().getDetectionRegistry());
    detectionPipelineContext = nodeContext.getDetectionPipelineContext();

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

    final var params = new ObjectMapper().convertValue(paramsMap, EnumeratorOperatorParams.class);

    final String type = optional(params.getType()).orElse(DEFAULT_ENUMERATOR_TYPE);
    final Enumerator enumerator = detectionRegistry.buildEnumerator(type);
    final List<EnumerationItemDTO> items = enumerator.enumerate(new Context().setParams(paramsMap));
    validate(params, items);

    /* Populate names if not present */
    final EnumerationItemNameGenerator generator = new EnumerationItemNameGenerator();
    items.stream()
        .filter(ei -> ei.getName() == null)
        .forEach(ei -> ei.setName(generator.generateName(ei)));

    /* Sync with existing enumeration items */
    final List<EnumerationItemDTO> prepared = prepare(items);

    /* Set output */
    setOutput(DEFAULT_OUTPUT_KEY, new EnumeratorResult(prepared));
  }

  private void validate(final EnumeratorOperatorParams params,
      final List<EnumerationItemDTO> items) {
    if (params.getIdKeys() != null && !params.getIdKeys().isEmpty()) {
      /* If id keys are specified, then all items must contain all the id keys. */
      final Set<String> idKeys = new HashSet<>(params.getIdKeys());
      items.forEach(ei -> checkArgument(ei.getParams().keySet().containsAll(idKeys),
          "Enumeration item " + ei + " does not contain all the id keys: " + idKeys));
    }
  }

  private List<EnumerationItemDTO> prepare(final List<EnumerationItemDTO> enumerationItems) {
    final DetectionPipelineUsage usage = requireNonNull(detectionPipelineContext.getUsage(),
        "Detection pipeline usage is not set");
    if (EVALUATION.equals(usage)) {
      // do nothing - no need to persist enumerationItems nor fetch existing one downstream
      return enumerationItems;
    }
    checkArgument(DETECTION.equals(usage), "DetectionPipelineUsage not implemented: " + usage);

    // Add alert id to enumeration items
    final Long alertId = requireNonNull(detectionPipelineContext.getAlertId(),
        "alert ID is not set");

    /* decorate enumeration item with alert id */
    final var decorated = enumerationItems.stream()
        .map(e -> e.setAlert(newAlert(alertId)))
        .collect(Collectors.toList());

    /* find existing or create new enumeration item */
    final EnumerationItemManager enumerationItemManager = detectionPipelineContext
        .getApplicationContext()
        .getEnumerationItemManager();

    return decorated.stream()
        .map(enumerationItemManager::findExistingOrCreate)
        .collect(Collectors.toList());
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
