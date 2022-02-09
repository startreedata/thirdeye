package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetector;
import ai.startree.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.HoltWintersDetector;
import ai.startree.thirdeye.detection.components.detectors.HoltWintersDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.MeanVarianceRuleDetector;
import ai.startree.thirdeye.detection.components.detectors.MeanVarianceRuleDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.PercentageChangeRuleDetector;
import ai.startree.thirdeye.detection.components.detectors.PercentageChangeRuleDetectorSpec;
import ai.startree.thirdeye.detection.components.detectors.ThresholdRuleDetector;
import ai.startree.thirdeye.detection.components.detectors.ThresholdRuleDetectorSpec;
import ai.startree.thirdeye.detection.components.triggers.ConsoleOutputTrigger;
import ai.startree.thirdeye.detection.components.triggers.ConsoleOutputTriggerSpec;
import ai.startree.thirdeye.detection.components.triggers.GenericEventTriggerFactory;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorV2Factory;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import com.google.common.collect.ImmutableList;

public class DetectionComponentsPlugin implements Plugin {

  @Override
  public Iterable<AnomalyDetectorV2Factory> getAnomalyDetectorV2Factories() {
    return ImmutableList.of(
        new GenericAnomalyDetectorV2Factory<>(
            "PERCENTAGE_CHANGE",
            PercentageChangeRuleDetectorSpec.class,
            PercentageChangeRuleDetector.class
        ),
        new GenericAnomalyDetectorV2Factory<>(
            "HOLT_WINTERS",
            HoltWintersDetectorSpec.class,
            HoltWintersDetector.class
        ),
        new GenericAnomalyDetectorV2Factory<>(
            "ABSOLUTE_CHANGE",
            AbsoluteChangeRuleDetectorSpec.class,
            AbsoluteChangeRuleDetector.class
        ),
        new GenericAnomalyDetectorV2Factory<>(
            "THRESHOLD",
            ThresholdRuleDetectorSpec.class,
            ThresholdRuleDetector.class
        ),
        new GenericAnomalyDetectorV2Factory<>(
            "MEAN_VARIANCE",
            MeanVarianceRuleDetectorSpec.class,
            MeanVarianceRuleDetector.class
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
