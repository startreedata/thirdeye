package ai.startree.thirdeye.spi.rca;

public interface ContributorsFinderFactory {

  String name();

  ContributorsFinder build(final ContributorsFinderContext context);
}
