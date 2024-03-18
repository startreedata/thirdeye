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
package ai.startree.thirdeye.service;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.datalayer.core.EnumerationItemMaintainer;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import ai.startree.thirdeye.spi.util.AnomalyUtils;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MaintenanceService {

  private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);

  private final EnumerationItemManager enumerationItemManager;
  private final AnomalyManager anomalyManager;
  private final AuthorizationManager authorizationManager;
  private final EnumerationItemMaintainer enumerationItemMaintainer;

  @Inject
  public MaintenanceService(final EnumerationItemManager enumerationItemManager,
      final AnomalyManager anomalyManager, final AuthorizationManager authorizationManager,
      final EnumerationItemMaintainer enumerationItemMaintainer) {
    this.enumerationItemManager = enumerationItemManager;
    this.anomalyManager = anomalyManager;
    this.authorizationManager = authorizationManager;
    this.enumerationItemMaintainer = enumerationItemMaintainer;
  }

  public void migrateEnumerationItems(final ThirdEyeServerPrincipal principal, final long fromId,
      final long toId) {
    final EnumerationItemDTO from = enumerationItemManager.findById(fromId);
    authorizationManager.ensureCanDelete(principal, from);

    final EnumerationItemDTO to = enumerationItemManager.findById(toId);
    authorizationManager.ensureCanDelete(principal, to);

    enumerationItemMaintainer.migrateAndRemove(from, to);
    logDeleteOperation(from, principal, false);
  }

  private static void logDeleteOperation(final EnumerationItemDTO ei,
      final ThirdEyeServerPrincipal principal, final boolean dryRun) {
    String eiString;
    try {
      eiString = ThirdEyeSerialization.getObjectMapper()
          .writeValueAsString(ApiBeanMapper.toApi(ei));
    } catch (final Exception e) {
      eiString = ei.toString();
    }
    log.warn("Deleting{} by {}. enumeration item(id: {}}) json: {}", dryRun ? "(dryRun)" : "",
        principal.getName(), ei.getId(), eiString);
  }

  public void updateAnomalyIgnoredIndex(final ThirdEyeServerPrincipal principal) {
    // skip already updated ignored index
    final DaoFilter filter = new DaoFilter().setPredicate(Predicate.NEQ("ignored", true));
    anomalyManager.filter(filter)
        .stream()
        .peek(anomaly -> authorizationManager.ensureCanEdit(principal, anomaly, anomaly))
        .filter(AnomalyUtils::isIgnore)
        .forEach(anomalyManager::update);
  }
}
