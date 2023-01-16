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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static ai.startree.thirdeye.util.ResourceUtils.badRequest;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Api(tags = "Enumeration Item", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class EnumerationItemResource extends CrudResource<EnumerationItemApi, EnumerationItemDTO> {

  @Inject
  public EnumerationItemResource(final EnumerationItemManager enumerationItemManager,
      final AuthorizationManager authorizationManager) {
    super(enumerationItemManager, ImmutableMap.of(), authorizationManager);
  }

  @Override
  protected EnumerationItemDTO createDto(final ThirdEyePrincipal principal,
      final EnumerationItemApi api) {
    final EnumerationItemDTO dto = ApiBeanMapper.toEnumerationItemDTO(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected EnumerationItemDTO toDto(final EnumerationItemApi api) {
    throw badRequest(ERR_OPERATION_UNSUPPORTED,
        "Enumeration Items are immutable. You can regenerate from the alert.");
  }

  @Override
  protected EnumerationItemApi toApi(final EnumerationItemDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
