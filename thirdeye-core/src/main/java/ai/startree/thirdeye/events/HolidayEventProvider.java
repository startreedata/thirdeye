/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.events;

import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.events.EventType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HolidayEventProvider implements EventDataProvider<EventDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(HolidayEventProvider.class);

  private final EventManager eventDAO;

  public HolidayEventProvider(final EventManager eventManager) {
    eventDAO = eventManager;
  }

  @Override
  public List<EventDTO> getEvents(EventFilter eventFilter) {
    List<EventDTO> allEventsBetweenTimeRange = eventDAO.findEventsBetweenTimeRange(
        eventFilter.getEventType(),
        eventFilter.getStartTime(),
        eventFilter.getEndTime());

    LOG.info("Fetched {} {} events between {} and {}", allEventsBetweenTimeRange.size(),
        eventFilter.getEventType(), eventFilter.getStartTime(), eventFilter.getEndTime());
    return EventFilter
        .applyDimensionFilter(allEventsBetweenTimeRange, eventFilter.getTargetDimensionMap());
  }

  @Override
  public String getEventType() {
    return EventType.HOLIDAY.toString();
  }
}
