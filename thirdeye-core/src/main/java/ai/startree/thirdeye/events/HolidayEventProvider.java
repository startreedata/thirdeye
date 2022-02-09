/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.events;

import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.detection.events.EventType;
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
