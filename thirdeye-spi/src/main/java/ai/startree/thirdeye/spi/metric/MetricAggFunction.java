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
package ai.startree.thirdeye.spi.metric;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum MetricAggFunction {
  SUM, AVG, COUNT, COUNT_DISTINCT, MAX, PCT50, PCT90, PCT95, PCT99;

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
    return valueOf(aggFunction.toUpperCase(Locale.ROOT));
  }
}
