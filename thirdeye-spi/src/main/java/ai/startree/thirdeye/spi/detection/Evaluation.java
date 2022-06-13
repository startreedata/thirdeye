/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.dataframe.DoubleSeries;

/**
 * The util class for model evaluation
 */
public class Evaluation {

  // Suppresses default constructor, ensuring non-instantiability.
  private Evaluation() {
  }

  /**
   * Calculate the mean absolute percentage error (MAPE).
   * See https://en.wikipedia.org/wiki/Mean_absolute_percentage_error
   *
   * @param current current time series
   * @param predicted baseline time series
   * @return the mape value
   */
  public static double calculateMape(DoubleSeries current, DoubleSeries predicted) {
    if (current.contains(0.0)) {
      return Double.POSITIVE_INFINITY;
    }
    return predicted.divide(current).subtract(1).abs().mean().value();
  }
}
