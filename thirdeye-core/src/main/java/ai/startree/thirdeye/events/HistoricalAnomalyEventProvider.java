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

import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.events.EventType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricalAnomalyEventProvider implements EventDataProvider<EventDTO> {

  private final MergedAnomalyResultManager mergedAnomalyDAO;

  public HistoricalAnomalyEventProvider(final MergedAnomalyResultManager mergedAnomalyResultDAO) {
    mergedAnomalyDAO = mergedAnomalyResultDAO;
  }

  @Override
  public List<EventDTO> getEvents(EventFilter eventFilter) {
    List<MergedAnomalyResultDTO> mergedAnomalies;
    List<EventDTO> events = new ArrayList<>();
    if (eventFilter.getMetricName() == null) {
      mergedAnomalies =
          mergedAnomalyDAO.findByTime(eventFilter.getStartTime(), eventFilter.getStartTime());
    } else {
      mergedAnomalies = mergedAnomalyDAO
          .findByMetricTime(eventFilter.getMetricName(), eventFilter.getStartTime(),
              eventFilter.getStartTime());
    }

    if (mergedAnomalies != null) {
      for (MergedAnomalyResultDTO mergedAnomalyResultDTO : mergedAnomalies) {
        events.add(getAnomalyEvent(mergedAnomalyResultDTO));
      }
    }

    return events;
  }

  private EventDTO getAnomalyEvent(MergedAnomalyResultDTO anomalyResultDTO) {
    EventDTO eventDTO = new EventDTO();
    eventDTO.setEventType(EventType.HISTORICAL_ANOMALY.name());
    eventDTO.setStartTime(anomalyResultDTO.getStartTime());
    eventDTO.setEndTime(anomalyResultDTO.getEndTime());
    eventDTO.setMetric(anomalyResultDTO.getMetric());
    eventDTO.setId(anomalyResultDTO.getId());
    Map<String, List<String>> dimensionValuesMap = new HashMap<>();
    if (anomalyResultDTO.getDimensions() != null && anomalyResultDTO.getDimensions().size() > 0) {
      for (String dimension : anomalyResultDTO.getDimensions().keySet()) {
        List<String> dimensionValues;
        if (dimensionValuesMap.containsKey(dimension)) {
          dimensionValues = dimensionValuesMap.get(dimension);
        } else {
          dimensionValues = new ArrayList<>();
          dimensionValuesMap.put(dimension, dimensionValues);
        }
        dimensionValues.add(anomalyResultDTO.getDimensions().get(dimension));
      }
    }
    eventDTO.setTargetDimensionMap(dimensionValuesMap);
    return eventDTO;
  }

  @Override
  public String getEventType() {
    return EventType.HISTORICAL_ANOMALY.toString();
  }
}
