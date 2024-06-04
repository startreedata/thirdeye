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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.events.EventType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestEventManager {

  private long testEventId;
  private EventManager eventDAO;

  @BeforeClass
  void beforeClass() {
    eventDAO = MySqlTestDatabase.sharedInjector().getInstance(EventManager.class);
  }

  @AfterClass
  void clean() {
    eventDAO.findAll().forEach(eventDAO::delete);
  }

  @Test
  public void testCreate() {
    EventDTO eventDTO = new EventDTO();
    eventDTO.setName("test");
    eventDTO.setMetric("test");
    eventDTO.setEventType(EventType.DEPLOYMENT.name());
    eventDTO.setService("testService");
    eventDTO.setStartTime(System.currentTimeMillis() - 10);
    eventDTO.setEndTime(System.currentTimeMillis());
    Map<String, List<String>> targetDimensionsMap = new HashMap<>();
    eventDTO.setTargetDimensionMap(targetDimensionsMap);

    testEventId = eventDAO.save(eventDTO);
    Assert.assertTrue(testEventId > 0);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testGetById() {
    EventDTO testEventDTO = eventDAO.findById(testEventId);
    Assert.assertEquals(testEventDTO.getId().longValue(), testEventId);

    List<EventDTO> results1 = eventDAO
        .findEventsBetweenTimeRangeInNamespace(0,
            System.currentTimeMillis(),
            EventType.DEPLOYMENT.name(),
            null);
    Assert.assertEquals(results1.size(), 1);
  }

  @Test(dependsOnMethods = {"testGetById"})
  public void testDelete() {
    eventDAO.deleteById(testEventId);
    EventDTO testEventDTO = eventDAO.findById(testEventId);
    Assert.assertNull(testEventDTO);
  }
}
