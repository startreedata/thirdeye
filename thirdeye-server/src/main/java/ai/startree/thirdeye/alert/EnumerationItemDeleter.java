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
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class EnumerationItemDeleter {

  private final SubscriptionGroupManager subscriptionGroupManager;
  private final AnomalyManager anomalyManager;
  private final EnumerationItemManager enumerationItemManager;

  @Inject
  public EnumerationItemDeleter(final EnumerationItemManager enumerationItemManager,
      final SubscriptionGroupManager subscriptionGroupManager,
      final AnomalyManager anomalyManager) {
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.anomalyManager = anomalyManager;
    this.enumerationItemManager = enumerationItemManager;
  }

  public void delete(final EnumerationItemDTO dto) {
    requireNonNull(dto.getId(), "EnumerationItemDTO.id cannot be null for deletion");
    disassociateFromSubscriptionGroups(dto.getId());
    deleteAssociatedAnomalies(dto.getId());

    enumerationItemManager.delete(dto);
  }

  public void deleteAssociatedAnomalies(final Long enumerationItemId) {
    final List<AnomalyDTO> anomalies = anomalyManager.filter(
        new AnomalyFilter().setEnumerationItemId(enumerationItemId));
    anomalies.forEach(anomalyManager::delete);
  }

  private void disassociateFromSubscriptionGroups(final Long enumerationItemId) {
    final List<SubscriptionGroupDTO> allSubscriptionGroups = subscriptionGroupManager.findAll();

    final List<SubscriptionGroupDTO> updated = new ArrayList<>();
    for (final SubscriptionGroupDTO sg : allSubscriptionGroups) {
      optional(sg.getAlertAssociations())
          .map(aas -> aas.removeIf(aa ->
              aa.getEnumerationItem() != null &&
                  enumerationItemId.equals(aa.getEnumerationItem().getId())))
          .filter(b -> b)
          .ifPresent(b -> updated.add(sg));
    }
    subscriptionGroupManager.update(updated);
  }
}
