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

import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventFilter {

  private static final Logger LOG = LoggerFactory.getLogger(EventFilter.class);

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
