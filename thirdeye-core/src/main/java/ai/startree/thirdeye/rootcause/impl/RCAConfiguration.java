/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Config class for RCA's yml config
 * Maintain a list of configs for each external event data provider
 * Maintain a list of configs for each external pipeline using this config
 */
public class RCAConfiguration {

  private Map<String, List<PipelineConfiguration>> frameworks = new HashMap<>();
  private List<String> formatters = Collections.emptyList();
  private int parallelism = 1;

  public Map<String, List<PipelineConfiguration>> getFrameworks() {
    return frameworks;
  }

  public RCAConfiguration setFrameworks(
      final Map<String, List<PipelineConfiguration>> frameworks) {
    this.frameworks = frameworks;
    return this;
  }

  public int getParallelism() {
    return parallelism;
  }

  public RCAConfiguration setParallelism(final int parallelism) {
    this.parallelism = parallelism;
    return this;
  }

  public List<String> getFormatters() {
    return formatters;
  }

  public RCAConfiguration setFormatters(final List<String> formatters) {
    this.formatters = formatters;
    return this;
  }
}
