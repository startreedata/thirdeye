/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.events.EventType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestEventManager {

  private long testEventId;
  private EventManager eventDAO;

  @BeforeClass
  void beforeClass() {
    eventDAO = new TestDatabase().createInjector().getInstance(EventManager.class);
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
    System.out.println(testEventDTO.getStartTime());
    System.out.println(testEventDTO.getEndTime());
    System.out.println(testEventDTO.getEventType());
    List<EventDTO> results0 = eventDAO.findByEventType(EventType.DEPLOYMENT.name());
    Assert.assertEquals(results0.size(), 1);

    List<EventDTO> results1 = eventDAO
        .findEventsBetweenTimeRange(EventType.DEPLOYMENT.name(), 0, System.currentTimeMillis());
    Assert.assertEquals(results1.size(), 1);
  }

  @Test(dependsOnMethods = {"testGetById"})
  public void testDelete() {
    eventDAO.deleteById(testEventId);
    EventDTO testEventDTO = eventDAO.findById(testEventId);
    Assert.assertNull(testEventDTO);
  }
}
