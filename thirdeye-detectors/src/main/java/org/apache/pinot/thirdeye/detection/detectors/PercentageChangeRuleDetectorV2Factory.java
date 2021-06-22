package org.apache.pinot.thirdeye.detection.detectors;

import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactoryContext;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Factory;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;
import org.apache.pinot.thirdeye.spi.detection.v2.components.detector.AnomalyDetector;

class PercentageChangeRuleDetectorV2Factory implements AnomalyDetectorV2Factory {

  @Override
  public String name() {
    return "PERCENTAGE_RULE";
  }

  @Override
  public <T extends AbstractSpec> AnomalyDetector<T> build(
      final AnomalyDetectorFactoryContext context) {
    final org.apache.pinot.thirdeye.detection.v2.detectors.PercentageChangeRuleDetectorSpec spec = AbstractSpec
        .fromProperties(
            context.getProperties(),
            org.apache.pinot.thirdeye.detection.v2.detectors.PercentageChangeRuleDetectorSpec.class);
    final org.apache.pinot.thirdeye.detection.v2.detectors.PercentageChangeRuleDetector percentageChangeRuleDetector = new org.apache.pinot.thirdeye.detection.v2.detectors.PercentageChangeRuleDetector();
    percentageChangeRuleDetector.init(spec);
    return (AnomalyDetector<T>) percentageChangeRuleDetector;
  }

  @Override
  public boolean isBaselineProvider() {
    return BaselineProvider.isBaselineProvider(
        org.apache.pinot.thirdeye.detection.v2.detectors.PercentageChangeRuleDetector.class);
  }
}
