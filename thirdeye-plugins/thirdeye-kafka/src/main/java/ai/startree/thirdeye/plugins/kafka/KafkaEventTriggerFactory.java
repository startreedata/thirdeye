/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.kafka;

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
