/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */
package ai.startree.thirdeye.spi.detection;

/**
 * The type of anomaly.
 */
public enum AnomalyType {
  // Metric deviates from normal behavior. This is the default type.
  DEVIATION("Deviation"),
  // There is a trend change for underline metric.
  TREND_CHANGE("Trend Change"),
  // The metric is not available within specified time.
  DATA_SLA("SLA Violation");

  private final String label;

  AnomalyType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
