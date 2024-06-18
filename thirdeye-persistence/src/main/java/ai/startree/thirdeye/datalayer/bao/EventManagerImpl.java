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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.calcite.filter.SqlFilterRunner;
import ai.startree.thirdeye.datalayer.calcite.object.adapter.EventToRelationAdapter;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EventManagerImpl extends AbstractManagerImpl<EventDTO> implements EventManager {

  private static final Logger LOG = LoggerFactory.getLogger(EventManagerImpl.class);
  
  private final SqlFilterRunner<EventDTO> sqlFilterRunner = new SqlFilterRunner<>(
      new EventToRelationAdapter());
  private final boolean shareEventsInUnsetNamespace;

  @Inject
  public EventManagerImpl(GenericPojoDao genericPojoDao) {
    super(EventDTO.class, genericPojoDao);

    shareEventsInUnsetNamespace = System.getenv("TE_SHARE_EVENTS_IN_UNSET_NAMESPACE") != null;
    if (shareEventsInUnsetNamespace) {
      LOG.warn("Events with a namespace not set are made available to all namespaces. " 
          + "This configuration is not recommended. Please migrate your events to dedicated namespaces and disable " 
          + "this feature by unsetting the environment variable TE_SHARE_EVENTS_IN_UNSET_NAMESPACE.");
    }
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRangeInNamespace(final long startTime,
      final long endTime,
      final @Nullable String namespace) {
    Predicate predicate = Predicate
        .AND(Predicate.GT("endTime", startTime),
            Predicate.LT("startTime", endTime));
    final List<EventDTO> events = findByPredicate(predicate);
    return filteredByNamespace(namespace, events);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRangeInNamespace(final long startTime,
      final long endTime,
      final @Nullable List<@NonNull String> eventTypes, final @Nullable String namespace) {
    if (eventTypes == null || eventTypes.isEmpty()) {
      return findEventsBetweenTimeRangeInNamespace(startTime, endTime, namespace);
    }
    final Predicate predicate = Predicate
        .AND(Predicate.IN("eventType", eventTypes.toArray(new String[0])),
            Predicate.GT("endTime", startTime),
            Predicate.LT("startTime", endTime));
    final List<EventDTO> events = findByPredicate(predicate);
    return filteredByNamespace(namespace, events);
  }

  @Override
  public List<EventDTO> findEventsBetweenTimeRangeInNamespace(final long startTime,
      final long endTime, final @Nullable List<@NonNull String> eventTypes,
      final @Nullable String freeTextSqlFilter, final @Nullable String namespace) {
    final List<EventDTO> events = findEventsBetweenTimeRangeInNamespace(startTime, endTime, eventTypes, namespace);
    return sqlFilterRunner.applyFilter(events, freeTextSqlFilter);
  }

  @NonNull
  private List<EventDTO> filteredByNamespace(final @Nullable String namespace,
      final List<EventDTO> events) {
    return events.stream()
        .filter(e -> Objects.equals(namespace, e.namespace()) || (shareEventsInUnsetNamespace && Objects.equals(null, e.namespace())))
        // return a mutable list - mutability is used by consumers
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
