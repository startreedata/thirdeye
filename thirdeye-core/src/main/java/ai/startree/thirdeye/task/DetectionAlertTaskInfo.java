/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

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
