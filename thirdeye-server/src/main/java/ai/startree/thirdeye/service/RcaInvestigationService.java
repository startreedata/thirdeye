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
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.RcaInvestigationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RcaInvestigationService extends CrudService<RcaInvestigationApi, RcaInvestigationDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(RcaInvestigationService.class);

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
  protected RcaInvestigationDTO toDto(final RcaInvestigationApi api) {
    final RcaInvestigationDTO dto = ApiBeanMapper.toDto(api);
    // fixme authz - once namespace resolver is removed - simply inherit from the anomaly id - or let enrich namespace do its work (this is better for update case)
    final ResourceIdentifier authId = authorizationManager.resourceId(dto);
    dto.setAuth(new AuthorizationConfigurationDTO().setNamespace(authId.getNamespace()));
    return dto;
  }

  @Override
  protected RcaInvestigationApi toApi(final RcaInvestigationDTO dto) {
    final RcaInvestigationApi api = ApiBeanMapper.toApi(dto);
    if (api.getAuth() == null) {
      LOG.warn("RcaInvestigation entity {} has a namespace resolved at read time. " 
          + "Migrating to a namespace resolved at write time.", dto.getId());
      // migrate entities
      final String namespace = authorizationManager.resourceId(dto).getNamespace();
      dto.setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace));
      final int success = dtoManager.update(dto);
      if (success == 0) {
        LOG.error("Failed to migrate namespace for RcaInvestigation entity {}. Please reach out to support.", dto.getId());
        api.setAuth(new AuthorizationConfigurationApi().setNamespace(namespace));
        return api;
      } else {
        return toApi(dto);
      }
    }
    return api;
  }

  @Override
  protected void validate(final ThirdEyePrincipal principal, final RcaInvestigationApi api, final RcaInvestigationDTO existing) {
    super.validate(principal, api, existing);
    ensureExists(api.getName(), "Name must be present");
    ensureExists(api.getAnomaly(), "Anomaly field must be set");
    ensureExists(api.getAnomaly().getId(), "Anomaly id field must be set");
    // not checking here if the anomaly id is valid - it is checked at authz check time - consider redesign?
  }
}
