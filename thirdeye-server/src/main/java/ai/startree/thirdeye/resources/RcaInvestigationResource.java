/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import ai.startree.thirdeye.spi.datalayer.bao.RcaInvestigationManager;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Api(authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaInvestigationResource extends CrudResource<RcaInvestigationApi, RcaInvestigationDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("anomaly.id", "anomalyId")
      .put("createdBy.principal", "owner") // will most likely be deprecated or underlying column will change - ui will need update to get the owner
      .put("created", "created")
      .build();

  @Inject
  public RcaInvestigationResource(final RcaInvestigationManager rootCauseSessionDAO) {
    super(rootCauseSessionDAO, API_TO_INDEX_FILTER_MAP);
  }

  @Override
  protected RcaInvestigationDTO createDto(final ThirdEyePrincipal principal,
      final RcaInvestigationApi api) {
    final RcaInvestigationDTO rcaInvestigationDTO = ApiBeanMapper.toDto(api);
    rcaInvestigationDTO.setCreatedBy(principal.getName());
    return rcaInvestigationDTO;
  }

  @Override
  protected RcaInvestigationDTO toDto(final RcaInvestigationApi api) {
    return ApiBeanMapper.toDto(api);
  }

  @Override
  protected RcaInvestigationApi toApi(final RcaInvestigationDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @Override
  protected void validate(final RcaInvestigationApi api, final RcaInvestigationDTO existing) {
    super.validate(api, existing);
    ensureExists(api.getName(), "Name must be present");
  }
}
