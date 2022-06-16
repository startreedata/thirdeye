package ai.startree.thirdeye.plugins.rca.contributors.cube;

import ai.startree.thirdeye.spi.rca.ContributorsFinder;
import ai.startree.thirdeye.spi.rca.ContributorsFinderContext;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CubeContributorsFinderPluginFactory implements ContributorsFinderFactory {

  @Override
  public String name() {
    return "cube";
  }

  @Override
  public @NonNull ContributorsFinder build(final ContributorsFinderContext context) {
    final CubeConfiguration configuration = new ObjectMapper().convertValue(context.getParams(), CubeConfiguration.class);

    return new CubeContributorsFinder(
        context.getAggregationLoader(),
        configuration);
  }
}
