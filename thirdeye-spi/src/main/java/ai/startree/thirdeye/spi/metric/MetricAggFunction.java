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
package ai.startree.thirdeye.spi.metric;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_LOCALE;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

public enum MetricAggFunction {
  SUM, AVG, COUNT, COUNT_DISTINCT, MAX, MIN, PCT50, PCT90, PCT95, PCT99;

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
    return valueOf(aggFunction.toUpperCase(DEFAULT_LOCALE));
  }

  /**
   * Parse percentile percent of String of format pctXXXX.
   * The 2 first digits are the digits before the comma.
   * The other ones are the digits after the comma.
   * Eg: PCT05, pct95, pct999
   *
   * todo cyril clean this interface - the goal is to allow any number for percentile
   */
  public static Double parsePercentile(final String percentileString) {
    final String lowerCase = requireNonNull(percentileString).toLowerCase(DEFAULT_LOCALE);
    if (!lowerCase.startsWith("pct")) {
      return null;
    }
    final String percentDigits = lowerCase.substring(3);
    if (!(percentDigits.length() >= 2)) {
      return null;
    }
    final String beforeCommaDigits = percentDigits.substring(0, 2);
    double percentile = Double.parseDouble(beforeCommaDigits);

    final String afterCommaDigits = percentDigits.substring(2);
    if (afterCommaDigits.length() > 0) {
      double afterComma = Double.parseDouble(afterCommaDigits);
      afterComma /= Math.pow(10, afterCommaDigits.length());
      percentile += afterComma;
    }

    return percentile;
  }
}
