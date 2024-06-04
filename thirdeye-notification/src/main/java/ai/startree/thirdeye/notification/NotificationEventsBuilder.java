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
package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.config.TimeConfiguration;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.events.EventType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This email formatter lists the anomalies by their functions or metric.
 */
@Singleton
public class NotificationEventsBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationEventsBuilder.class);

  private final EventManager eventDao;

  private final DateTimeZone dateTimeZone;
  private final Period preEventCrawlOffset;
  private final Period postEventCrawlOffset;

  @Inject
  public NotificationEventsBuilder(final EventManager eventDao,
      final TimeConfiguration timeConfiguration) {
    this.eventDao = eventDao;
    dateTimeZone = timeConfiguration.getTimezone();

    final Period defaultPeriod = isoPeriod(Constants.NOTIFICATIONS_DEFAULT_EVENT_CRAWL_OFFSET);
    preEventCrawlOffset = defaultPeriod;
    postEventCrawlOffset = defaultPeriod;
  }

  public List<EventApi> getRelatedEvents(final Collection<AnomalyDTO> anomalies) {
    DateTime windowStart = DateTime.now(dateTimeZone);
    DateTime windowEnd = new DateTime(0, dateTimeZone);

    for (final AnomalyDTO anomaly : anomalies) {
      final DateTime anomalyStartTime = new DateTime(anomaly.getStartTime(), dateTimeZone);
      final DateTime anomalyEndTime = new DateTime(anomaly.getEndTime(), dateTimeZone);

      if (anomalyStartTime.isBefore(windowStart)) {
        windowStart = anomalyStartTime;
      }
      if (anomalyEndTime.isAfter(windowEnd)) {
        windowEnd = anomalyEndTime;
      }
    }

    // holidays
    final DateTime eventStart = windowStart.minus(preEventCrawlOffset);
    final DateTime eventEnd = windowEnd.plus(postEventCrawlOffset);
    final List<EventDTO> holidays = getHolidayEvents(
        eventStart,
        eventEnd);
    holidays.sort(Comparator.comparingLong(EventDTO::getStartTime));

    return holidays.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  /**
   * Fetch the events around a start and an end time
   *
   * @param start the start time of the event, preEventCrawlOffset is added before the given
   *     date time
   * @param end the end time of the event, postEventCrawlOffset is added after the given date
   *     time
   * @return a list of related events
   */
  private List<EventDTO> getHolidayEvents(final DateTime start, final DateTime end) {
    LOG.info("Fetching holidays with preEventCrawlOffset {} and postEventCrawlOffset {}",
        preEventCrawlOffset, postEventCrawlOffset);
    final long startTimeWithOffsetMillis = start.minus(preEventCrawlOffset).getMillis();
    final long endTimeWithOffsetMillis = end.plus(postEventCrawlOffset).getMillis();
    final List<EventDTO> allEventsBetweenTimeRange = eventDao.findEventsBetweenTimeRangeInNamespace(
        startTimeWithOffsetMillis,
        endTimeWithOffsetMillis,
        EventType.HOLIDAY.name(),
        namespace);

    LOG.info("Fetched {} {} events between {} and {}", allEventsBetweenTimeRange.size(),
        EventType.HOLIDAY.name(), startTimeWithOffsetMillis, endTimeWithOffsetMillis);
    return allEventsBetweenTimeRange;
  }
}
