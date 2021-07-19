package org.apache.pinot.thirdeye.spi.detection;

public interface EventTrigger<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Initialize Trigger
   */
  void init(T spec);

  /**
   * Trigger with one event
   */
  void trigger(Object[] event) throws EventTriggerException;
}
