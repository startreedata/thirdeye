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
