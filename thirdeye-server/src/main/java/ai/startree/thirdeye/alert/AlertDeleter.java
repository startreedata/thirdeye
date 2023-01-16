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
package ai.startree.thirdeye.alert;

import static java.util.Collections.singleton;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AlertDeleter {

  private final AlertManager alertManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  @Inject
  public AlertDeleter(final AlertManager alertManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.alertManager = alertManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  public void delete(final AlertDTO dto) {
    final Long alertId = dto.getId();

    disassociateFromSubscriptionGroups(alertId);
    deleteAssociatedAnomalies(alertId);

    alertManager.delete(dto);
  }

  public void deleteAssociatedAnomalies(final Long alertId) {
    final List<MergedAnomalyResultDTO> anomalies = mergedAnomalyResultManager.findByPredicate(
        Predicate.EQ("detectionConfigId", alertId));
    anomalies.forEach(mergedAnomalyResultManager::delete);
  }

  @SuppressWarnings("unchecked")
  private void disassociateFromSubscriptionGroups(final Long alertId) {
    final List<SubscriptionGroupDTO> allSubscriptionGroups = subscriptionGroupManager.findAll();

    List<SubscriptionGroupDTO> updated = new ArrayList<>();
    for (SubscriptionGroupDTO sg : allSubscriptionGroups) {
      final List<Long> alertIds = (List<Long>) sg.getProperties().get("detectionConfigIds");
      if (alertIds.contains(alertId)) {
        alertIds.removeAll(singleton(alertId));
        updated.add(sg);
      }
    }
    subscriptionGroupManager.update(updated);
  }
}
