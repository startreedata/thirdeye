package org.apache.pinot.thirdeye.detection.v2.operator;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.EventTrigger;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactoryContext;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class EventTriggerOperator extends DetectionPipelineOperator {

  public EventTriggerOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
  }

  @Override
  public void execute() throws Exception {
    if (component instanceof EventTrigger) {
      final Map<String, DataTable> timeSeriesMap = DetectionUtils.getTimeSeriesMap(inputMap);
      for (String inputKey : timeSeriesMap.keySet()) {
        final DataTable dataTable = timeSeriesMap.get(inputKey);
        for (int rowIdx = 0; rowIdx < dataTable.getRowCount(); rowIdx++) {
          ((EventTrigger) component).trigger(dataTable.getColumns(), dataTable.getColumnTypes(), DataTable.getRow(dataTable, rowIdx));
        }
      }
      ((EventTrigger) component).close();
    }
  }

  @Override
  public String getOperatorName() {
    return "EventTriggerOperator";
  }

  @Override
  protected BaseComponent createComponent() {
    final String type = requireNonNull(MapUtils.getString(planNode.getParams(), PROP_TYPE),
        "Must have 'type' in trigger config");
    final Map<String, Object> componentSpec = getComponentSpec(planNode.getParams());
    return new DetectionRegistry().buildTrigger(type, new EventTriggerFactoryContext()
        .setProperties(componentSpec));
  }
}
