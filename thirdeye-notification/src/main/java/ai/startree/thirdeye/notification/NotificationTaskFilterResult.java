/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import java.util.Set;

public class NotificationTaskFilterResult {

  private SubscriptionGroupDTO subscriptionGroup;
  private Set<AnomalyDTO> anomalies;
  private Set<AnomalyDTO> completedAnomalies;

  public SubscriptionGroupDTO getSubscriptionGroup() {
    return subscriptionGroup;
  }

  public NotificationTaskFilterResult setSubscriptionGroup(
      final SubscriptionGroupDTO subscriptionGroup) {
    this.subscriptionGroup = subscriptionGroup;
    return this;
  }

  public Set<AnomalyDTO> getAnomalies() {
    return anomalies;
  }

  public NotificationTaskFilterResult setAnomalies(
      final Set<AnomalyDTO> anomalies) {
    this.anomalies = anomalies;
    return this;
  }

  public Set<AnomalyDTO> getCompletedAnomalies() {
    return completedAnomalies;
  }

  public NotificationTaskFilterResult setCompletedAnomalies(
      final Set<AnomalyDTO> completedAnomalies) {
    this.completedAnomalies = completedAnomalies;
    return this;
  }
}
