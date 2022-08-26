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
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.components.EventDataFetcher;
import ai.startree.thirdeye.detectionpipeline.spec.EventFetcherSpec;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.DataFetcher;
import ai.startree.thirdeye.spi.detection.model.DetectionPipelineResultImpl;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import java.util.Map;

public class EventFetcherOperator extends DetectionPipelineOperator {

  private static final String OPERATOR_NAME = "EventFetcherOperator";

  private DataFetcher<EventFetcherSpec> eventFetcher;

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    final EventManager eventDao = (EventManager) context.getProperties()
        .get(Constants.EVENT_MANAGER_REF_KEY);
    this.eventFetcher = createEventFetcher(optional(planNode.getParams()).map(TemplatableMap::valueMap)
        .orElse(null), eventDao);

    checkArgument(inputMap == null || inputMap.size() == 0,
        OPERATOR_NAME + " must have exactly 0 input node.");
    checkArgument(outputKeyMap.size() == 1,
        OPERATOR_NAME + " must have exactly 1 output node.");
  }

  @Override
  public void execute() throws Exception {
    final DataTable dataTable = eventFetcher.getDataTable(detectionInterval);
    resultMap.put(outputKeyMap.values().iterator().next(),
        DetectionPipelineResultImpl.of(dataTable));
  }

  @Override
  public String getOperatorName() {
    return OPERATOR_NAME;
  }

  private DataFetcher<EventFetcherSpec> createEventFetcher(
      final Map<String, Object> params, final EventManager eventDao) {
    final Map<String, Object> componentSpec = getComponentSpec(params);
    final EventFetcherSpec spec = requireNonNull(
        AbstractSpec.fromProperties(componentSpec, EventFetcherSpec.class),
        "Unable to construct EventFetcherSpec");
    spec.setEventManager(eventDao);

    final DataFetcher<EventFetcherSpec> eventFetcher = new EventDataFetcher();
    eventFetcher.init(spec);

    return eventFetcher;
  }
}
