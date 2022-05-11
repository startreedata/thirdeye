/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.events;

import ai.startree.thirdeye.datalayer.bao.AbstractManagerImpl;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Mock events manager for testing
 */
public class MockEventsManager extends AbstractManagerImpl<EventDTO> implements EventManager {

  private final Collection<EventDTO> entities;

  /**
   * Instantiates a new Mock events manager.
   *
   * @param entities the collection of entities
   */
  @Inject
  MockEventsManager(Collection<EventDTO> entities, GenericPojoDao genericPojoDao) {
    super(EventDTO.class, genericPojoDao);
    this.entities = entities;
  }

  @Override
  public List<EventDTO> findAll() {
    return new ArrayList<>(entities);
  }

  @Override
  public Long save(EventDTO entity) {
    entities.add(entity);
    return entity.getId();
  }

  @Override
  public int update(EventDTO entity) {
    for (EventDTO eventDTO : entities) {
      if (eventDTO.getId().equals(entity.getId())) {
        eventDTO = entity;
        return 1;
      }
    }
    return 0;
  }

  @Override
  public int delete(EventDTO entity) {
    for (EventDTO eventDTO : entities) {
      if (eventDTO.getId().equals(entity.getId())) {
        entities.remove(eventDTO);
        return 1;
      }
    }
    return 0;
  }

  @Override
  public List<EventDTO> findByEventType(String eventType) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRangeWithType(String eventType, long startTime, long endTime) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRange(final long startTime, final long endTime) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRangeByName(String eventType, String name,
      long startTime, long endTime) {
    throw new AssertionError("not implemented");
  }
}
