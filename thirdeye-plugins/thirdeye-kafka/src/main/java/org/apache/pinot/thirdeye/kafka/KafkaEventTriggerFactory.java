package org.apache.pinot.thirdeye.kafka;

import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.EventTrigger;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactory;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactoryContext;

public class KafkaEventTriggerFactory implements EventTriggerFactory {
  private static final String name = "KAFKA_PRODUCER";

  @Override
  public String name() {
    return name;
  }

  @Override
  public EventTrigger<KafkaProducerTriggerSpec> build(final EventTriggerFactoryContext context) {
    EventTrigger<KafkaProducerTriggerSpec> trigger = new KafkaProducerTrigger();
    trigger.init(buildSpec(context));
    return trigger;
  }

  private KafkaProducerTriggerSpec buildSpec(final EventTriggerFactoryContext context) {
    return AbstractSpec.fromProperties(context.getProperties(), KafkaProducerTriggerSpec.class);
  }
}
