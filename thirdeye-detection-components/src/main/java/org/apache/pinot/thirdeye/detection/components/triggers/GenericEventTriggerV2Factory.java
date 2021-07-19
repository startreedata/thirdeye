/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection.components.triggers;

import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerV2;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerV2Factory;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerV2FactoryContext;

/**
 * Absolute change rule detection
 */
public class GenericEventTriggerV2Factory<T extends AbstractSpec> implements
    EventTriggerV2Factory {

  private final String name;
  private final Class<T> specClazz;
  private final Class<? extends EventTriggerV2<T>> clazz;

  public GenericEventTriggerV2Factory(final String name,
      final Class<T> specClazz,
      final Class<? extends EventTriggerV2<T>> clazz) {
    this.clazz = clazz;
    this.specClazz = specClazz;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public EventTriggerV2<T> build(final EventTriggerV2FactoryContext context) {
    try {
      final EventTriggerV2<T> detector = clazz.newInstance();
      detector.init(buildSpec(context));
      return detector;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private T buildSpec(final EventTriggerV2FactoryContext context) {
    return AbstractSpec.fromProperties(context.getProperties(), specClazz);
  }
}
