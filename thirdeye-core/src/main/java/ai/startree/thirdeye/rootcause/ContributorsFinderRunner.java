/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.rootcause.configuration.ContributorsFinderConfiguration;
import ai.startree.thirdeye.rootcause.configuration.RcaConfiguration;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.rca.ContributorsFinder;
import ai.startree.thirdeye.spi.rca.ContributorsFinderContext;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import ai.startree.thirdeye.spi.rca.ContributorsFinderResult;
import ai.startree.thirdeye.spi.rca.ContributorsSearchConfiguration;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class ContributorsFinderRunner {

  private final Map<String, ContributorsFinderFactory> factoryMap = new HashMap<>();
  private final AggregationLoader aggregationLoader;
  private final String defaultAlgorithm;
  private final Map<String, Object> defaultParams;

  private final Map<Integer, ContributorsFinder> cache = new HashMap<>();

  @Inject
  public ContributorsFinderRunner(final AggregationLoader aggregationLoader,
      final RcaConfiguration rcaConfiguration) {
    this.aggregationLoader = aggregationLoader;
    final @NonNull ContributorsFinderConfiguration configuration = rcaConfiguration.getContributorsFinderConfiguration();
    this.defaultAlgorithm = configuration.getAlgorithm();
    this.defaultParams = configuration.getParams();
  }

  public void addContributorsFinderFactory(final ContributorsFinderFactory factory) {
    factoryMap.put(factory.name(), factory);
  }

  private int cacheHash(final String name, final Map<String, Object> params) {
    // cache hash key for a (contributors plugin, params) tuple
    return Objects.hash(name, params);
  }

  private @NonNull ContributorsFinder get(final String name, final Map<String, Object> params) {

    return cache.computeIfAbsent(
        cacheHash(name, params),
        k -> loadContributorsFinder(name, params)
    );
  }

  private @NonNull ContributorsFinder loadContributorsFinder(final String name,
      final Map<String, Object> params) {
    checkArgument(name != null, "name is null");
    final ContributorsFinderFactory contributorsFinderFactory = factoryMap.get(name);
    checkArgument(contributorsFinderFactory != null,
        "Unable to load contributorsFinderFactory for: " + name);
    final ContributorsFinderContext context = new ContributorsFinderContext()
        .setAggregationLoader(aggregationLoader)
        .setParams(params);

    return contributorsFinderFactory.build(context);
  }

  public ContributorsFinderResult run(final ContributorsSearchConfiguration searchConfiguration)
      throws Exception {
    final ContributorsFinder contributorsFinder = get(defaultAlgorithm, defaultParams);

    return contributorsFinder.search(searchConfiguration);
  }
}
