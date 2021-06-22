package org.apache.pinot.thirdeye.spi.detection;

public interface AnomalyDetectorV2Factory {

  String name();

  <T extends AbstractSpec>
  org.apache.pinot.thirdeye.spi.detection.v2.components.detector.AnomalyDetector<T> build(
      AnomalyDetectorFactoryContext context);

  boolean isBaselineProvider();
}
