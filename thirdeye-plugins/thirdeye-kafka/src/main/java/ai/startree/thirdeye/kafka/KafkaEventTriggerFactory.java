/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.kafka;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.EventTrigger;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import ai.startree.thirdeye.spi.detection.EventTriggerFactoryContext;

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
