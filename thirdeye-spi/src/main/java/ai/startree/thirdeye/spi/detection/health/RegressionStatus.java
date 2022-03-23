/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The regression status for a detection config
 */
public class RegressionStatus {

  // the average mape for each detector
  @JsonProperty
  private final Map<String, Double> detectorMapes;

  // the health status for each detector
  @JsonProperty
  private final Map<String, HealthStatus> detectorHealthStatus;

  // the overall regression health for the detection config
  @JsonProperty
  private final HealthStatus healthStatus;

  // default constructor for deserialization
  public RegressionStatus() {
    this.detectorMapes = Collections.emptyMap();
    this.detectorHealthStatus = Collections.emptyMap();
    this.healthStatus = HealthStatus.UNKNOWN;
  }

  public RegressionStatus(Map<String, Double> detectorMapes,
      Map<String, HealthStatus> detectorHealthStatus,
      HealthStatus healthStatus) {
    this.detectorMapes = detectorMapes;
    this.detectorHealthStatus = detectorHealthStatus;
    this.healthStatus = healthStatus;
  }

  public static RegressionStatus fromDetectorMapes(Map<String, Double> detectorMapes) {
    Map<String, HealthStatus> detectorHealthStatus = detectorMapes.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> classifyMapeHealth(e.getValue())));
    return new RegressionStatus(detectorMapes, detectorHealthStatus,
        classifyOverallRegressionStatus(detectorHealthStatus));
  }

  public Map<String, Double> getDetectorMapes() {
    return detectorMapes;
  }

  public Map<String, HealthStatus> getDetectorHealthStatus() {
    return detectorHealthStatus;
  }

  public HealthStatus getHealthStatus() {
    return healthStatus;
  }

  private static HealthStatus classifyMapeHealth(double mape) {
    if (Double.isNaN(mape)) {
      return HealthStatus.UNKNOWN;
    }
    if (mape < 0.2) {
      return HealthStatus.GOOD;
    }
    if (mape < 0.5) {
      return HealthStatus.MODERATE;
    }
    return HealthStatus.BAD;
  }

  /**
   * Classify the regression status of the detection config based on the health status for each
   * detector
   *
   * @param detectorHealthStatus the health status for each detector
   * @return the overall regression status
   */
  private static HealthStatus classifyOverallRegressionStatus(
      Map<String, HealthStatus> detectorHealthStatus) {
    if (detectorHealthStatus.isEmpty()) {
      return HealthStatus.UNKNOWN;
    }
    if (detectorHealthStatus.containsValue(HealthStatus.GOOD)) {
      return HealthStatus.GOOD;
    }
    if (detectorHealthStatus.containsValue(HealthStatus.MODERATE)) {
      return HealthStatus.MODERATE;
    }
    return HealthStatus.BAD;
  }
}
