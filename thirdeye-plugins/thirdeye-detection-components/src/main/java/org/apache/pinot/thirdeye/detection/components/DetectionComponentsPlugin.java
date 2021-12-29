package org.apache.pinot.thirdeye.detection.components;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetectorSpec;
import org.apache.pinot.thirdeye.detection.components.detectors.HoltWintersDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.HoltWintersDetectorSpec;
import org.apache.pinot.thirdeye.detection.components.detectors.MeanVarianceRuleDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.MeanVarianceRuleDetectorSpec;
import org.apache.pinot.thirdeye.detection.components.detectors.PercentageChangeRuleDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.PercentageChangeRuleDetectorSpec;
import org.apache.pinot.thirdeye.detection.components.detectors.ThresholdRuleDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.ThresholdRuleDetectorSpec;
import org.apache.pinot.thirdeye.detection.components.triggers.ConsoleOutputTrigger;
import org.apache.pinot.thirdeye.detection.components.triggers.ConsoleOutputTriggerSpec;
import org.apache.pinot.thirdeye.detection.components.triggers.GenericEventTriggerFactory;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Factory;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactory;

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
