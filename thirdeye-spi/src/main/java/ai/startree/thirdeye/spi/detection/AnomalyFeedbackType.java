/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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
