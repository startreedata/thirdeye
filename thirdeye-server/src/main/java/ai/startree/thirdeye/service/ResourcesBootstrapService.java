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
package ai.startree.thirdeye.service;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.core.BootstrapResourcesRegistry;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ResourcesBootstrapService {

  private static final Logger LOG = LoggerFactory.getLogger(ResourcesBootstrapService.class);

  private final BootstrapResourcesRegistry bootstrapResourcesRegistry;
  private final AlertTemplateService alertTemplateService;

  @Inject
  public ResourcesBootstrapService(final BootstrapResourcesRegistry bootstrapResourcesRegistry,
      final AlertTemplateService alertTemplateService) {
    this.bootstrapResourcesRegistry = bootstrapResourcesRegistry;
    this.alertTemplateService = alertTemplateService;
  }

  public void bootstrap() {
    LOG.info("Loading recommended templates: START.");
    final List<AlertTemplateApi> alertTemplates = bootstrapResourcesRegistry.getAlertTemplates();
    LOG.info("Loading recommended templates: templates to load: {}",
        alertTemplates.stream().map(AlertTemplateApi::getName).collect(Collectors.toList()));
    final List<AlertTemplateApi> loadedTemplates = alertTemplateService.loadTemplates(
        AuthorizationManager.getInternalValidPrincipal(), alertTemplates, true);
    LOG.info("Loading recommended templates: SUCCESS. Templates loaded: {}",
        loadedTemplates.stream().map(AlertTemplateApi::getName).collect(Collectors.toList()));
  }
}
