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
package ai.startree.thirdeye.events;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.datasource.DAORegistry;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.events.EventType;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestHolidayEventProvider {

  public class TestEventManager {

    long testEventId;
    HolidayEventProvider holidayEventProvider = null;
    long hoursAgo5 = new DateTime(DateTimeZone.UTC).minusHours(5).getMillis();
    long hoursAgo4 = new DateTime(DateTimeZone.UTC).minusHours(4).getMillis();
    long hoursAgo3 = new DateTime(DateTimeZone.UTC).minusHours(3).getMillis();
    long hoursAgo2 = new DateTime(DateTimeZone.UTC).minusHours(2).getMillis();

    private TestDbEnv testDAOProvider;
    private EventManager eventDAO;

    @BeforeClass
    void beforeClass() {
      testDAOProvider = new TestDbEnv();
      DAORegistry daoRegistry = TestDbEnv.getInstance();
      eventDAO = daoRegistry.getEventDAO();
      holidayEventProvider = new HolidayEventProvider(TestDbEnv.getInstance().getEventDAO());
    }

    @AfterClass(alwaysRun = true)
    void afterClass() {
      testDAOProvider.cleanup();
    }

    @Test
    public void testGetEvents() {
      EventDTO event1 = new EventDTO();
      event1.setName("event1");
      event1.setEventType(EventType.DEPLOYMENT.toString());
      eventDAO.save(event1);

      EventDTO event2 = new EventDTO();
      event2.setName("event2");
      event2.setEventType(EventType.HOLIDAY.toString());
      event2.setStartTime(hoursAgo4);
      event2.setEndTime(hoursAgo3);
      Map<String, List<String>> eventDimensionMap2 = new HashMap<>();
      eventDimensionMap2.put("country_code", Lists.newArrayList("peru", "brazil"));
      eventDimensionMap2.put("BrowserName", Lists.newArrayList("chrome"));
      event2.setTargetDimensionMap(eventDimensionMap2);
      eventDAO.save(event2);

      EventDTO event3 = new EventDTO();
      event3.setName("event3");
      event3.setStartTime(hoursAgo3);
      event3.setEndTime(hoursAgo2);
      event3.setEventType(EventType.HOLIDAY.toString());
      Map<String, List<String>> eventDimensionMap3 = new HashMap<>();
      eventDimensionMap3.put("country_code", Lists.newArrayList("srilanka", "india"));
      event3.setTargetDimensionMap(eventDimensionMap3);
      eventDAO.save(event3);

      EventDTO event4 = new EventDTO();
      event4.setName("event4");
      event4.setStartTime(hoursAgo4);
      event4.setEndTime(hoursAgo3);
      event4.setEventType(EventType.HOLIDAY.toString());
      Map<String, List<String>> eventDimensionMap4 = new HashMap<>();
      eventDimensionMap4.put("country_code", Lists.newArrayList("srilanka", "india"));
      event4.setTargetDimensionMap(eventDimensionMap3);
      eventDAO.save(event4);

      Assert.assertEquals(eventDAO.findAll().size(), 4);

      // invalid time
      EventFilter eventFilter = new EventFilter();
      List<EventDTO> events = holidayEventProvider.getEvents(eventFilter);
      Assert.assertEquals(events.size(), 0);

      // check that it gets all HOLIDAY events in time range, and only HOLIDAY events
      eventFilter.setStartTime(hoursAgo5);
      eventFilter.setEndTime(hoursAgo3);
      eventFilter.setEventType(EventType.HOLIDAY.name());
      events = holidayEventProvider.getEvents(eventFilter);
      Assert.assertEquals(events.size(), 2);

      // check for HOLIDAY events in time range and filters
      Map<String, List<String>> filterMap = new HashMap<>();
      filterMap.put("country_code", Lists.newArrayList("india"));
      eventFilter.setTargetDimensionMap(filterMap);
      events = holidayEventProvider.getEvents(eventFilter);
      Assert.assertEquals(events.size(), 1);
    }
  }
}
