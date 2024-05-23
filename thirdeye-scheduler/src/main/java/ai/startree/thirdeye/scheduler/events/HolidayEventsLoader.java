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
package ai.startree.thirdeye.scheduler.events;

import static java.util.Collections.singleton;

import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.events.EventType;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.icu.util.TimeZone;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Holiday events loader, which loads the holiday events from Google Calendar periodically.
 */
@Singleton
public class HolidayEventsLoader implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(HolidayEventsLoader.class);
  private static final String NO_COUNTRY_CODE = "no country code";
  /**
   * Override the time zone code for a country
   */
  private static final Map<String, String> COUNTRY_TO_TIMEZONE = ImmutableMap.of("US", "PST");
  /**
   * Global instance of the HTTP transport.
   */
  private static HttpTransport HTTP_TRANSPORT;

  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    } catch (Exception e) {
      LOG.error("Can't create http transport with google api.", e);
    }
  }

  private final HolidayEventsLoaderConfiguration config;
  /**
   * Calendar Api private key path
   */
  private final String keyPath;
  private final ScheduledExecutorService scheduledExecutorService;
  private final EventManager eventManager;

  @Inject
  public HolidayEventsLoader(
      final EventManager eventManager,
      final HolidayEventsLoaderConfiguration config) {
    this.config = config;
    this.keyPath = config.getGoogleJsonKeyPath();
    this.eventManager = eventManager;
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder().setNameFormat(
            "holiday-events-loader-%d").build());
  }

  public void start() {
    scheduledExecutorService
        .scheduleAtFixedRate(this, 0, config.getRunFrequency(), TimeUnit.DAYS);
  }

  public void shutdown() {
    scheduledExecutorService.shutdown();
  }

  /**
   * Fetch holidays and save to ThirdEye database.
   */
  public void run() {
    final long start = System.currentTimeMillis();
    final long end = start + config.getHolidayLoadRange();

    loadHolidays(start, end);
  }

  public void loadHolidays(long start, long end) {
    LOG.info("Loading holidays between {} and {}", start, end);
    final List<Event> newHolidays;
    try {
      newHolidays = getAllHolidays(start, end);
    } catch (Exception e) {
      LOG.error("Fetch holidays failed. Aborting.", e);
      return;
    }
    final Map<HolidayEvent, Set<String>> newHolidayEventToCountryCodes = aggregateCountryCodesGroupByHolidays(
        newHolidays);

    final Map<String, List<EventDTO>> holidayNameToHolidayEvent = getHolidayNameToEventDtoMap(
        newHolidayEventToCountryCodes);

    // Get the existing holidays within the time range from the database
    final List<EventDTO> existingEvents = eventManager
        .findEventsBetweenTimeRange(start, end, EventType.HOLIDAY.toString());

    mergeWithExistingHolidays(holidayNameToHolidayEvent, existingEvents);
  }

  private Map<HolidayEvent, Set<String>> aggregateCountryCodesGroupByHolidays(
      List<Event> newHolidays) {
    // A map from new holiday to a set of country codes that has the holiday
    final Map<HolidayEvent, Set<String>> newHolidayEventToCountryCodes = new HashMap<>();

    // Convert Google Event Type to holiday events and aggregates the country code list
    for (Event holiday : newHolidays) {
      final String countryCode = getCountryCode(holiday);
      final String timeZone = getTimeZoneForCountry(countryCode);
      final HolidayEvent holidayEvent =
          new HolidayEvent(holiday.getSummary(), EventType.HOLIDAY.toString(),
              getUtcTimeStamp(holiday.getStart().getDate().getValue(), timeZone),
              getUtcTimeStamp(holiday.getEnd().getDate().getValue(), timeZone));
      if (!newHolidayEventToCountryCodes.containsKey(holidayEvent)) {
        newHolidayEventToCountryCodes.put(holidayEvent, new HashSet<>());
      }
      if (!countryCode.equals(NO_COUNTRY_CODE)) {
        newHolidayEventToCountryCodes.get(holidayEvent).add(countryCode);
      }
      LOG.info("Get holiday event {} in country {} between {} and {} in timezone {} ",
          holidayEvent.getName(),
          countryCode, holidayEvent.getStartTime(), holidayEvent.getEndTime(), timeZone);
    }
    return newHolidayEventToCountryCodes;
  }

  private long getUtcTimeStamp(long timeStamp, String timeZone) {
    return timeStamp - TimeZone.getTimeZone(timeZone).getOffset(timeStamp);
  }

  private String getTimeZoneForCountry(String countryCode) {
    // if time zone of a country is set explicitly
    if (COUNTRY_TO_TIMEZONE.containsKey(countryCode)) {
      return COUNTRY_TO_TIMEZONE.get(countryCode);
    }
    // guess the time zone from country code
    String timeZone = "GMT";
    final String[] timeZones = TimeZone.getAvailableIDs(countryCode);
    if (timeZones.length != 0) {
      timeZone = timeZones[0];
    }
    return timeZone;
  }

  public Map<String, List<EventDTO>> getHolidayNameToEventDtoMap(
      Map<HolidayEvent, Set<String>> newHolidayEventToCountryCodes) {
    final Map<String, List<EventDTO>> holidayNameToHolidayEvent = new HashMap<>();

    // Convert Holiday Events to EventDTOs.
    for (Map.Entry<HolidayEvent, Set<String>> entry : newHolidayEventToCountryCodes.entrySet()) {
      final HolidayEvent newHolidayEvent = entry.getKey();
      final Set<String> newCountryCodes = entry.getValue();
      final String holidayName = newHolidayEvent.getName();

      final EventDTO eventDTO = new EventDTO();
      eventDTO.setName(holidayName);
      eventDTO.setEventType(newHolidayEvent.getEventType());
      eventDTO.setStartTime(newHolidayEvent.getStartTime());
      eventDTO.setEndTime(newHolidayEvent.getEndTime());

      final Map<String, List<String>> targetDimensionMap = new HashMap<>();
      targetDimensionMap.put("countryCode", new ArrayList<>(newCountryCodes));
      eventDTO.setTargetDimensionMap(targetDimensionMap);

      if (!holidayNameToHolidayEvent.containsKey(holidayName)) {
        holidayNameToHolidayEvent.put(holidayName, new ArrayList<>());
      }
      holidayNameToHolidayEvent.get(holidayName).add(eventDTO);
    }
    return holidayNameToHolidayEvent;
  }

  private void mergeWithExistingHolidays(Map<String, List<EventDTO>> holidayNameToHolidayEvent,
      List<EventDTO> existingEvents) {
    for (EventDTO existingEvent : existingEvents) {
      final String holidayName = existingEvent.getName();
      if (!holidayNameToHolidayEvent.containsKey(holidayName)) {
        // If a event disappears, delete the event
        eventManager.delete(existingEvent);
      } else {
        // If an existing event shows up again, overwrite with new time and country code.
        final List<EventDTO> eventList = holidayNameToHolidayEvent.get(holidayName);
        final EventDTO newEvent = eventList.remove(eventList.size() - 1);

        existingEvent.setStartTime(newEvent.getStartTime());
        existingEvent.setEndTime(newEvent.getEndTime());
        existingEvent.setTargetDimensionMap(newEvent.getTargetDimensionMap());
        eventManager.update(existingEvent);

        if (eventList.isEmpty()) {
          holidayNameToHolidayEvent.remove(holidayName);
        }
      }
    }

    // Add all remaining new events into the database
    for (List<EventDTO> eventDTOList : holidayNameToHolidayEvent.values()) {
      for (EventDTO eventDTO : eventDTOList) {
        eventManager.save(eventDTO);
      }
    }
  }

  private String getCountryCode(Event holiday) {
    final String calendarName = holiday.getCreator().getDisplayName();
    if (calendarName != null && calendarName.length() > 12) {
      String countryName = calendarName.substring(12);
      for (Locale locale : Locale.getAvailableLocales()) {
        if (locale.getDisplayCountry().equals(countryName)) {
          return locale.getCountry();
        }
      }
    }
    return NO_COUNTRY_CODE;
  }

  /**
   * Fetch holidays from all calendars in Google Calendar API
   *
   * @param start Lower bound (inclusive) for an holiday's end time to filter by.
   * @param end Upper bound (exclusive) for an holiday's start time to filter by.
   */
  private List<Event> getAllHolidays(long start, long end) throws Exception {
    final List<Event> events = new ArrayList<>();
    for (String calendar : config.getCalendars()) {
      try {
        events.addAll(this.getCalendarEvents(calendar, start, end));
      } catch (GoogleJsonResponseException e) {
        LOG.warn("Fetch holiday events failed in calendar {}.", calendar, e);
      }
    }
    return events;
  }

  private List<Event> getCalendarEvents(String Calendar_id, long start, long end) throws Exception {
    final GoogleCredential credential = GoogleCredential
        .fromStream(new FileInputStream(keyPath))
        .createScoped(singleton(CalendarScopes.CALENDAR_READONLY));

    final Calendar calendar = new Calendar
        .Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), credential)
        .setApplicationName("thirdeye")
        .build();

    return calendar.events()
        .list(Calendar_id)
        .setTimeMin(new DateTime(start))
        .setTimeMax(new DateTime(end))
        .execute()
        .getItems();
  }
}
