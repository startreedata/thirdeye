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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
        eventEnd,
        new HashMap<>());
    holidays.sort(Comparator.comparingLong(EventDTO::getStartTime));

    return holidays.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  /**
   * Taking advantage of event data provider, extract the events around the given start and end time
   *
   * @param start the start time of the event, preEventCrawlOffset is added before the given
   *     date time
   * @param end the end time of the event, postEventCrawlOffset is added after the given date
   *     time
   * @param targetDimensions the affected dimensions
   * @return a list of related events
   */
  private List<EventDTO> getHolidayEvents(final DateTime start, final DateTime end,
      final Map<String, List<String>> targetDimensions) {
    final EventFilter eventFilter = new EventFilter();
    eventFilter.setEventType(EventType.HOLIDAY.name());
    eventFilter.setStartTime(start.minus(preEventCrawlOffset).getMillis());
    eventFilter.setEndTime(end.plus(postEventCrawlOffset).getMillis());
    eventFilter.setTargetDimensionMap(targetDimensions);

    LOG.info("Fetching holidays with preEventCrawlOffset {} and postEventCrawlOffset {}",
        preEventCrawlOffset, postEventCrawlOffset);
    return getEvents(eventFilter);
  }

  public List<EventDTO> getEvents(EventFilter eventFilter) {
    List<EventDTO> allEventsBetweenTimeRange = eventDao.findEventsBetweenTimeRangeInNamespace(
        eventFilter.getStartTime(),
        eventFilter.getEndTime(),
        eventFilter.getEventType(),
        namespace);

    LOG.info("Fetched {} {} events between {} and {}", allEventsBetweenTimeRange.size(),
        eventFilter.getEventType(), eventFilter.getStartTime(), eventFilter.getEndTime());
    return EventFilter
        .applyDimensionFilter(allEventsBetweenTimeRange, eventFilter.getTargetDimensionMap());
  }

  public static class EventFilter {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationEventsBuilder.EventFilter.class);

    String eventType;
    String serviceName;
    String metricName;
    long startTime;
    long endTime;
    Map<String, List<String>> targetDimensionMap;

    public Map<String, List<String>> getTargetDimensionMap() {
      return targetDimensionMap;
    }

    public void setTargetDimensionMap(Map<String, List<String>> targetDimensionMap) {
      this.targetDimensionMap = targetDimensionMap;
    }

    public String getMetricName() {
      return metricName;
    }

    public void setMetricName(String metricName) {
      this.metricName = metricName;
    }

    public long getEndTime() {
      return endTime;
    }

    public void setEndTime(long endTime) {
      this.endTime = endTime;
    }

    public String getEventType() {
      return eventType;
    }

    public void setEventType(String eventType) {
      this.eventType = eventType;
    }

    public String getServiceName() {
      return serviceName;
    }

    public void setServiceName(String serviceName) {
      this.serviceName = serviceName;
    }

    public long getStartTime() {
      return startTime;
    }

    public void setStartTime(long startTime) {
      this.startTime = startTime;
    }

    /**
     * Helper method to filter out from list of events, only those events which match the
     * filterDimensions map
     * Each event can have a dimensions map with (key:value) = (dimension name : list of dimension
     * values)
     * The eventFilterDimension map contains a similar schema map.
     * the job of this method is to only pass those events, which meet atleast one of the value filter
     * for atleast one dimension
     * Eg: If event has map { (country):(us), (browser):(chrome) } and event filter has map {
     * (country_code) : (us, india)},
     * this qualifies as a pass from the method.
     * This method also does some basic dimension name and value transformation, such as standardizing
     * case and removing non-alphanumeric
     * Eventually we would have a standardization pipeline, which would rid us of the need to do any
     * standardization in this method,
     * and also handle more complex standardization such as US=USA,Unites States, etc
     *
     * @param allEvents - all events, with no filtering applied
     * @param eventFilterDimensionMap - filter criteria based on dimension names and values
     */
    public static List<EventDTO> applyDimensionFilter(List<EventDTO> allEvents,
        Map<String, List<String>> eventFilterDimensionMap) {
      List<EventDTO> filteredEvents = new ArrayList<>();

      if (CollectionUtils.isNotEmpty(allEvents)) {

        // if filter map not empty, filter events
        if (MapUtils.isNotEmpty(eventFilterDimensionMap)) {
          // go over each event
          for (EventDTO event : allEvents) {
            boolean eventAdded = false;
            Map<String, List<String>> eventDimensionMap = event.getTargetDimensionMap();

            // if dimension map is empty, this event will be skipped, because we know that event filter is not empty
            if (MapUtils.isNotEmpty(eventDimensionMap)) {

              // go over each dimension in event's dimension map, to see if it passes any filter
              for (Entry<String, List<String>> eventMapEntry : eventDimensionMap.entrySet()) {
                // TODO: get this transformation from standardization table
                String eventDimension = eventMapEntry.getKey();
                String eventDimensionTransformed = transformDimensionName(eventDimension);
                List<String> eventDimensionValues = eventMapEntry.getValue();
                List<String> eventDimensionValuesTransformed = transformDimensionValues(
                    eventDimensionValues);

                // for each filter_dimension : dimension_values pair
                for (Entry<String, List<String>> filterMapEntry : eventFilterDimensionMap
                    .entrySet()) {
                  // TODO: get this transformation from standardization table
                  String filterDimension = filterMapEntry.getKey();
                  String filterDimensionTransformed = transformDimensionName(filterDimension);
                  List<String> filterDimensionValues = filterMapEntry.getValue();
                  List<String> filteredDimensionValuesTransformed = transformDimensionValues(
                      filterDimensionValues);

                  // if event has this dimension to filter on
                  if (eventDimensionTransformed.contains(filterDimensionTransformed) ||
                      filterDimensionTransformed.contains(eventDimensionTransformed)) {
                    // and if it matches any of the filter values, add it
                    Set<String> eventDimensionValuesSet = new HashSet<>(
                        eventDimensionValuesTransformed);
                    eventDimensionValuesSet.retainAll(filteredDimensionValuesTransformed);
                    if (!eventDimensionValuesSet.isEmpty()) {
                      filteredEvents.add(event);
                      eventAdded = true;
                      break;
                    }
                  }
                }
                if (eventAdded) {
                  break;
                }
              }
            }
          }
        } else {
          filteredEvents.addAll(allEvents);
        }
      }

      LOG.info("Whitelisting complete. Returning {} fetched events after whitelist",
          filteredEvents.size());
      return filteredEvents;
    }

    private static String transformDimensionName(String dimensionName) {
      String dimensionNameTransformed = dimensionName.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
      return dimensionNameTransformed;
    }

    private static List<String> transformDimensionValues(List<String> dimensionValues) {
      List<String> dimensionValuesTransformed = new ArrayList<>();
      if (dimensionValues != null) {
        for (String value : dimensionValues) {
          dimensionValuesTransformed.add(value.toLowerCase());
        }
      }
      return dimensionValuesTransformed;
    }
  }
}
