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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.calcite.filter.SqlFilterRunner;
import ai.startree.thirdeye.datalayer.calcite.object.adapter.EventToRelationAdapter;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public class EventManagerImpl extends AbstractManagerImpl<EventDTO> implements EventManager {

  private final SqlFilterRunner<EventDTO> sqlFilterRunner = new SqlFilterRunner<>(
      new EventToRelationAdapter());

  @Inject
  public EventManagerImpl(GenericPojoDao genericPojoDao) {
    super(EventDTO.class, genericPojoDao);
  }

  public List<EventDTO> findByEventType(String eventType) {
    Predicate predicate = Predicate.EQ("eventType", eventType);
    return findByPredicate(predicate);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRange(final long startTime, final long endTime) {
    Predicate predicate = Predicate
        .AND(Predicate.GT("endTime", startTime),
            Predicate.LT("startTime", endTime));
    return findByPredicate(predicate);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRange(final long startTime,
      final long endTime, @Nullable final List<@NonNull String> eventTypes) {
    if (eventTypes == null || eventTypes.isEmpty()) {
      return findEventsBetweenTimeRange(startTime, endTime);
    }
    final Predicate predicate = Predicate
        .AND(Predicate.IN("eventType", eventTypes.toArray(new String[0])),
            Predicate.GT("endTime", startTime),
            Predicate.LT("startTime", endTime));
    return findByPredicate(predicate);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRange(final long startTime, final long endTime,
      @Nullable final List<@NonNull String> eventTypes, @Nullable final String freeTextSqlFilter) {
    final List<EventDTO> events = findEventsBetweenTimeRange(startTime, endTime, eventTypes);

    return sqlFilterRunner.applyFilter(events, freeTextSqlFilter);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRangeByName(String eventType, String name, long start,
      long end) {
    Predicate predicate = Predicate
        .AND(Predicate.EQ("eventType", eventType), Predicate.EQ("name", name),
            Predicate.GT("endTime", start), Predicate.LT("startTime", end));
    return findByPredicate(predicate);
  }
}
