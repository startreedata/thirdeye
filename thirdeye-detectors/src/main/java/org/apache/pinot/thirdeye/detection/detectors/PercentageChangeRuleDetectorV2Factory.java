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
    final PercentageChangeRuleDetectorSpecV2 spec = AbstractSpec.fromProperties(
        context.getProperties(),
        PercentageChangeRuleDetectorSpecV2.class);
    final PercentageChangeRuleDetectorV2 percentageChangeRuleDetectorV2 = new PercentageChangeRuleDetectorV2();
    percentageChangeRuleDetectorV2.init(spec);
    return (AnomalyDetector<T>) percentageChangeRuleDetectorV2;
  }

  @Override
  public boolean isBaselineProvider() {
    return BaselineProvider.isBaselineProvider(PercentageChangeRuleDetectorV2.class);
  }
}
