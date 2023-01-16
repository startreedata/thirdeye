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

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
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

@Api(tags = "Dataset", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class DatasetResource extends CrudResource<DatasetApi, DatasetConfigDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("name", "dataset")
      .build();

  @Inject
  public DatasetResource(final DatasetConfigManager datasetConfigManager,
      final AuthorizationManager authorizationManager) {
    super(datasetConfigManager, API_TO_INDEX_FILTER_MAP, authorizationManager);
  }

  @Override
  protected DatasetConfigDTO createDto(final ThirdEyePrincipal principal,
      final DatasetApi api) {
    final DatasetConfigDTO dto = toDto(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected DatasetConfigDTO toDto(final DatasetApi api) {
    return ApiBeanMapper.toDatasetConfigDto(api);
  }

  @Override
  protected DatasetApi toApi(final DatasetConfigDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
