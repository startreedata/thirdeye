package org.apache.pinot.thirdeye.detection.v2.operator;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerV2;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerV2FactoryContext;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class EventTriggerOperator extends DetectionPipelineOperator<DataTable> {

  public EventTriggerOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
  }

  @Override
  public void execute() throws Exception {
    for (Object key : this.getComponents().keySet()) {
      final BaseComponent component = this.getComponents().get(key);
      if (component instanceof EventTriggerV2) {

        final Map<String, DataTable> timeSeriesMap = DetectionUtils.getTimeSeriesMap(inputMap);
        for (String inputKey : timeSeriesMap.keySet()) {
          final DataTable dataTable = timeSeriesMap.get(inputKey);
          for (int rowIdx = 0; rowIdx < dataTable.getRowCount(); rowIdx++) {
            ((EventTriggerV2) component).trigger(DataTable.getRow(dataTable, rowIdx));
          }
        }
      }
    }
  }

  @Override
  public String getOperatorName() {
    return "EventTriggerOperator";
  }

  @Override
  protected BaseComponent createComponentUsingFactory(final String type,
      final Map<String, Object> componentSpec) {
    return new DetectionRegistry().buildTriggerV2(type, new EventTriggerV2FactoryContext()
        .setProperties(componentSpec));
  }
}
