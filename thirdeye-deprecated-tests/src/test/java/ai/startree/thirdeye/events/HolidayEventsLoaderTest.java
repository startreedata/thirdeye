/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.events;

import static org.mockito.Mockito.mock;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.events.EventType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Holiday events loader test.
 */
public class HolidayEventsLoaderTest {

  private HolidayEventsLoader holidayEventsLoader;
  private Map<String, List<EventDTO>> holidayNameToHolidayEvent;
  private MockEventsManager eventsDAO;

  /**
   * Before method.
   */
  @BeforeMethod
  public void BeforeMethod() {
    HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration = new HolidayEventsLoaderConfiguration();
    holidayEventsLoaderConfiguration.setCalendars(Arrays.asList("US_HOLIDAY", "CHINA_HOLIDAY"));
    holidayEventsLoaderConfiguration.setHolidayLoadRange(2592000000L);
    EventDTO eventDTO = new EventDTO();
    EventDTO anotherEventDTO = new EventDTO();
    eventDTO.setName("Some festival");
    anotherEventDTO.setName("Disappeared festival");
    Map<String, List<String>> countryCodes = new HashMap<>();
    countryCodes.put("countryCode", Arrays.asList("us", "jp", "cn", "ca"));
    anotherEventDTO.setTargetDimensionMap(countryCodes);
    eventDTO.setId(0L);
    anotherEventDTO.setId(1L);
    eventsDAO = new MockEventsManager(new HashSet<>(Arrays.asList(eventDTO, anotherEventDTO)),
        null);
    holidayEventsLoader = new HolidayEventsLoader(mock(ThirdEyeServerConfiguration.class),
        eventsDAO,
        holidayEventsLoaderConfiguration);
  }

  /**
   * Test get holiday name to event dto map function.
   */
  @Test
  public void testGetHolidayNameToEventDtoMap() {
    Map<HolidayEvent, Set<String>> newHolidayEventToCountryCodes = new HashMap<>();
    HolidayEvent firstHolidayEvent =
        new HolidayEvent("Some festival", EventType.HOLIDAY.toString(),
            1521676800L, 1521763200L);

    HolidayEvent secondHolidayEvent =
        new HolidayEvent("Some special day", EventType.HOLIDAY.toString(),
            1521676800L,
            1521763200L);

    HolidayEvent thirdHolidayEvent =
        new HolidayEvent("Some festival", EventType.HOLIDAY.toString(),
            1521504000L, 1521590400L);

    newHolidayEventToCountryCodes.put(firstHolidayEvent, new HashSet<>(Arrays.asList("us", "cn")));
    newHolidayEventToCountryCodes.put(secondHolidayEvent, Collections.singleton("us"));
    newHolidayEventToCountryCodes.put(thirdHolidayEvent, Collections.singleton("uk"));

    holidayNameToHolidayEvent = holidayEventsLoader
        .getHolidayNameToEventDtoMap(newHolidayEventToCountryCodes);
    Assert.assertEquals(holidayNameToHolidayEvent.get("Some festival").size(), 2);
    Assert.assertEquals(holidayNameToHolidayEvent.get("Some special day").size(), 1);
    Assert.assertEquals(holidayNameToHolidayEvent.get("Some festival").get(0).getName(),
        "Some festival");
    Assert.assertEquals(holidayNameToHolidayEvent.get("Some festival").get(1).getName(),
        "Some festival");
  }

  /**
   * Test merge with existing holidays.
   */
  @Test(dependsOnMethods = "testGetHolidayNameToEventDtoMap")
  public void testMergeWithExistingHolidays() {
    holidayEventsLoader.mergeWithExistingHolidays(holidayNameToHolidayEvent, eventsDAO.findAll());
    List<EventDTO> holidays = eventsDAO.findAll();
    Map<String, List<EventDTO>> eventNameToEventDto = new HashMap<>();
    for (EventDTO holiday : holidays) {
      String holidayName = holiday.getName();
      if (!eventNameToEventDto.containsKey(holidayName)) {
        eventNameToEventDto.put(holidayName, new ArrayList<EventDTO>());
      }
      eventNameToEventDto.get(holidayName).add(holiday);
    }
    Assert.assertFalse(eventNameToEventDto.containsKey("Disappeared festival"));
    Assert.assertTrue(eventNameToEventDto.containsKey("Some special day"));
    Assert.assertEquals(eventNameToEventDto.get("Some special day").size(), 1);
    Assert.assertEquals(eventNameToEventDto.get("Some special day").get(0).getTargetDimensionMap()
            .get("countryCode"),
        Collections.singletonList("us"));
    Assert.assertTrue(eventNameToEventDto.containsKey("Some festival"));
    List<EventDTO> festivalEvents = eventNameToEventDto.get("Some festival");

    Assert.assertEquals(festivalEvents.size(), 2);

    festivalEvents.sort(Comparator.comparingInt(o -> o.getTargetDimensionMap()
        .get("countryCode")
        .size()));

    Assert.assertTrue(festivalEvents.get(0)
        .getTargetDimensionMap()
        .get("countryCode")
        .equals(Collections.singletonList("uk")) && new HashSet<>(festivalEvents.get(1)
        .getTargetDimensionMap()
        .get("countryCode")).equals(new HashSet<>(Arrays.asList("us", "cn"))));
  }
}
