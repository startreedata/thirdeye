/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.metric;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum MetricAggFunction {
  SUM, AVG, COUNT, COUNT_DISTINCT, MAX, PCT50, PCT90, PCT95, PCT99;

  public static final String PERCENTILE_PREFIX = "PCT";

  public static final List<MetricAggFunction> AVAILABLE_METRIC_AGG_FUNCTIONS = List.of(
      MetricAggFunction.values());

  public static final List<String> AVAILABLE_METRIC_AGG_FUNCTIONS_NAMES = AVAILABLE_METRIC_AGG_FUNCTIONS
      .stream().map(MetricAggFunction::name).collect(Collectors.toList());

  public boolean isPercentile() {
    return this.toString().startsWith(PERCENTILE_PREFIX);
  }

  public static MetricAggFunction fromString(String aggFunction) {
    if (aggFunction == null) {
      return null;
    }
    return valueOf(aggFunction.toUpperCase(Locale.ROOT));
  }
}
