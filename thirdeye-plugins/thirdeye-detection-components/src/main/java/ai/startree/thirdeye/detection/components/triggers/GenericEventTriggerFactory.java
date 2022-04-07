/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.triggers;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.EventTrigger;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import ai.startree.thirdeye.spi.detection.EventTriggerFactoryContext;

/**
 * Absolute change rule detection
 */
public class GenericEventTriggerFactory<T extends AbstractSpec> implements
    EventTriggerFactory {

  private final String name;
  private final Class<T> specClazz;
  private final Class<? extends EventTrigger<T>> clazz;

  public GenericEventTriggerFactory(final String name,
      final Class<T> specClazz,
      final Class<? extends EventTrigger<T>> clazz) {
    this.clazz = clazz;
    this.specClazz = specClazz;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public EventTrigger<T> build(final EventTriggerFactoryContext context) {
    try {
      final EventTrigger<T> detector = clazz.newInstance();
      detector.init(buildSpec(context));
      return detector;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private T buildSpec(final EventTriggerFactoryContext context) {
    return AbstractSpec.fromProperties(context.getProperties(), specClazz);
  }
}
