/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnomalySubscriptionGroupNotificationDTO extends AbstractDTO {

  private Long anomalyId;
  private Long detectionConfigId;
  private List<Long> notifiedSubscriptionGroupIds = new ArrayList<>();

  public Long getAnomalyId() {
    return anomalyId;
  }

  public void setAnomalyId(Long anomalyId) {
    this.anomalyId = anomalyId;
  }

  public Long getDetectionConfigId() {
    return detectionConfigId;
  }

  public void setDetectionConfigId(Long detectionConfigId) {
    this.detectionConfigId = detectionConfigId;
  }

  public List<Long> getNotifiedSubscriptionGroupIds() {
    return notifiedSubscriptionGroupIds;
  }

  public void setNotifiedSubscriptionGroupIds(List<Long> notifiedSubscriptionGroupIds) {
    this.notifiedSubscriptionGroupIds = notifiedSubscriptionGroupIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnomalySubscriptionGroupNotificationDTO that = (AnomalySubscriptionGroupNotificationDTO) o;
    return Objects.equals(anomalyId, that.anomalyId) && Objects
        .equals(detectionConfigId, that.detectionConfigId)
        && Objects.equals(notifiedSubscriptionGroupIds, that.notifiedSubscriptionGroupIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(anomalyId, detectionConfigId, notifiedSubscriptionGroupIds);
  }

  @Override
  public String toString() {
    return "AnomalySubscriptionGroupNotificationBean{" + "anomalyId=" + anomalyId
        + ", detectionConfigId="
        + detectionConfigId + ", notifiedSubscriptionGroupIds=" + notifiedSubscriptionGroupIds
        + '}';
  }
}
