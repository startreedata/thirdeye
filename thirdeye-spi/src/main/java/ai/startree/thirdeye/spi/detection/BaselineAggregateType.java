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

