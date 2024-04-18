/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.rca.HeatmapCalculator;
import ai.startree.thirdeye.spi.api.HeatMapResponseApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class RcaMetricService {

  
  private final AnomalyManager anomalyDao;
  private final HeatmapCalculator heatmapCalculator;
  private final AuthorizationManager authorizationManager;

  @Inject
  public RcaMetricService(final AnomalyManager anomalyDao, final HeatmapCalculator heatmapCalculator,
      final AuthorizationManager authorizationManager) {
    this.anomalyDao = anomalyDao;
    this.heatmapCalculator = heatmapCalculator;
    this.authorizationManager = authorizationManager;
  }

  public HeatMapResponseApi computeHeatmap(final ThirdEyePrincipal principal, final long anomalyId,
      final String baselineOffset,
      final List<String> filters,
      final List<String> dimensions,
      final List<String> excludedDimensions,
      final Integer limit) throws Exception {
    final AnomalyDTO anomalyDto = ensureExists(anomalyDao.findById(anomalyId),
        String.format("Anomaly ID: %d", anomalyId));
    authorizationManager.ensureCanRead(principal, anomalyDto);
    return heatmapCalculator.compute(anomalyDto,
        baselineOffset,
        filters,
        limit,
        dimensions,
        excludedDimensions);
  }
}
