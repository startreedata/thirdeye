package org.apache.pinot.thirdeye.detection.detectors;

import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetector;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactory;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactoryContext;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;

public class PercentageChangeRuleDetectorFactory implements AnomalyDetectorFactory {

  @Override
  public String name() {
    return "PERCENTAGE_RULE";
  }

  @Override
  public <T extends AbstractSpec> AnomalyDetector<T> build(
      final AnomalyDetectorFactoryContext context) {
    PercentageChangeRuleDetector detector = new PercentageChangeRuleDetector();
    final PercentageChangeRuleDetectorSpec spec = AbstractSpec.fromProperties(
        context.getProperties(),
        PercentageChangeRuleDetectorSpec.class);
    detector.init(spec, context.getInputDataFetcher());
    return (AnomalyDetector<T>) detector;
  }

  @Override
  public boolean isBaselineProvider() {
    return BaselineProvider.isBaselineProvider(PercentageChangeRuleDetector.class);
  }
}
