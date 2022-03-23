/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

public enum AnomalyFeedbackType {
  ANOMALY("Confirmed Anomaly"),
  ANOMALY_EXPECTED("Expected Anomaly"),
  NOT_ANOMALY("False Alarm"),
  ANOMALY_NEW_TREND("New Trend"),
  NO_FEEDBACK("Not Resolved");

  String userReadableName;

  AnomalyFeedbackType(String userReadableName) {
    this.userReadableName = userReadableName;
  }

  public String getUserReadableName() {
    return this.userReadableName;
  }

  public boolean isAnomaly() {
    return this.equals(ANOMALY) || this.equals(ANOMALY_EXPECTED) || this.equals(ANOMALY_NEW_TREND);
  }

  public boolean isNotAnomaly() {
    return this.equals(NOT_ANOMALY);
  }

  public boolean isUnresolved() {
    return this.equals(NO_FEEDBACK);
  }
}
