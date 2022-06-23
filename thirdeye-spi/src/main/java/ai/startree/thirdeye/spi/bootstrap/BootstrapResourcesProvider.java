package ai.startree.thirdeye.spi.bootstrap;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import java.util.List;

/**
 * BootstrapResourcesProvider provide resources to bootstrap ThirdEye installation and configuration.
 */
public interface BootstrapResourcesProvider {

  List<AlertTemplateApi> getAlertTemplates();
}
