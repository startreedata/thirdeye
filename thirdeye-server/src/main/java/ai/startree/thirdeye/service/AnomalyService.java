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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.ResourceUtils.ensure;
import static ai.startree.thirdeye.ResourceUtils.ensureExists;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.api.AnomalyStatsApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import com.google.common.collect.ImmutableMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.Nullable;

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

  @Inject
  public AnomalyService(
      final AnomalyManager anomalyManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, anomalyManager, API_TO_INDEX_FILTER_MAP);
    this.anomalyManager = anomalyManager;
  }

  @Override
  protected AnomalyDTO toDto(final AnomalyApi api) {
    return ApiBeanMapper.toDto(api);
  }

  @Override
  protected AnomalyApi toApi(final AnomalyDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  public void setFeedback(final ThirdEyeServerPrincipal principal, final Long id,
      final AnomalyFeedbackApi api) {
    final AnomalyDTO dto = getDto(id);
    // todo cyril review authz - only require read right to add a feedback to an anomaly - to avoid feedback frictions for the moment
    authorizationManager.ensureNamespace(principal, dto);
    authorizationManager.ensureCanRead(principal, dto);
    final AnomalyFeedbackDTO feedbackDTO = ApiBeanMapper.toAnomalyFeedbackDTO(api);
    feedbackDTO.setUpdatedBy(principal.getName());
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

  public AnomalyStatsApi stats(final ThirdEyePrincipal principal, final @Nullable Long startTime,
      final @Nullable Long endTime) {
    final AnomalyFilter filter = new AnomalyFilter()
        .setStartTimeIsGte(startTime)
        .setEndTimeIsLte(endTime);
    final @Nullable String namespace = authorizationManager.currentNamespace(principal);
    // todo cyril authz usage of dummy entity for access check - avoid this
    authorizationManager.ensureCanRead(principal, new AnomalyDTO().setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace)));
    return anomalyManager.anomalyStats(namespace, filter);
  }

  @Override
  protected void validate(final ThirdEyePrincipal principal, final AnomalyApi api,
      @Nullable final AnomalyDTO existing) {
    super.validate(principal, api, existing);
    ensureExists(api.getAlert(), "alert must be set");
    final long alertId = ensureExists(api.getAlert().getId(), "alert id must be set");
    
    if (existing != null) {
      final Long existingAlertId = existing.getDetectionConfigId();
      checkState(existingAlertId != null, "alert id should be set in the existing anomaly");
      ensure(alertId == existingAlertId, "Edited alert id and existing alert id should be the same");
    }
    // not validating that the alert id is a valid one - the authorization manager will take care of this 
  }
}
