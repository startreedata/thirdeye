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

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessorFactory;
import com.google.auto.service.AutoService;
import java.util.List;

@AutoService(Plugin.class)
public class PostProcessorsPlugin implements Plugin {

  @Override
  public Iterable<AnomalyPostProcessorFactory> getAnomalyPostProcessorFactories() {
    return List.of(
        new GenericPostProcessorFactory<>(ColdStartPostProcessor.NAME, ColdStartPostProcessor.class),
        new GenericPostProcessorFactory<>(TimeOfWeekPostProcessor.NAME, TimeOfWeekPostProcessor.class),
        new GenericPostProcessorFactory<>(ThresholdPostProcessor.NAME, ThresholdPostProcessor.class),
        new GenericPostProcessorFactory<>(EventPostProcessor.NAME, EventPostProcessor.class)
    );
  }
}
