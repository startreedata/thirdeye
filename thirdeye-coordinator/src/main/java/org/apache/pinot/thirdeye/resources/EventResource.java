package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;

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
import org.apache.pinot.thirdeye.spi.ThirdEyeStatus;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

  private final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration;
  private final HolidayEventsLoader holidayEventsLoader;

  @Inject
  public EventResource(
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration,
      final HolidayEventsLoader holidayEventsLoader) {
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    this.holidayEventsLoader = holidayEventsLoader;
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
