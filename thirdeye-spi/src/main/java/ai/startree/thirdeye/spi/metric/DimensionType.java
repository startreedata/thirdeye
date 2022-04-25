package ai.startree.thirdeye.spi.metric;

// fixme cyril - alert evaluator getDimensionType is hardcoed to STRING - dimension type is not implemented correctly in onboarder
// fixme cyril add dimensionType to ConfigGenerator and dimension object in dataset
// fixme cyril put this in the spi
public enum DimensionType {
  STRING,
  NUMERIC,
  BOOLEAN
}
