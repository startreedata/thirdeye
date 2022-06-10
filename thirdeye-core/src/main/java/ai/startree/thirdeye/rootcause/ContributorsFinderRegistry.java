/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.rca.ContributorsFinder;
import ai.startree.thirdeye.spi.rca.ContributorsFinderContext;
import ai.startree.thirdeye.spi.rca.ContributorsFinderFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ContributorsFinderRegistry {

  private final Map<String, ContributorsFinderFactory> factoryMap = new HashMap<>();
  private final AggregationLoader aggregationLoader;

  @Inject
  public ContributorsFinderRegistry(final AggregationLoader aggregationLoader) {
    this.aggregationLoader = aggregationLoader;
  }

  public void addContributorsFinderFactory(final ContributorsFinderFactory factory) {
    factoryMap.put(factory.name(), factory);
  }

  public ContributorsFinder get(final String name, final Map<String, Object> params) {
    requireNonNull(name, "name is null");
    final ContributorsFinderFactory contributorsFinderFactory = requireNonNull(factoryMap.get(name),
        "Unable to load contributorsFinderFactory: " + name);
    final ContributorsFinderContext context = new ContributorsFinderContext()
        .setAggregationLoader(aggregationLoader)
        .setParams(params);

    return contributorsFinderFactory.build(context);
  }
}
