package ai.startree.thirdeye.spi.metric;

// fixme cyril - alert evaluator getDimensionType is hardcoded to STRING - dimension type should be stored correctly at dataset onboarding time
public enum DimensionType {
  STRING,
  NUMERIC,
  BOOLEAN
}
