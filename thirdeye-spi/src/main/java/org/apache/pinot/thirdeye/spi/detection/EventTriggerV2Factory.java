package org.apache.pinot.thirdeye.spi.detection;

public interface EventTriggerV2Factory {

  String name();

  <T extends AbstractSpec>
  EventTriggerV2<T> build(EventTriggerV2FactoryContext context);
}
