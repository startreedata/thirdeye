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
package ai.startree.thirdeye.datastructures;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/******************************************************************************
 *  Interval data type with long coordinates.
 ******************************************************************************/


public interface Interval1D extends Comparable<Interval1D> {
  long getMin();

  long getMax();

  // does this interval intersect that one?
  default boolean intersects(Interval1D that) {
    if (that.getMax() <= this.getMin()) return false;
    return that.getMin() < this.getMax();
  }

  // does this interval a intersect b?
  default boolean contains(int x) {
    return (this.getMin() <= x) && (x < this.getMax());
  }

  default int compareTo(Interval1D that) {
    if      (this.getMin() < that.getMin()) return -1;
    else if (this.getMin() > that.getMin()) return +1;
    else if (this.getMax() < that.getMax()) return -1;
    else if (this.getMax() > that.getMax()) return +1;
    else                          return  0;
  }

  default Interval toJoda(DateTimeZone timeZone) {
    return new Interval(getMin(), getMax(), timeZone);
  }

  static Interval1D fromJoda(Interval interval) {
    return new Interval1D() {
      @Override
      public long getMin() {
        return interval.getStartMillis();
      }

      @Override
      public long getMax() {
        return interval.getEndMillis();
      }
    };
  }

  static Interval1D of(final long min, final long max) {
    return new Interval1D() {
      @Override
      public long getMin() {
        return min;
      }

      @Override
      public long getMax() {
        return max;
      }
    };
  }

}


