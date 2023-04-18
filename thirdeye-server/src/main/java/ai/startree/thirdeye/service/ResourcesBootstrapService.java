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
