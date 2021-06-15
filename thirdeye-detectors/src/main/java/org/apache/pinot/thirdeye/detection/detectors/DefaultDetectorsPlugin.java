package org.apache.pinot.thirdeye.detection.detectors;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactory;

public class DefaultDetectorsPlugin implements Plugin {

  @Override
  public Iterable<AnomalyDetectorFactory> getAnomalyDetectorFactories() {
    return ImmutableList.of(
        new PercentageChangeRuleDetectorFactory()
    );
  }
}
