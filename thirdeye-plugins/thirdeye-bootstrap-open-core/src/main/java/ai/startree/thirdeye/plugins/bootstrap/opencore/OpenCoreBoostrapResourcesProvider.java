package ai.startree.thirdeye.plugins.bootstrap.opencore;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.bootstrap.BootstrapResourcesProvider;
import java.util.List;

public class OpenCoreBoostrapResourcesProvider implements BootstrapResourcesProvider {

  private static final String RESOURCES_TEMPLATES_PATH = "alert-templates";

  @Override
  public List<AlertTemplateApi> getAlertTemplates() {
    return readJsonObjectsFromResourcesFolder(RESOURCES_TEMPLATES_PATH,
        this.getClass(),
        AlertTemplateApi.class);
  }

  private List<AlertTemplateApi> readJsonObjectsFromResourcesFolder(
      final String resourcesTemplatesPath,
      final Class<? extends OpenCoreBoostrapResourcesProvider> aClass,
      final Class<AlertTemplateApi> alertTemplateApiClass) {
    // fixme cyril use the core implem
    return null;
  }
}
