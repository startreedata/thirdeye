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
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.util.AnomalyUtils;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MaintenanceService {

  private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);

  private final AnomalyManager anomalyManager;
  private final AuthorizationManager authorizationManager;

  @Inject
  public MaintenanceService(final AnomalyManager anomalyManager,
      final AuthorizationManager authorizationManager) {
    this.anomalyManager = anomalyManager;
    this.authorizationManager = authorizationManager;
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
