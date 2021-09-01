package org.apache.pinot.thirdeye.detection.components;

import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetector;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactory;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactoryContext;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;

public class GenericAnomalyDetectorFactory<T extends AbstractSpec> implements
    AnomalyDetectorFactory {

  private final String name;
  private final Class<T> specClazz;
  private final Class<? extends AnomalyDetector<T>> clazz;

  public GenericAnomalyDetectorFactory(final String name,
      final Class<T> specClazz,
      final Class<? extends AnomalyDetector<T>> clazz) {
    this.clazz = clazz;
    this.specClazz = specClazz;
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AnomalyDetector<T> build(final AnomalyDetectorFactoryContext context) {
    try {
      final AnomalyDetector<T> detector = clazz.newInstance();

      // This is the v1 factory and is thus using the v1 API which is deprecated.
      //noinspection deprecation
      detector.init(buildSpec(context), context.getInputDataFetcher());
      return detector;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private T buildSpec(final AnomalyDetectorFactoryContext context) {
    return AbstractSpec.fromProperties(context.getProperties(), specClazz);
  }

  @Override
  public boolean isBaselineProvider() {
    return BaselineProvider.isBaselineProvider(clazz);
  }
}
