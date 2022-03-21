/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.v2.operator;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detection.annotation.registry.DetectionRegistry;
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
    eventTrigger = createEventTrigger(planNode.getParams());
  }

  @Override
  public void execute() throws Exception {
    final Map<String, DataTable> timeSeriesMap = DetectionUtils.getTimeSeriesMap(inputMap);
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
      final Map<String, Object> params) {
    final String type = requireNonNull(MapUtils.getString(params, PROP_TYPE),
        "Must have 'type' in trigger config");
    final Map<String, Object> componentSpec = getComponentSpec(params);
    return new DetectionRegistry().buildTrigger(type, new EventTriggerFactoryContext()
        .setProperties(componentSpec));
  }
}
