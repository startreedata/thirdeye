/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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
