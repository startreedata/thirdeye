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

import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoader;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoaderConfiguration;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.common.collect.ImmutableMap;
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
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Event", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class EventResource extends CrudResource<EventApi, EventDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("type", "eventType")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .build();
  private final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration;
  private final HolidayEventsLoader holidayEventsLoader;
  private final AnomalyManager anomalyManager;

  @Inject
  public EventResource(
      final EventManager eventManager,
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration,
      final HolidayEventsLoader holidayEventsLoader,
      final AnomalyManager anomalyManager,
      final AuthorizationManager authorizationManager) {
    super(eventManager, API_TO_INDEX_FILTER_MAP, authorizationManager);
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    this.holidayEventsLoader = holidayEventsLoader;
    this.anomalyManager = anomalyManager;
  }

  @Override
  protected EventDTO createDto(final ThirdEyePrincipal principal, final EventApi api) {
    return ApiBeanMapper.toEventDto(api);
  }

  @Override
  protected EventApi toApi(final EventDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @Override
  protected EventDTO toDto(final EventApi api) {
    return ApiBeanMapper.toEventDto(api);
  }

  /**
   * Load the holidays between startTime and endTime to Third Eye database.
   *
   * @param startTime the start time
   * @param endTime the end time
   */
  @POST
  @Path("/holidays/load")
  public Response loadHolidays(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("start_time") long startTime,
      @FormParam("end_time") long endTime
  ) {
    ensure(holidayEventsLoaderConfiguration.isEnabled(),
        ThirdEyeStatus.ERR_CONFIG,
        "Holiday events are disabled.");
    holidayEventsLoader.loadHolidays(startTime, endTime);
    return Response.ok().build();
  }

  @POST
  @Path("/create-from-anomaly")
  public Response loadHolidays(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("anomalyId") long anomalyId
  ) {
    final AnomalyDTO anomalyDto = ensureExists(anomalyManager.findById(
        anomalyId));
    final EventDTO eventDTO = new EventDTO()
        .setName("Anomaly " + anomalyId)
        .setEventType("ANOMALY")
        .setStartTime(anomalyDto.getStartTime())
        .setEndTime(anomalyDto.getEndTime());
    dtoManager.save(eventDTO);
    return Response.ok(toApi(eventDTO)).build();
  }
}
