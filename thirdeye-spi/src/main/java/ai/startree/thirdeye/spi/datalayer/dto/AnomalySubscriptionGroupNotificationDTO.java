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
