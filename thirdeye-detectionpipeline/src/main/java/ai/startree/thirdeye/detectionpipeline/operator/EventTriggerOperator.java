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
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.EventTrigger;
import ai.startree.thirdeye.spi.detection.EventTriggerFactoryContext;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;

public class EventTriggerOperator extends DetectionPipelineOperator {

  private EventTrigger<? extends AbstractSpec> eventTrigger;

  public EventTriggerOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    final DetectionRegistry detectionRegistry = (DetectionRegistry) context.getProperties()
        .get(Constants.DETECTION_REGISTRY_REF_KEY);
    requireNonNull(detectionRegistry, "DetectionRegistry is not set");

    eventTrigger = createEventTrigger(optional(planNode.getParams()).map(TemplatableMap::valueMap).orElse(null), detectionRegistry);
  }

  @Override
  public void execute() throws Exception {
    final Map<String, DataTable> timeSeriesMap = DetectionUtils.getDataTableMap(inputMap);
    for (String inputKey : timeSeriesMap.keySet()) {
      final DataTable dataTable = timeSeriesMap.get(inputKey);
      for (int rowIdx = 0; rowIdx < dataTable.getRowCount(); rowIdx++) {
        eventTrigger.trigger(dataTable.getColumns(),
            dataTable.getColumnTypes(),
            DataTable.getRow(dataTable, rowIdx));
      }
    }
    eventTrigger.close();
  }

  @Override
  public String getOperatorName() {
    return "EventTriggerOperator";
  }

  protected EventTrigger<? extends AbstractSpec> createEventTrigger(
      final Map<String, Object> params,
      final DetectionRegistry detectionRegistry) {
    final String type = requireNonNull(MapUtils.getString(params, PROP_TYPE),
        "Must have 'type' in trigger config");
    final Map<String, Object> componentSpec = getComponentSpec(params);
    return detectionRegistry.buildTrigger(type, new EventTriggerFactoryContext()
        .setProperties(componentSpec));
  }
}
