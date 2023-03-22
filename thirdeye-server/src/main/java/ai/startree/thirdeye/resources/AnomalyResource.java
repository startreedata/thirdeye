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

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.service.AnomalyService;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Anomaly", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AnomalyResource extends CrudResource<AnomalyApi, AnomalyDTO> {

  private final AnomalyService anomalyService;

  @Inject
  public AnomalyResource(final AnomalyService anomalyService) {
    super(anomalyService);
    this.anomalyService = anomalyService;
  }

  @Path("{id}/feedback")
  @POST
  @Timed
  public Response setFeedback(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @PathParam("id") final Long id,
      final AnomalyFeedbackApi api) {
    anomalyService.setFeedback(id, api);

    return Response
        .ok()
        .build();
  }

  @GET
  @Path("stats")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnomalyStats(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @QueryParam("startTime") final Long startTime,
      @QueryParam("endTime") final Long endTime
  ) {
    return Response.ok(anomalyService.stats(startTime, endTime)).build();
  }
}
