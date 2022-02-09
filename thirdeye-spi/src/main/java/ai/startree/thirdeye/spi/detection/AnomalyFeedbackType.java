/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
