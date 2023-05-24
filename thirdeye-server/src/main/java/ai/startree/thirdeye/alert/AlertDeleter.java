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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Collections.singleton;

import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class AlertDeleter {

  private final AlertManager alertManager;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final AnomalyManager anomalyManager;
  private final EnumerationItemManager enumerationItemManager;

  @Inject
  public AlertDeleter(final AlertManager alertManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final AnomalyManager anomalyManager,
      final EnumerationItemManager enumerationItemManager) {
    this.alertManager = alertManager;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.anomalyManager = anomalyManager;
    this.enumerationItemManager = enumerationItemManager;
  }

  public void delete(final AlertDTO dto) {
    final Long alertId = dto.getId();

    disassociateFromSubscriptionGroups(alertId);
    deleteAssociatedAnomalies(alertId);
    deleteAssociatedEnumerationItems(alertId);

    alertManager.delete(dto);
  }

  public void deleteAssociatedAnomalies(final Long alertId) {
    final List<AnomalyDTO> anomalies = anomalyManager.findByPredicate(
        Predicate.EQ("detectionConfigId", alertId));
    anomalies.forEach(anomalyManager::delete);
  }

  @SuppressWarnings("unchecked")
  private void disassociateFromSubscriptionGroups(final Long alertId) {
    final List<SubscriptionGroupDTO> allSubscriptionGroups = subscriptionGroupManager.findAll();

    final Set<SubscriptionGroupDTO> updated = new HashSet<>();
    for (final SubscriptionGroupDTO sg : allSubscriptionGroups) {
      final List<Long> alertIds = (List<Long>) sg.getProperties().get("detectionConfigIds");
      if (alertIds.contains(alertId)) {
        alertIds.removeAll(singleton(alertId));
        updated.add(sg);
      }
      optional(sg.getAlertAssociations())
          .map(aas -> aas.removeIf(aa -> alertId.equals(aa.getAlert().getId())))
          .filter(b -> b)
          .ifPresent(b -> updated.add(sg));
    }
    subscriptionGroupManager.update(new ArrayList<>(updated));
  }

  private void deleteAssociatedEnumerationItems(final Long alertId) {
    final List<Long> ids = enumerationItemManager.filter(new DaoFilter()
            .setPredicate(Predicate.EQ("alertId", alertId)))
        .stream()
        .map(AbstractDTO::getId)
        .collect(Collectors.toList());
    enumerationItemManager.deleteByIds(ids);
  }
}
