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
package ai.startree.thirdeye.detectionpipeline.spec;

import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventFetcherSpec extends AbstractSpec {

  /**
   * Period Java ISO8601 standard.
   * Used to apply a lookback to the startTime.
   * Eg minTime=startTime - lookback
   */
  private String startTimeLookback;

  /**
   * Period Java ISO8601 standard.
   * Used to apply a lookback to the endTime.
   * Eg maxTime=endTime - lookback
   */
  private String endTimeLookback;

  /**
   * Period Java ISO8601 standard.
   * Used to apply a margin on the event lookup limits.
   * Eg [minTime-lookaround, maxTime+lookaround]
   */
  private String lookaround;

  /**
   * Type filters
   * These filters are applied on an index.
   * Useful optimization if there is a lot of different type of events.
   */
  private List<String> eventTypes = List.of();

  /**
   * Dimension filters. As a sql filter string. If empty or null, no filter is applied.
   */
  private String sqlFilter = "";

  /**
   * Injected at runtime
   */
  private EventManager eventManager;

  public String getStartTimeLookback() {
    return startTimeLookback;
  }

  public EventFetcherSpec setStartTimeLookback(final String startTimeLookback) {
    this.startTimeLookback = startTimeLookback;
    return this;
  }

  public String getEndTimeLookback() {
    return endTimeLookback;
  }

  public EventFetcherSpec setEndTimeLookback(final String endTimeLookback) {
    this.endTimeLookback = endTimeLookback;
    return this;
  }

  public String getLookaround() {
    return lookaround;
  }

  public EventFetcherSpec setLookaround(final String lookaround) {
    this.lookaround = lookaround;
    return this;
  }

  public EventManager getEventManager() {
    return eventManager;
  }

  public EventFetcherSpec setEventManager(
      final EventManager eventManager) {
    this.eventManager = eventManager;
    return this;
  }

  public List<String> getEventTypes() {
    return eventTypes;
  }

  public EventFetcherSpec setEventTypes(
      final List<String> eventTypes) {
    this.eventTypes = eventTypes;
    return this;
  }

  public String getSqlFilter() {
    return sqlFilter;
  }

  public EventFetcherSpec setSqlFilter(final String sqlFilter) {
    this.sqlFilter = sqlFilter;
    return this;
  }
}
