package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.events.HolidayEventsLoader;
import org.apache.pinot.thirdeye.events.HolidayEventsLoaderConfiguration;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.ThirdEyeStatus;
import org.apache.pinot.thirdeye.spi.api.EventApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EventDTO;

@Api(tags = "Event")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class EventResource extends CrudResource<EventApi, EventDTO>{

  public static final ImmutableMap<String, String> API_TO_BEAN_MAP = ImmutableMap.<String, String>builder()
      .put("type", "eventType")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .build();
  private final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration;
  private final HolidayEventsLoader holidayEventsLoader;

  @Inject
  public EventResource(
      final EventManager eventManager,
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration,
      final HolidayEventsLoader holidayEventsLoader) {
    super(eventManager, API_TO_BEAN_MAP);
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    this.holidayEventsLoader = holidayEventsLoader;
  }

  @Override
  protected EventDTO createDto(final ThirdEyePrincipal principal, final EventApi api) {
    return ApiBeanMapper.toEventDto(api);
  }

  @Override
  protected EventApi toApi(final EventDTO dto) {
    return ApiBeanMapper.toApi(dto);
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
      @Auth ThirdEyePrincipal principal,
      @FormParam("start_time") long startTime,
      @FormParam("end_time") long endTime
  ) {
    ensure(holidayEventsLoaderConfiguration.isEnabled(),
        ThirdEyeStatus.ERR_CONFIG,
        "Holiday events are disabled.");
    holidayEventsLoader.loadHolidays(startTime, endTime);
    return Response.ok().build();
  }
}
