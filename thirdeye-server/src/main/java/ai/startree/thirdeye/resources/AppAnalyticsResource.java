/*
 * Copyright 2022 StarTree Inc
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

import ai.startree.thirdeye.core.AppAnalyticsService;
import ai.startree.thirdeye.spi.api.AppAnalyticsApi;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "App Analytics")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AppAnalyticsResource {

  private final AppAnalyticsService appAnalyticsService;

  @Inject
  public AppAnalyticsResource(final AppAnalyticsService appAnalyticsService) {
    this.appAnalyticsService = appAnalyticsService;
  }

  public static String appVersion() {
    return AppAnalyticsResource.class.getPackage().getImplementationVersion();
  }

  @GET
  public Response getAnalyticsPayload() {
    return Response.ok(new AppAnalyticsApi()
        .setVersion(appVersion())
        .setUniqueMonitoredMetrics(appAnalyticsService.uniqueMonitoredMetricsCount())
    ).build();
  }

  @GET
  @Path("version")
  public Response getVersion() {
    return Response.ok(appVersion()).build();
  }
}
