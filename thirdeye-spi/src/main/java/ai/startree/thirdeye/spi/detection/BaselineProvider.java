/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;

/**
 * The baseline provider to calculate predicted baseline.
 *
 * todo cyril the abstraction is good but the interface is not compatible with v2
 */
public interface BaselineProvider<T extends AbstractSpec> extends BaseComponent<T> {

  static boolean isBaselineProvider(Class<?> clazz) {
    return BaselineProvider.class.isAssignableFrom(clazz);
  }

  /**
   * Compute the baseline time series for the metric slice.
   *
   * @return the time series contains predicted baseline.
   */
  @Deprecated
  default TimeSeries computePredictedTimeSeries(MetricSlice slice) {
     // todo cyril drop this method this interface is refactored
    throw new UnsupportedOperationException();
  }

  /**
   * Compute the baseline time series for the metric slice.
   * default implementation is to call computePredictedTimeSeries and aggregate using the aggregate
   * function
   *
   * @return the predicted value.
   */
  @Deprecated
  default Double computePredictedAggregates(MetricSlice slice,
      Series.DoubleFunction aggregateFunction) {
    try {
      TimeSeries baselineTimeSeries = this.computePredictedTimeSeries(slice);
      return baselineTimeSeries.getPredictedBaseline().aggregate(aggregateFunction).getDouble(0);
    } catch (Exception e) {
      return Double.NaN;
    }
  }
}
