package ai.startree.thirdeye.plugins.rca.contributors.simple;


import ai.startree.thirdeye.spi.rca.ContributorsFinder;
import ai.startree.thirdeye.spi.rca.ContributorsFinderContext;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SimpleContributorsFinderPluginFactory implements ContributorsFinderFactory {

  @Override
  public String name() {
    return "simple";
  }

  @Override
  public @NonNull ContributorsFinder build(final ContributorsFinderContext context) {
    final SimpleConfiguration configuration = new ObjectMapper().convertValue(context.getParams(), SimpleConfiguration.class);

    return new SimpleContributorsFinder(
        context.getAggregationLoader(),
        configuration);
  }
}
