/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EventManager extends AbstractManager<EventDTO> {

  List<EventDTO> findByEventType(String eventType);

  List<EventDTO> findEventsBetweenTimeRange(long startTime, long endTime);

  default List<EventDTO> findEventsBetweenTimeRange(long startTime, long endTime, @Nullable String eventType) {
    List<String> eventTypes = eventType != null ? List.of(eventType) : List.of();
    return findEventsBetweenTimeRange(startTime, endTime, eventTypes);
  }

  List<EventDTO> findEventsBetweenTimeRange(long startTime, long endTime, @Nullable final List<@NonNull String> eventTypes);

  List<EventDTO> findEventsBetweenTimeRange(final long startTime, final long endTime,
      @Nullable final List<@NonNull String> eventTypes, @Nullable final String freeTextSqlFilter);

  List<EventDTO> findEventsBetweenTimeRangeByName(String eventType, String name, long startTime,
      long endTime);
}
