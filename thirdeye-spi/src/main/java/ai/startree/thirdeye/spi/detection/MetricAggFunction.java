/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

public enum MetricAggFunction {
  SUM, AVG, COUNT, COUNT_DISTINCT, MAX, PCT50, PCT90, PCT95, PCT99;

  public static final String PERCENTILE_PREFIX = "PCT";

  public boolean isPercentile() {
    return this.toString().startsWith(PERCENTILE_PREFIX);
  }
}
