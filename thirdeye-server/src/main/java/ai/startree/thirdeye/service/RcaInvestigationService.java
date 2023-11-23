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

import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.ensureNull;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.RcaInvestigationManager;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RcaInvestigationService extends CrudService<RcaInvestigationApi, RcaInvestigationDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("anomaly.id", "anomalyId")
      .put("createdBy.principal",
          "owner") // will most likely be deprecated or underlying column will change - ui will need update to get the owner
      .put("created", "created")
      .build();
  final AnomalyManager anomalyManager;

  @Inject
  public RcaInvestigationService(final RcaInvestigationManager rootCauseSessionDAO,
      final AnomalyManager anomalyManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, rootCauseSessionDAO, API_TO_INDEX_FILTER_MAP);
    this.anomalyManager = anomalyManager;
  }

  @Override
  protected RcaInvestigationDTO createDto(final ThirdEyeServerPrincipal principal,
      final RcaInvestigationApi api) {
    return toDto(api);
  }

  @Override
  protected RcaInvestigationDTO toDto(final RcaInvestigationApi api) {
    return ApiBeanMapper.toDto(api);
  }

  @Override
  protected RcaInvestigationApi toApi(final RcaInvestigationDTO dto) {
    final var investigationApi = ApiBeanMapper.toApi(dto);
    investigationApi.setAuth(new AuthorizationConfigurationApi()
        .setNamespace(authorizationManager.resourceId(dto).getNamespace()));
    return investigationApi;
  }

  @Override
  protected void validate(final RcaInvestigationApi api, final RcaInvestigationDTO existing) {
    super.validate(api, existing);
    ensureExists(api.getName(), "Name must be present");
    ensureNull(api.getAuth(), "cannot set auth for investigations");
  }
}
