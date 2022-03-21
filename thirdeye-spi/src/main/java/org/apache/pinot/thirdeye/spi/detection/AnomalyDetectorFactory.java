package org.apache.pinot.thirdeye.spi.detection;

public interface AnomalyDetectorFactory {

  String name();

  <T extends AbstractSpec> AnomalyDetector<T> build(AnomalyDetectorFactoryContext context);

  boolean isBaselineProvider();
}
