/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 * Config class for RCA's yml config
 * Maintain a list of configs for each external event data provider
 * Maintain a list of configs for each external pipeline using this config
 */
public class RcaConfiguration {

  private List<String> formatters = Collections.emptyList();
  private int parallelism = 1;
  @JsonProperty("topContributors")
  private ContributorsFinderConfiguration contributorsFinderConfiguration = new ContributorsFinderConfiguration();

  public int getParallelism() {
    return parallelism;
  }

  public RcaConfiguration setParallelism(final int parallelism) {
    this.parallelism = parallelism;
    return this;
  }

  public List<String> getFormatters() {
    return formatters;
  }

  public RcaConfiguration setFormatters(final List<String> formatters) {
    this.formatters = formatters;
    return this;
  }

  public ContributorsFinderConfiguration getContributorsFinderConfiguration() {
    return contributorsFinderConfiguration;
  }

  public RcaConfiguration setContributorsFinderConfiguration(
      final ContributorsFinderConfiguration contributorsFinderConfiguration) {
    this.contributorsFinderConfiguration = contributorsFinderConfiguration;
    return this;
  }
}
