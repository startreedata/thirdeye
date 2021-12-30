package org.apache.pinot.thirdeye.spi.detection;

public interface AnomalyDetectorV2Factory {

  String name();

  <T extends AbstractSpec>
  AnomalyDetectorV2<T> build(
      AnomalyDetectorFactoryV2Context context);

  boolean isBaselineProvider();
}
