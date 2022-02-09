package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactoryV2Context;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorV2;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorV2Factory;
import ai.startree.thirdeye.spi.detection.BaselineProvider;

public class GenericAnomalyDetectorV2Factory<T extends AbstractSpec> implements
    AnomalyDetectorV2Factory {

  private final String name;
  private final Class<T> specClazz;
  private final Class<? extends AnomalyDetectorV2<T>> clazz;

  public GenericAnomalyDetectorV2Factory(final String name,
      final Class<T> specClazz,
      final Class<? extends AnomalyDetectorV2<T>> clazz) {
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
  public AnomalyDetectorV2<T> build(final AnomalyDetectorFactoryV2Context context) {
    try {
      final AnomalyDetectorV2<T> detector = clazz.newInstance();
      detector.init(buildSpec(context));
      return detector;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private T buildSpec(final AnomalyDetectorFactoryV2Context context) {
    return AbstractSpec.fromProperties(context.getProperties(), specClazz);
  }

  @Override
  public boolean isBaselineProvider() {
    return BaselineProvider.isBaselineProvider(clazz);
  }
}
