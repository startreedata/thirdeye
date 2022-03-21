/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.rootcause.timeseries;

import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.Series;

/**
 * Aggregation types supported by BaselineAggregate.
 */
public enum BaselineAggregateType {
  SUM(DoubleSeries.SUM),
  PRODUCT(DoubleSeries.PRODUCT),
  MEAN(DoubleSeries.MEAN),
  AVG(DoubleSeries.MEAN),
  COUNT(DoubleSeries.SUM),
  MEDIAN(DoubleSeries.MEDIAN),
  MIN(DoubleSeries.MIN),
  MAX(DoubleSeries.MAX),
  STD(DoubleSeries.STD);

  final Series.DoubleFunction function;

  BaselineAggregateType(Series.DoubleFunction function) {
    this.function = function;
  }

  public Series.DoubleFunction getFunction() {
    return function;
  }
}

