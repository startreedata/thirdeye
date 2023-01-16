/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.plugins.detection.components.triggers;

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
