/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.kafka;

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;

@AutoService(Plugin.class)
public class KafkaPlugin implements Plugin {
  @Override
  public Iterable<EventTriggerFactory> getEventTriggerFactories() {
    return ImmutableList.of(
        new KafkaEventTriggerFactory()
    );
  }
}
