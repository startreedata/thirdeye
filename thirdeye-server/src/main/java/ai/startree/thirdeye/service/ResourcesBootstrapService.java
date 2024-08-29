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
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
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
  private final AlertTemplateManager templateDao;
  private final Timer recommendedTemplatesLoadingTimer;

  @Inject
  public ResourcesBootstrapService(final AlertTemplateService alertTemplateService,
      final AuthorizationManager authorizationManager,
      final AlertTemplateManager templateDao) {
    this.alertTemplateService = alertTemplateService;
    this.authorizationManager = authorizationManager;
    this.templateDao = templateDao;

    this.recommendedTemplatesLoadingTimer = Timer.builder("thirdeye_recommended_templates_loading")
        .description("Time to load recommended templates in all namespaces.")
        .register(Metrics.globalRegistry);
  }

  public void bootstrap(final ThirdEyeServerPrincipal principal) {
    // loading templates may take some time - putting a timer to see if this should be optimized - see comment below
    final Timer.Sample templateLoadSample = Timer.start(Metrics.globalRegistry);
    authorizationManager.ensureHasRootAccess(principal);
    // update templates in all known namespaces 
    // for new namespaces we will need to create templates manually or as part as the namespace creation - design is yet to define todo authz
    final HashSet<String> distinctNamespaces = templateDao.findAll()
        .stream()
        .map(AbstractDTO::namespace)
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
