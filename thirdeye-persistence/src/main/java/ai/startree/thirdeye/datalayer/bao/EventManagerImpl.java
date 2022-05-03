/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class EventManagerImpl extends AbstractManagerImpl<EventDTO> implements EventManager {

  @Inject
  public EventManagerImpl(GenericPojoDao genericPojoDao) {
    super(EventDTO.class, genericPojoDao);
  }

  public List<EventDTO> findByEventType(String eventType) {
    Predicate predicate = Predicate.EQ("eventType", eventType);
    return findByPredicate(predicate);
  }

  public List<EventDTO> findEventsBetweenTimeRangeWithType(String eventType, long startTime,
      long endTime) {
    Predicate predicate = Predicate
        .AND(Predicate.EQ("eventType", eventType), Predicate.GT("endTime", startTime),
            Predicate.LT("startTime", endTime));
    return findByPredicate(predicate);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRange(final long startTime, final long endTime) {
    Predicate predicate = Predicate
        .AND(Predicate.GT("endTime", startTime),
            Predicate.LT("startTime", endTime));
    return findByPredicate(predicate);
  }

  public List<EventDTO> findEventsBetweenTimeRangeByName(String eventType, String name, long start,
      long end) {
    Predicate predicate = Predicate
        .AND(Predicate.EQ("eventType", eventType), Predicate.EQ("name", name),
            Predicate.GT("endTime", start), Predicate.LT("startTime", end));
    return findByPredicate(predicate);
  }
}
