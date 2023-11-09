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

package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoader;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.common.collect.ImmutableMap;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventService extends CrudService<EventApi, EventDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("type", "eventType")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .build();
  private final HolidayEventsLoader holidayEventsLoader;
  private final AnomalyManager anomalyManager;

  @Inject
  public EventService(
      final EventManager eventManager,
      final HolidayEventsLoader holidayEventsLoader,
      final AnomalyManager anomalyManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, eventManager, API_TO_INDEX_FILTER_MAP);
    this.holidayEventsLoader = holidayEventsLoader;
    this.anomalyManager = anomalyManager;
  }

  @Override
  protected EventDTO createDto(final ThirdEyeServerPrincipal principal, final EventApi api) {
    return ApiBeanMapper.toEventDto(api);
  }

  @Override
  protected EventApi toApi(final EventDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @Override
  protected EventDTO toDto(final EventApi api) {
    return ApiBeanMapper.toEventDto(api);
  }

  public void loadHolidays(final long startTime, final long endTime) {
    holidayEventsLoader.loadHolidays(startTime, endTime);
  }

  public EventApi loadHolidays(long anomalyId) {
    final AnomalyDTO anomalyDto = ensureExists(anomalyManager.findById(
        anomalyId));
    final EventDTO eventDTO = new EventDTO()
        .setName("Anomaly " + anomalyId)
        .setEventType("ANOMALY")
        .setStartTime(anomalyDto.getStartTime())
        .setEndTime(anomalyDto.getEndTime());
    dtoManager.save(eventDTO);
    return toApi(eventDTO);
  }
}
