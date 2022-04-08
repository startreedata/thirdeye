/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetector;
import ai.startree.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.HoltWintersDetector;
import ai.startree.thirdeye.detection.components.detectors.HoltWintersDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.MeanVarianceRuleDetector;
import ai.startree.thirdeye.detection.components.detectors.MeanVarianceRuleDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.PercentageChangeRuleDetector;
import ai.startree.thirdeye.detection.components.detectors.PercentageChangeRuleDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.RemoteHttpDetector;
import ai.startree.thirdeye.detection.components.detectors.RemoteHttpDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.ThresholdRuleDetector;
import ai.startree.thirdeye.detection.components.detectors.ThresholdRuleDetectorSpec;
import ai.startree.thirdeye.detection.components.triggers.ConsoleOutputTrigger;
import ai.startree.thirdeye.detection.components.triggers.ConsoleOutputTriggerSpec;
import ai.startree.thirdeye.detection.components.triggers.GenericEventTriggerFactory;
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
