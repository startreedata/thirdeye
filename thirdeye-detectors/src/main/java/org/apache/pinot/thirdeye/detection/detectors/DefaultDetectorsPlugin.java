package org.apache.pinot.thirdeye.detection.detectors;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactory;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Factory;

public class DefaultDetectorsPlugin implements Plugin {

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
        new PercentageChangeRuleDetectorV2Factory()
    );
  }
}
