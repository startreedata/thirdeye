package org.apache.pinot.thirdeye.plugin.detection;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.plugin.detection.detectors.AbsoluteChangeRuleDetector;
import org.apache.pinot.thirdeye.plugin.detection.detectors.AbsoluteChangeRuleDetectorSpec;
import org.apache.pinot.thirdeye.plugin.detection.detectors.DataSlaQualityChecker;
import org.apache.pinot.thirdeye.plugin.detection.detectors.DataSlaQualityCheckerSpec;
import org.apache.pinot.thirdeye.plugin.detection.detectors.HoltWintersDetector;
import org.apache.pinot.thirdeye.plugin.detection.detectors.HoltWintersDetectorSpec;
import org.apache.pinot.thirdeye.plugin.detection.detectors.MeanVarianceRuleDetector;
import org.apache.pinot.thirdeye.plugin.detection.detectors.MeanVarianceRuleDetectorSpec;
import org.apache.pinot.thirdeye.plugin.detection.detectors.PercentageChangeRuleDetector;
import org.apache.pinot.thirdeye.plugin.detection.detectors.PercentageChangeRuleDetectorSpec;
import org.apache.pinot.thirdeye.plugin.detection.detectors.ThresholdRuleDetector;
import org.apache.pinot.thirdeye.plugin.detection.detectors.ThresholdRuleDetectorSpec;
import org.apache.pinot.thirdeye.plugin.detection.triggers.ConsoleOutputTrigger;
import org.apache.pinot.thirdeye.plugin.detection.triggers.ConsoleOutputTriggerSpec;
import org.apache.pinot.thirdeye.plugin.detection.triggers.GenericEventTriggerFactory;
import org.apache.pinot.thirdeye.plugin.detection.triggers.KafkaProducerTrigger;
import org.apache.pinot.thirdeye.plugin.detection.triggers.KafkaProducerTriggerSpec;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactory;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Factory;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactory;

public class DetectionComponentsPlugin implements Plugin {

  @Override
  public Iterable<AnomalyDetectorFactory> getAnomalyDetectorFactories() {
    return ImmutableList.of(
        new GenericAnomalyDetectorFactory<>(
            "ABSOLUTE_CHANGE_RULE",
            AbsoluteChangeRuleDetectorSpec.class,
            AbsoluteChangeRuleDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "HOLT_WINTERS_RULE",
            HoltWintersDetectorSpec.class,
            HoltWintersDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "MEAN_VARIANCE_RULE",
            MeanVarianceRuleDetectorSpec.class,
            MeanVarianceRuleDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "PERCENTAGE_RULE",
            PercentageChangeRuleDetectorSpec.class,
            PercentageChangeRuleDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "THRESHOLD",
            ThresholdRuleDetectorSpec.class,
            ThresholdRuleDetector.class
        ),
        new GenericAnomalyDetectorFactory<>(
            "DATA_SLA",
            DataSlaQualityCheckerSpec.class,
            DataSlaQualityChecker.class
        )
    );
  }

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
        ),
        new GenericEventTriggerFactory<>(
            "KAFKA_PRODUCER",
            KafkaProducerTriggerSpec.class,
            KafkaProducerTrigger.class
        )
    );
  }
}
