/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.joda.time.Interval;

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
    Collections.sort(intervals, new Comparator<Interval>() {
      @Override
      public int compare(Interval o1, Interval o2) {
        return o1.getStart().compareTo(o2.getStart());
      }
    });

    // Merge intervals
    Stack<Interval> intervalStack = new Stack<>();
    intervalStack.push(intervals.get(0));

    for (int i = 1; i < intervals.size(); i++) {
      Interval top = intervalStack.peek();
      Interval target = intervals.get(i);

      if (top.overlap(target) == null && (top.getEnd() != target.getStart())) {
        intervalStack.push(target);
      } else if (top.equals(target)) {
        continue;
      } else {
        Interval newTop = new Interval(
            Math.min(top.getStart().getMillis(), target.getStart().getMillis()),
            Math.max(top.getEnd().getMillis(), target.getEnd().getMillis()));
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

  /**
   * turns a list of intervals into a string.
   * Ex. [1-5, 6-6, 9-15] -> "{1-5, 6, 9-15}"
   *
   * @return string form of interval list
   */
  public static String getIntervalRangesAsString(List<Interval> intervals) {
    if (intervals.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder("{");

    for (Interval interval : intervals) {
      long start = interval.getStartMillis();
      long end = interval.getEndMillis();

      // we check using >= because times may not be 100% perfectly aligned due to boundary shifting misalignment.
      if (start >= end) {
        sb.append(start);
      } else {
        sb.append(start);
        sb.append("-");
        sb.append(end);
      }
      sb.append(", ");
    }

    // delete the last "," and " " characters.
    // StringBuilder start is inclusive, end is exclusive.
    sb.delete(sb.length() - 2, sb.length());
    sb.append("}");

    return sb.toString();
  }
}
