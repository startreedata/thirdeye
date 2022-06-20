package ai.startree.thirdeye.plugins.rca.contributors.shallow;


import ai.startree.thirdeye.spi.rca.ContributorsFinder;
import ai.startree.thirdeye.spi.rca.ContributorsFinderContext;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShallowContributorsFinderPluginFactory implements ContributorsFinderFactory {

  @Override
  public String name() {
    return "shallow";
  }

  @Override
  public @NonNull ContributorsFinder build(final ContributorsFinderContext context) {
    final ShallowConfiguration configuration = new ObjectMapper().convertValue(context.getParams(), ShallowConfiguration.class);

    return new ShallowContributorsFinder(
        context.getAggregationLoader(),
        configuration);
  }
}
