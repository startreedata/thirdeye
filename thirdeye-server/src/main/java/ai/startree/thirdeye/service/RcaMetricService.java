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

package ai.startree.thirdeye.service;

import ai.startree.thirdeye.rca.HeatmapCalculator;
import ai.startree.thirdeye.spi.api.HeatMapResponseApi;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class RcaMetricService {

  private final HeatmapCalculator heatmapCalculator;

  @Inject
  public RcaMetricService(final HeatmapCalculator heatmapCalculator) {
    this.heatmapCalculator = heatmapCalculator;
  }

  public HeatMapResponseApi computeHeatmap(final long anomalyId,
      final String baselineOffset,
      final List<String> filters,
      final List<String> dimensions,
      final List<String> excludedDimensions,
      final Integer limit) throws Exception {
    return heatmapCalculator.compute(anomalyId,
        baselineOffset,
        filters,
        limit,
        dimensions,
        excludedDimensions);
  }
}
