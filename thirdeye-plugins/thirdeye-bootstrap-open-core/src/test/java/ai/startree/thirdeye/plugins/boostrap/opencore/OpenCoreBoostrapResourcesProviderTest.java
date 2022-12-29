package ai.startree.thirdeye.plugins.boostrap.opencore;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.plugins.bootstrap.opencore.OpenCoreBoostrapResourcesProvider;
import org.testng.annotations.Test;

public class OpenCoreBoostrapResourcesProviderTest {

  // ensures the generation of percentile templates is not broken
  // has to be updated every time a template is added
  @Test
  public void testNumberOfTemplates() {
    final OpenCoreBoostrapResourcesProvider provider = new OpenCoreBoostrapResourcesProvider();
    final var templates = provider.getAlertTemplates();
    assertThat(templates.size()).isEqualTo(8);
    assertThat(
        templates.stream().filter(t -> t.getName().contains("-percentile")).count()).isEqualTo(4);
  }
}
