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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ResourcesBootstrapService {

  private static final Logger LOG = LoggerFactory.getLogger(ResourcesBootstrapService.class);
  private final AlertTemplateService alertTemplateService;
  private final AuthorizationManager authorizationManager;
  private final EnumerationItemManager enumerationItemDao;
  private final AlertManager alertDao;
  private final Timer enumerationItemMigrationTimer;
  private final Timer recommendedTemplatesLoadingTimer;

  @Inject
  public ResourcesBootstrapService(final AlertTemplateService alertTemplateService,
      final AuthorizationManager authorizationManager,
      final EnumerationItemManager enumerationItemManager,
      final AlertManager alertManager) {
    this.alertTemplateService = alertTemplateService;
    this.authorizationManager = authorizationManager;
    this.enumerationItemDao = enumerationItemManager;
    this.alertDao = alertManager;

    this.enumerationItemMigrationTimer = Timer.builder("thirdeye_enumeration_item_migration")
        .description("Time to migrate enumeration item at startup of the scheduler.")
        .register(Metrics.globalRegistry);
    this.recommendedTemplatesLoadingTimer = Timer.builder("thirdeye_recommended_templates_loading")
        .description("Time to load recommended templates in all namespaces.")
        .register(Metrics.globalRegistry);
  }

  public void bootstrap(final ThirdEyeServerPrincipal principal) {
    // migrate enumeration items: write auth in database - todo authz - THROWABLE CODE - can be removed once migration is done
    //   note: this migration may take some time - there can be thousands of enumeration items - introduced a metric to ensure it's not too slow
    boolean enumerationItemMigrationSuccess = true;
    final Timer.Sample sample = Timer.start(Metrics.globalRegistry);
    try {
      for (final EnumerationItemDTO item : enumerationItemDao.findAll()) {
        // fixme cyril optimize migration by not migrating entities that already have a namespace 
        //  BUT as long as changing namespace of an alert is allowed, forcing migration at every start here - 
        //if (item.getAuth() != null) { // maybe it's item.namespace() only - think about it
        //  continue;  
        //}
        final Long alertId = optional(item.getAlert()).map(AbstractDTO::getId).orElse(null);
        if (alertId != null) {
          final AlertDTO alertDto = alertDao.findById(alertId);
          if (alertDto != null) {
            item.setAuth(new AuthorizationConfigurationDTO().setNamespace(alertDto.namespace()));
            final Long success = enumerationItemDao.save(item);
            if (success < 1) {
              LOG.error("Failed to save enumeration item {}.", item.getId());
              enumerationItemMigrationSuccess = false;
            }  
          } else {
            LOG.error("Enumeration item {} has an unknown alert id {}.", item.getId(), alertId);
            enumerationItemMigrationSuccess = false;  
          }
        } else {
          LOG.error("Enumeration item {} with unset alert id. This should not happen. Please reach out to StarTree support", item.getId());
          enumerationItemMigrationSuccess = false;
        }
      } 
    } catch (Exception e) {
      enumerationItemMigrationSuccess = false; 
      LOG.error("An error happened while trying to migrate enumeration items to the new namespacing system.", e);
    }
    sample.stop(enumerationItemMigrationTimer);
    if (!enumerationItemMigrationSuccess) {
      LOG.error(
          "Enumeration item migration to new namespacing system failed. Please store the log of this process and reach out to StarTree support.");
    }

    // loading templates may take some time - putting a timer to see if this should be optimized - see comment below
    final Timer.Sample templateLoadSample = Timer.start(Metrics.globalRegistry);
    authorizationManager.ensureHasRootAccess(principal);
    // install templates in all known namespaces 
    // hack get distinct namespaces by looking at alerts - fixme cyril authz - get list of namespaces from the ThirdEye authorizer
    final HashSet<String> distinctNamespaces = alertDao.findAll()
        .stream()
        .map(e -> e.namespace())
        .collect(Collectors.toCollection(HashSet::new));
    // continue to install in the unset namespace for the moment
    distinctNamespaces.add(null);
    // todo cyril - this may be slow because the loadRecommendedTemplates load templates from disk, performs some template generation, then write them - only the write part should be in the loop
    //  check timer - it's not trivial to implement (input template api are mutated in loadRecommendedTemplates)
    for (final String namespace: distinctNamespaces) {
      alertTemplateService.loadRecommendedTemplates(principal, true, namespace); 
    }
    templateLoadSample.stop(recommendedTemplatesLoadingTimer);
  }
}
