package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactory;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactoryContext;
import ai.startree.thirdeye.spi.detection.BaselineProvider;

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
      detector.init(buildSpec(context));
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
