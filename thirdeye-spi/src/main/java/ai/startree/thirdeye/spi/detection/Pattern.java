/*
 * Copyright 2022 StarTree Inc
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

import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.Series.DoubleConditional;

/**
 * Up or down detection pattern
 */
public enum Pattern {
  UP {
    @Override
    public boolean isAnomaly(final double currentValue, final double lowerBound,
        final double upperBound) {
      return currentValue > upperBound;
    }
  },

  DOWN {
    @Override
    public boolean isAnomaly(final double currentValue, final double lowerBound,
        final double upperBound) {
      return currentValue < lowerBound;
    }
  },

  UP_OR_DOWN {
    @Override
    public boolean isAnomaly(final double currentValue, final double lowerBound,
        final double upperBound) {
      return UP.isAnomaly(currentValue, lowerBound, upperBound) || DOWN.isAnomaly(currentValue,
          lowerBound,
          upperBound);
    }
  };

  public abstract boolean isAnomaly(final double currentValue, final double lowerBound,
      final double upperBound);

  public BooleanSeries isAnomaly(final DoubleSeries currentValues, final DoubleSeries lowerBounds,
      final DoubleSeries upperBounds) {
    final DoubleConditional isAnomalyFn = doubles -> this.isAnomaly(doubles[0],
        doubles[1],
        doubles[2]);

    return DoubleSeries.map(isAnomalyFn, currentValues, lowerBounds, upperBounds);
  }
}
