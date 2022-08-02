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
package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.base.AbstractInterval;

public class IntervalUtils {

  /**
   * This method is designed to merge a list of intervals to a list of intervals with no overlap in
   * between
   *
   * @return a list of intervals with no overlap in between
   */
  public static List<Interval> mergeIntervals(List<Interval> intervals) {
    if (intervals == null || intervals.size() == 0) {
      return intervals;
    }
    // Sort Intervals
    intervals.sort(Comparator.comparing(AbstractInterval::getStart));

    // Merge intervals
    Stack<Interval> intervalStack = new Stack<>();
    intervalStack.push(intervals.get(0));

    for (int i = 1; i < intervals.size(); i++) {
      Interval top = intervalStack.peek();
      Interval target = intervals.get(i);

      if (top.overlap(target) == null && (top.getEnd() != target.getStart())) {
        intervalStack.push(target);
      } else if (!top.equals(target)) {
        Interval newTop = new Interval(
            Math.min(top.getStart().getMillis(), target.getStart().getMillis()),
            Math.max(top.getEnd().getMillis(), target.getEnd().getMillis()),
            DateTimeZone.UTC);
        intervalStack.pop();
        intervalStack.push(newTop);
      }
    }

    return intervalStack;
  }

  /**
   * Merge intervals for each dimension map
   */
  public static void mergeIntervals(Map<DimensionMap, List<Interval>> anomalyIntervals) {
    for (DimensionMap dimension : anomalyIntervals.keySet()) {
      anomalyIntervals.put(dimension, mergeIntervals(anomalyIntervals.get(dimension)));
    }
  }
}
