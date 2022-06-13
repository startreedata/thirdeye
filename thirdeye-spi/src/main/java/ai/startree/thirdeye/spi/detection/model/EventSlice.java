/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.model;

import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Selector for events based on (optionally) start time, end time, and dimension filters.
 */
public class EventSlice {

  final long start;
  final long end;
  final Multimap<String, String> filters;

  public EventSlice() {
    start = -1;
    end = -1;
    filters = ArrayListMultimap.create();
  }

  public EventSlice(final long start, final long end, final Multimap<String, String> filters) {
    this.start = start;
    this.end = end;
    this.filters = filters;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  public Multimap<String, String> getFilters() {
    return filters;
  }

  public boolean match(final EventDTO event) {
    if (start >= 0 && event.getEndTime() <= start) {
      return false;
    }
    if (end >= 0 && event.getStartTime() >= end) {
      return false;
    }

    for (final String dimName : filters.keySet()) {
      if (event.getTargetDimensionMap().containsKey(dimName)) {
        boolean anyMatch = false;
        for (final String dimValue : event.getTargetDimensionMap().get(dimName)) {
          anyMatch |= filters.get(dimName).contains(dimValue);
        }
        if (!anyMatch) {
          return false;
        }
      }
    }

    return true;
  }
}
