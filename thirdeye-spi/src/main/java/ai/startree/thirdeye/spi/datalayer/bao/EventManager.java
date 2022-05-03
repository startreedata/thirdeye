/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.util.List;

public interface EventManager extends AbstractManager<EventDTO> {

  List<EventDTO> findByEventType(String eventType);

  List<EventDTO> findEventsBetweenTimeRangeWithType(String eventType, long startTime, long endTime);

  List<EventDTO> findEventsBetweenTimeRange(long startTime, long endTime);

  List<EventDTO> findEventsBetweenTimeRangeByName(String eventType, String name, long startTime,
      long endTime);
}
