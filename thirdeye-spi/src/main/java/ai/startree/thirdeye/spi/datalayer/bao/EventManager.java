/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
