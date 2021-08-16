package org.apache.pinot.thirdeye.plugin.detection.trigger.kafka;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.EventTrigger;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactory;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactoryContext;

public class KafkaProducerPlugin implements Plugin {

  @Override
  public Iterable<EventTriggerFactory> getEventTriggerFactories() {
    return ImmutableList.of(new EventTriggerFactory() {
      @Override
      public String name() {
        return "KAFKA_PRODUCER";
      }

      @Override
      public <T extends AbstractSpec> EventTrigger<T> build(
          final EventTriggerFactoryContext context) {
        EventTrigger trigger = new KafkaProducerTrigger();
        trigger.init(AbstractSpec.fromProperties(context.getProperties(), KafkaProducerTriggerSpec.class));
        return trigger;
      }
    });
  }
}


