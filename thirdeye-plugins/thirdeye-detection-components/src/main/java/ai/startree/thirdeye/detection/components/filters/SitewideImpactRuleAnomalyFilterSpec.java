/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SitewideImpactRuleAnomalyFilterSpec extends AbstractSpec {

  private double threshold = Double.NaN;
  private String offset;
  private String pattern = "UP_OR_DOWN";
  private String sitewideMetricName;
  private String sitewideCollection;
  private Map<String, Collection<String>> filters = new HashMap<>();

  public double getThreshold() {
    return threshold;
  }

  public SitewideImpactRuleAnomalyFilterSpec setThreshold(final double threshold) {
    this.threshold = threshold;
    return this;
  }

  public String getOffset() {
    return offset;
  }

  public SitewideImpactRuleAnomalyFilterSpec setOffset(final String offset) {
    this.offset = offset;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  public SitewideImpactRuleAnomalyFilterSpec setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }

  public String getSitewideMetricName() {
    return sitewideMetricName;
  }

  public SitewideImpactRuleAnomalyFilterSpec setSitewideMetricName(
      final String sitewideMetricName) {
    this.sitewideMetricName = sitewideMetricName;
    return this;
  }

  public String getSitewideCollection() {
    return sitewideCollection;
  }

  public SitewideImpactRuleAnomalyFilterSpec setSitewideCollection(
      final String sitewideCollection) {
    this.sitewideCollection = sitewideCollection;
    return this;
  }

  public Map<String, Collection<String>> getFilters() {
    return filters;
  }

  public SitewideImpactRuleAnomalyFilterSpec setFilters(
      final Map<String, Collection<String>> filters) {
    this.filters = filters;
    return this;
  }
}
