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
package ai.startree.thirdeye.plugins.detection.components;

import ai.startree.thirdeye.plugins.detection.components.detectors.AbsoluteChangeRuleDetector;
import ai.startree.thirdeye.plugins.detection.components.detectors.AbsoluteChangeRuleDetectorSpec;
import ai.startree.thirdeye.plugins.detection.components.detectors.HoltWintersDetector;
import ai.startree.thirdeye.plugins.detection.components.detectors.HoltWintersDetectorSpec;
import ai.startree.thirdeye.plugins.detection.components.detectors.MeanVarianceRuleDetector;
import ai.startree.thirdeye.plugins.detection.components.detectors.MeanVarianceRuleDetectorSpec;
import ai.startree.thirdeye.plugins.detection.components.detectors.PercentageChangeRuleDetector;
import ai.startree.thirdeye.plugins.detection.components.detectors.PercentageChangeRuleDetectorSpec;
import ai.startree.thirdeye.plugins.detection.components.detectors.RemoteHttpDetector;
import ai.startree.thirdeye.plugins.detection.components.detectors.RemoteHttpDetectorSpec;
import ai.startree.thirdeye.plugins.detection.components.detectors.ThresholdRuleDetector;
import ai.startree.thirdeye.plugins.detection.components.detectors.ThresholdRuleDetectorSpec;
import ai.startree.thirdeye.plugins.detection.components.triggers.ConsoleOutputTrigger;
import ai.startree.thirdeye.plugins.detection.components.triggers.ConsoleOutputTriggerSpec;
import ai.startree.thirdeye.plugins.detection.components.triggers.GenericEventTriggerFactory;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactory;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;

@AutoService(Plugin.class)
public class DetectionComponentsPlugin implements Plugin {

  @Override
  public Iterable<AnomalyDetectorFactory> getAnomalyDetectorFactories() {
    return ImmutableList.of(
        new GenericAnomalyDetectorFactory<>(
            "PERCENTAGE_CHANGE",
            PercentageChangeRuleDetectorSpec.class,
            PercentageChangeRuleDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "HOLT_WINTERS",
            HoltWintersDetectorSpec.class,
            HoltWintersDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "ABSOLUTE_CHANGE",
            AbsoluteChangeRuleDetectorSpec.class,
            AbsoluteChangeRuleDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "THRESHOLD",
            ThresholdRuleDetectorSpec.class,
            ThresholdRuleDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "MEAN_VARIANCE",
            MeanVarianceRuleDetectorSpec.class,
            MeanVarianceRuleDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "REMOTE_HTTP",
            RemoteHttpDetectorSpec.class,
            RemoteHttpDetector.class
        )
    );
  }

  @Override
  public Iterable<EventTriggerFactory> getEventTriggerFactories() {
    return ImmutableList.of(
        new GenericEventTriggerFactory<>(
            "CONSOLE_OUTPUT",
            ConsoleOutputTriggerSpec.class,
            ConsoleOutputTrigger.class
        )
    );
  }
}
