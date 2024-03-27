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

import static ai.startree.thirdeye.util.ResourceUtils.ensure;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoaderConfiguration;
import ai.startree.thirdeye.service.EventService;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Tag(name = "Event")
@SecurityRequirement(name="oauth")
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth")
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource extends CrudResource<EventApi, EventDTO> {

  private final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration;
  private final EventService eventService;

  @Inject
  public EventResource(
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration,
      final EventService eventService) {
    super(eventService);
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    this.eventService = eventService;
  }

  /**
   * Load the holidays between startTime and endTime to Third Eye database.
   *
   * @param startTime the start time
   * @param endTime the end time
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("/holidays/load")
  public Response loadHolidays(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @FormParam("start_time") long startTime,
      @FormParam("end_time") long endTime
  ) {
    ensure(holidayEventsLoaderConfiguration.isEnabled(),
        ThirdEyeStatus.ERR_CONFIG,
        "Holiday events are disabled.");
    eventService.loadHolidays(principal, startTime, endTime);
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("/create-from-anomaly")
  public Response createFromAnomaly(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @FormParam("anomalyId") long anomalyId
  ) {
    return Response.ok(eventService.createFromAnomaly(principal, anomalyId)).build();
  }
}
