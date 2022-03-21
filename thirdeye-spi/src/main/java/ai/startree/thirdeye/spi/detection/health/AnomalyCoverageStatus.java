/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.health;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The anomaly coverage status for a detection config
 */
public class AnomalyCoverageStatus {

  // the anomaly coverage ratio. the percentage of anomalous duration in the duration of the whole window
  @JsonProperty
  private final double anomalyCoverageRatio;

  // the health status of the anomaly coverage ratio
  @JsonProperty
  private final HealthStatus healthStatus;

  private static final double COVERAGE_RATIO_BAD_UPPER_LIMIT = 0.85;
  private static final double COVERAGE_RATIO_BAD_LOWER_LIMIT = 0.01;
  private static final double COVERAGE_RATIO_MODERATE_LIMIT = 0.5;

  // default constructor for deserialization
  public AnomalyCoverageStatus() {
    this.anomalyCoverageRatio = Double.NaN;
    this.healthStatus = HealthStatus.UNKNOWN;
  }

  public AnomalyCoverageStatus(double anomalyCoverageRatio, HealthStatus healthStatus) {
    this.anomalyCoverageRatio = anomalyCoverageRatio;
    this.healthStatus = healthStatus;
  }

  public static AnomalyCoverageStatus fromCoverageRatio(double anomalyCoverageRatio) {
    return new AnomalyCoverageStatus(anomalyCoverageRatio,
        classifyCoverageStatus(anomalyCoverageRatio));
  }

  private static HealthStatus classifyCoverageStatus(double anomalyCoverageRatio) {
    if (Double.isNaN(anomalyCoverageRatio)) {
      return HealthStatus.UNKNOWN;
    }
    if (anomalyCoverageRatio > COVERAGE_RATIO_BAD_UPPER_LIMIT
        || anomalyCoverageRatio < COVERAGE_RATIO_BAD_LOWER_LIMIT) {
      return HealthStatus.BAD;
    }
    if (anomalyCoverageRatio > COVERAGE_RATIO_MODERATE_LIMIT) {
      return HealthStatus.MODERATE;
    }
    return HealthStatus.GOOD;
  }

  public double getAnomalyCoverageRatio() {
    return anomalyCoverageRatio;
  }

  public HealthStatus getHealthStatus() {
    return healthStatus;
  }
}
