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

import static ai.startree.thirdeye.RequestCache.buildCache;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.RequestCache;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.core.AppAnalyticsService;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnomalyService extends CrudService<AnomalyApi, AnomalyDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("alert.id", "detectionConfigId")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .put("isChild", "child")
      .put("metadata.metric.name", "metric")
      .put("metadata.dataset.name", "collection")
      .put("enumerationItem.id", "enumerationItemId")
      .put("feedback.id", "anomalyFeedbackId")
      .put("anomalyLabels.ignore", "ignored")
      .build();

  private final AnomalyManager anomalyManager;
  private final AlertManager alertManager;
  private final AppAnalyticsService analyticsService;

  @Inject
  public AnomalyService(
      final AnomalyManager anomalyManager,
      final AlertManager alertManager,
      final AppAnalyticsService analyticsService,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, anomalyManager, API_TO_INDEX_FILTER_MAP);
    this.anomalyManager = anomalyManager;
    this.alertManager = alertManager;
    this.analyticsService = analyticsService;
  }

  @Override
  protected RequestCache createRequestCache() {
    return super.createRequestCache()
        .setAlerts(buildCache(alertManager::findById));
  }

  @Override
  protected AnomalyDTO createDto(final ThirdEyePrincipal principal,
      final AnomalyApi api) {
    return toDto(api);
  }

  @Override
  protected AnomalyDTO toDto(final AnomalyApi api) {
    return ApiBeanMapper.toDto(api);
  }

  @Override
  protected AnomalyApi toApi(final AnomalyDTO dto, RequestCache cache) {
    final AnomalyApi anomalyApi = ApiBeanMapper.toApi(dto);
    optional(anomalyApi.getAlert())
        .filter(alertApi -> alertApi.getId() != null)
        .ifPresent(alertApi -> alertApi.setName(cache.getAlerts()
            .getUnchecked(alertApi.getId())
            .getName()));
    return anomalyApi;
  }

  public void setFeedback(final Long id, final AnomalyFeedbackApi api) {
    final AnomalyDTO dto = getDto(id);

    final AnomalyFeedbackDTO feedbackDTO = ApiBeanMapper.toAnomalyFeedbackDTO(api);
    dto.setFeedback(feedbackDTO);
    anomalyManager.updateAnomalyFeedback(dto);

    if (dto.isChild()) {
      optional(anomalyManager.findParent(dto))
          .ifPresent(p -> {
            p.setFeedback(feedbackDTO);
            anomalyManager.updateAnomalyFeedback(p);
          });
    }
  }

  public AnomalyStatsApi stats(final Long startTime, final Long endTime) {
    final List<Predicate> predicates = new ArrayList<>();
    optional(startTime)
        .ifPresent(start -> predicates.add(Predicate.GE("startTime", startTime)));
    optional(endTime)
        .ifPresent(end -> predicates.add(Predicate.LE("endTime", endTime)));
    final DaoFilter filter = predicates.isEmpty()
        ? null : new DaoFilter().setPredicate(Predicate.AND(predicates.toArray(Predicate[]::new)));
    final AnomalyStatsApi stats = analyticsService.computeAnomalyStats(filter);
    return stats;
  }
}
