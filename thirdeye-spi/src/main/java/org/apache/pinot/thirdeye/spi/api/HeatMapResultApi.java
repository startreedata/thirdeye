package org.apache.pinot.thirdeye.spi.api;

import java.util.Map;

public class HeatMapResultApi {
  private MetricApi metric;
  private HeatMapBreakdownApi baseline;
  private HeatMapBreakdownApi current;

  public MetricApi getMetric() {
    return metric;
  }

  public HeatMapResultApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public HeatMapBreakdownApi getBaseline() {
    return baseline;
  }

  public HeatMapResultApi setBaseline(
      final HeatMapBreakdownApi baseline) {
    this.baseline = baseline;
    return this;
  }

  public HeatMapBreakdownApi getCurrent() {
    return current;
  }

  public HeatMapResultApi setCurrent(
      final HeatMapBreakdownApi current) {
    this.current = current;
    return this;
  }

  public static class HeatMapBreakdownApi {
    private Map<String, Map<String, Double>> breakdown;

    public Map<String, Map<String, Double>> getBreakdown() {
      return breakdown;
    }

    public HeatMapBreakdownApi setBreakdown(
        final Map<String, Map<String, Double>> breakdown) {
      this.breakdown = breakdown;
      return this;
    }
  }
}
