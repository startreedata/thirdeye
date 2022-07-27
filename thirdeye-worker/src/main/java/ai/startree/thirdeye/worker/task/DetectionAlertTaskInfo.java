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
package ai.startree.thirdeye.worker.task;

import ai.startree.thirdeye.spi.task.TaskInfo;

/**
 * The Detection alert task info.
 */
public class DetectionAlertTaskInfo implements TaskInfo {

  private long detectionAlertConfigId;

  public DetectionAlertTaskInfo() {
  }

  public DetectionAlertTaskInfo(long detectionAlertConfigId) {
    this.detectionAlertConfigId = detectionAlertConfigId;
  }

  public long getDetectionAlertConfigId() {
    return detectionAlertConfigId;
  }

  public void setDetectionAlertConfigId(long detectionAlertConfigId) {
    this.detectionAlertConfigId = detectionAlertConfigId;
  }
}
