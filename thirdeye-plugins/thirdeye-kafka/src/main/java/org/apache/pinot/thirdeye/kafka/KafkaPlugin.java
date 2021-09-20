package org.apache.pinot.thirdeye.kafka;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactory;

public class KafkaPlugin implements Plugin {
  @Override
  public Iterable<EventTriggerFactory> getEventTriggerFactories() {
    return ImmutableList.of(
        new KafkaEventTriggerFactory()
    );
  }
}
