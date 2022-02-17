/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.entity;

public class AnomalySubscriptionGroupNotificationIndex extends AbstractIndexEntity {

  long anomalyId;
  long detectionConfigId;

  public long getAnomalyId() {
    return anomalyId;
  }

  public void setAnomalyId(long anomalyId) {
    this.anomalyId = anomalyId;
  }

  public long getDetectionConfigId() {
    return detectionConfigId;
  }

  public void setDetectionConfigId(long detectionConfigId) {
    this.detectionConfigId = detectionConfigId;
  }
}
