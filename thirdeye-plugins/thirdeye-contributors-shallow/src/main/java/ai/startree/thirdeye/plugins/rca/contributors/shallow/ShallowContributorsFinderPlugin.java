package ai.startree.thirdeye.plugins.rca.contributors.shallow;

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import com.google.auto.service.AutoService;
import java.util.List;

@AutoService(Plugin.class)
public class ShallowContributorsFinderPlugin implements Plugin {

  @Override
  public Iterable<ContributorsFinderFactory> getContributorsFinderFactories() {
    return List.of(
        new ShallowContributorsFinderPluginFactory()
    );
  }
}
