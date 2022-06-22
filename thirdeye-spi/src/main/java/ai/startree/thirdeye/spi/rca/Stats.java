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
package ai.startree.thirdeye.spi.rca;

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.dataframe.DoubleSeries;

public class Stats {

  public static double computeValueChangePercentage(final double baseline, final double current) {
    if (baseline != 0d) {
      double percentageChange = ((current - baseline) / baseline) * 100d;
      return roundUp(percentageChange);
    } else {
      return Double.NaN;
    }
  }

  public static DoubleSeries computeValueChangePercentage(final DoubleSeries baseline,
      final DoubleSeries current) {
    checkArgument(baseline.size() == current.size());
    final double[] values = new double[baseline.size()];
    for (int i = 0; i < baseline.size(); i++) {
      values[i] = computeValueChangePercentage(baseline.getDouble(i), current.getDouble(i));
    }
    return DoubleSeries.buildFrom(values);
  }

  public static double computeContributionChangePercentage(final double baseline,
      final double current,
      double baselineTotal,
      double currentTotal) {
    if (currentTotal != 0d && baselineTotal != 0d) {
      double contributionChange = ((current / currentTotal) - (baseline / baselineTotal)) * 100d;
      return roundUp(contributionChange);
    } else {
      return Double.NaN;
    }
  }

  public static DoubleSeries computeContributionChangePercentage(final DoubleSeries baseline,
      final DoubleSeries current, final double baselineTotal, final double currentTotal) {
    checkArgument(baseline.size() == current.size());
    final double[] values = new double[baseline.size()];
    for (int i = 0; i < baseline.size(); i++) {
      values[i] = computeContributionChangePercentage(baseline.getDouble(i),
          current.getDouble(i),
          baselineTotal,
          currentTotal);
    }
    return DoubleSeries.buildFrom(values);
  }

  public static double computeContributionToOverallChangePercentage(final double baseline,
      final double current,
      final double baselineTotal,
      final double currentTotal) {
    if (baselineTotal != 0d) {
      double contributionToOverallChange =
          ((current - baseline) / Math.abs(currentTotal - baselineTotal)) * 100d;
      return roundUp(contributionToOverallChange);
    } else {
      return Double.NaN;
    }
  }

  public static DoubleSeries computeContributionToOverallChangePercentage(
      final DoubleSeries baseline,
      final DoubleSeries current, final double baselineTotal, final double currentTotal) {
    checkArgument(baseline.size() == current.size());
    final double[] values = new double[baseline.size()];
    for (int i = 0; i < baseline.size(); i++) {
      values[i] = computeContributionToOverallChangePercentage(baseline.getDouble(i),
          current.getDouble(i),
          baselineTotal,
          currentTotal);
    }
    return DoubleSeries.buildFrom(values);
  }

  public static double roundUp(final double number) {
    return Math.round(number * 10000d) / 10000d;
  }
}
