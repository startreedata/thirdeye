package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import java.util.List;

public interface EventTrigger<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Initialize Trigger
   */
  void init(T spec);

  /**
   * Trigger with one event
   */
  void trigger(List<String> columnNames, List<ColumnType> columnTypes, Object[] event) throws EventTriggerException;

  /**
   * Close the Trigger
   */
  void close();
}
