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

import static ai.startree.thirdeye.ResourceUtils.respondOk;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.AlertTemplateService;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import io.dropwizard.auth.Auth;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Tag(name = "Alert Template")
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
public class AlertTemplateResource extends CrudResource<AlertTemplateApi, AlertTemplateDTO> {

  private final AlertTemplateService alertTemplateService;

  @Inject
  public AlertTemplateResource(final AlertTemplateService alertTemplateService) {
    super(alertTemplateService);
    this.alertTemplateService = alertTemplateService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("load-defaults")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  public Response loadRecommendedTemplates(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      @FormParam("updateExisting") final boolean updateExisting) {

    return respondOk(alertTemplateService.loadRecommendedTemplates(principal, updateExisting));
  }
}
