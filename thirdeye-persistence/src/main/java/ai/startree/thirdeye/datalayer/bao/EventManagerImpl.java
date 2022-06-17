/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

  @Override
  public List<EventDTO> findEventsBetweenTimeRange(final long startTime, final long endTime) {
    Predicate predicate = Predicate
        .AND(Predicate.GT("endTime", startTime),
            Predicate.LT("startTime", endTime));
    return findByPredicate(predicate);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRange(final long startTime,
      final long endTime, @Nullable final String eventType) {
    if (eventType == null) {
      return findEventsBetweenTimeRange(startTime, endTime);
    }
    Predicate predicate = Predicate
        .AND(Predicate.EQ("eventType", eventType), Predicate.GT("endTime", startTime),
            Predicate.LT("startTime", endTime));
    return findByPredicate(predicate);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRange(final long startTime, final long endTime,
      final @Nullable String eventType, final @Nullable Map<String, Set<String>> dimensionFilters) {
    List<EventDTO> events = findEventsBetweenTimeRange(startTime, endTime, eventType);

    return applyDimensionFilters(events, dimensionFilters);
  }

  @VisibleForTesting
  protected static List<EventDTO> applyDimensionFilters(@NonNull final List<EventDTO> events,
      final @Nullable Map<String, Set<String>> dimensionFilters) {
    if (dimensionFilters == null || dimensionFilters.isEmpty()) {
      return events;
    }

    final List<EventDTO> filteredEvents = new ArrayList<>();
    for (EventDTO e : events) {
      Map<String, List<String>> eventDimensions = e.getTargetDimensionMap();
      boolean matchFilter = true;
      for (String filterKey : dimensionFilters.keySet()) {
        final Set<String> filterValues = dimensionFilters.get(filterKey);
        final List<String> eventValues = eventDimensions.get(filterKey);
        if (eventValues == null) {
          matchFilter = false;
        } else {
          matchFilter = eventValues.stream().anyMatch(filterValues::contains);
        }
        if (!matchFilter) {
          break;
        }
      }

      if (matchFilter) {
        filteredEvents.add(e);
      }
    }

    return filteredEvents;
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRangeByName(String eventType, String name, long start,
      long end) {
    Predicate predicate = Predicate
        .AND(Predicate.EQ("eventType", eventType), Predicate.EQ("name", name),
            Predicate.GT("endTime", start), Predicate.LT("startTime", end));
    return findByPredicate(predicate);
  }
}
