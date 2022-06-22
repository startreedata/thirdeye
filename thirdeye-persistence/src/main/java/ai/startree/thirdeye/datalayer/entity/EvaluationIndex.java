/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.datalayer.entity;

public class EvaluationIndex extends AbstractIndexEntity {

  private long detectionConfigId;
  private long startTime;
  private long endTime;
  private String detectorName;
  private double mape;

  public long getDetectionConfigId() {
    return detectionConfigId;
  }

  public void setDetectionConfigId(long detectionConfigId) {
    this.detectionConfigId = detectionConfigId;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public String getDetectorName() {
    return detectorName;
  }

  public void setDetectorName(String detectorName) {
    this.detectorName = detectorName;
  }

  public double getMape() {
    return mape;
  }

  public void setMape(double mape) {
    this.mape = mape;
  }
}
