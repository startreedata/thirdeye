package ai.startree.thirdeye.plugins.bootstrap.opencore;

import static ai.startree.thirdeye.spi.util.FileUtils.readJsonObjectsFromResourcesFolder;

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
}
