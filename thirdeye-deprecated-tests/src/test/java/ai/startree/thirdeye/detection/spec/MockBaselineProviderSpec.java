/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.spec;

import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import java.util.Map;

public class MockBaselineProviderSpec extends AbstractSpec {

  private Map<MetricSlice, TimeSeries> baselineTimeseries;
  private Map<MetricSlice, Double> baselineAggregates;

  public Map<MetricSlice, TimeSeries> getBaselineTimeseries() {
    return baselineTimeseries;
  }

  public void setBaselineTimeseries(Map<MetricSlice, TimeSeries> baselineTimeseries) {
    this.baselineTimeseries = baselineTimeseries;
  }

  public Map<MetricSlice, Double> getBaselineAggregates() {
    return baselineAggregates;
  }

  public void setBaselineAggregates(Map<MetricSlice, Double> baselineAggregates) {
    this.baselineAggregates = baselineAggregates;
  }
}
