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
package ai.startree.thirdeye.resources;

import ai.startree.thirdeye.service.EnumerationItemService;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Tag(name = "Enumeration Item")
@SecurityRequirement(name="oauth")
@SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth"),
    @SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(name = Constants.NAMESPACE_SECURITY, type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = Constants.NAMESPACE_HTTP_HEADER)
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnumerationItemResource extends CrudResource<EnumerationItemApi, EnumerationItemDTO> {

  @Inject
  public EnumerationItemResource(final EnumerationItemService enumerationItemService) {
    super(enumerationItemService);
  }
}
