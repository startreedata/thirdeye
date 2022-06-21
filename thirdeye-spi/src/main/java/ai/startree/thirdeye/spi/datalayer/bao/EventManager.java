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
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface EventManager extends AbstractManager<EventDTO> {

  List<EventDTO> findByEventType(String eventType);

  List<EventDTO> findEventsBetweenTimeRange(long startTime, long endTime);

  List<EventDTO> findEventsBetweenTimeRange(long startTime, long endTime, @Nullable String eventType);

  List<EventDTO> findEventsBetweenTimeRange(final long startTime, final long endTime,
      @Nullable final String eventType, @Nullable final Map<String, Set<String>> dimensionFilters);

  List<EventDTO> findEventsBetweenTimeRangeByName(String eventType, String name, long startTime,
      long endTime);
}
