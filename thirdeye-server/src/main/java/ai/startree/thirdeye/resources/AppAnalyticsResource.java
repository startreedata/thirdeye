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

import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.AppAnalyticsService;
import io.dropwizard.auth.Auth;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Tag(name = "App Analytics")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AppAnalyticsResource {

  private final AppAnalyticsService appAnalyticsService;

  @Inject
  public AppAnalyticsResource(final AppAnalyticsService appAnalyticsService) {
    this.appAnalyticsService = appAnalyticsService;
  }

  @GET
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(
      @Parameter(hidden = true) @Auth final ThirdEyeServerPrincipal principal,
      @QueryParam("startTime") final Long startTime,
      @QueryParam("endTime") final Long endTime
  ) {
    return respondOk(appAnalyticsService.getAppAnalytics(principal, startTime, endTime));
  }

  @GET
  @Path("version")
  public Response getVersion() {
    return Response.ok(appAnalyticsService.appVersion(null)).build();
  }
}
