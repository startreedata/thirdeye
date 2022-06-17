package ai.startree.thirdeye.spi.rca;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface ContributorsFinderFactory {

  String name();

  @NonNull ContributorsFinder build(final ContributorsFinderContext context);
}
