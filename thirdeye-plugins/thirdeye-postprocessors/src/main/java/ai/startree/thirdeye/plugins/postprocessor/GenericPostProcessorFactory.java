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
package ai.startree.thirdeye.plugins.postprocessor;

import ai.startree.thirdeye.spi.detection.PostProcessorSpec;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import java.lang.reflect.InvocationTargetException;

public class GenericPostProcessorFactory<T extends PostProcessorSpec> implements
    AnomalyPostProcessorFactory {

  private final String name;
  private final Class<? extends AnomalyPostProcessor<T>> clazz;

  public GenericPostProcessorFactory(final String name,
      final Class<? extends AnomalyPostProcessor<T>> clazz) {
    this.clazz = clazz;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AnomalyPostProcessor<T> build() {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
