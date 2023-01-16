/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDTO extends AbstractDTO {

  String name;
  String metric;
  String service;
  String eventType;

  long startTime;
  long endTime;

  /**
   * targetDimensionMap will hold metadata of the event. for example holiday event will have
   * countryCode --> {US, CA, ...}
   * DeploymentEvent will have fabric --- > {prod-lva1, prod-ltx1..} hostname ---> {hosta, hostb,
   * hostc...}
   */
  Map<String, List<String>> targetDimensionMap;

  public String getName() {
    return name;
  }

  public EventDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getMetric() {
    return metric;
  }

  public EventDTO setMetric(final String metric) {
    this.metric = metric;
    return this;
  }

  public String getService() {
    return service;
  }

  public EventDTO setService(final String service) {
    this.service = service;
    return this;
  }

  public String getEventType() {
    return eventType;
  }

  public EventDTO setEventType(final String eventType) {
    this.eventType = eventType;
    return this;
  }

  public long getStartTime() {
    return startTime;
  }

  public EventDTO setStartTime(final long startTime) {
    this.startTime = startTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public EventDTO setEndTime(final long endTime) {
    this.endTime = endTime;
    return this;
  }

  public Map<String, List<String>> getTargetDimensionMap() {
    return targetDimensionMap;
  }

  public EventDTO setTargetDimensionMap(
      final Map<String, List<String>> targetDimensionMap) {
    this.targetDimensionMap = targetDimensionMap;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(),
        name,
        eventType,
        targetDimensionMap,
        service,
        metric,
        startTime,
        endTime);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof EventDTO)) {
      return false;
    }
    EventDTO eb = (EventDTO) obj;
    return Objects.equals(getId(), eb.getId())
        && Objects.equals(getName(), eb.getName())
        && Objects.equals(getEventType(), eb.getEventType())
        && Objects.equals(getTargetDimensionMap(), eb.getTargetDimensionMap())
        && Objects.equals(getService(), eb.getService())
        && Objects.equals(getMetric(), eb.getMetric())
        && Objects.equals(getStartTime(), eb.getStartTime())
        && Objects.equals(getEndTime(), eb.getEndTime());
  }
}
