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
package ai.startree.thirdeye.spi.detection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Deprecated // use a joda Period instead
public class TimeGranularity {

  private final int size;
  private final TimeUnit unit;

  public TimeGranularity() {
    this(1, TimeUnit.HOURS);
  }

  public TimeGranularity(int size, TimeUnit unit) {
    this.size = size;
    this.unit = unit;
  }

  /**
   * Copy constructor
   *
   * @param that to be copied
   */
  public TimeGranularity(TimeGranularity that) {
    this(that.getSize(), that.getUnit());
  }

  @JsonProperty
  public int getSize() {
    return size;
  }

  @JsonProperty
  public TimeUnit getUnit() {
    return unit;
  }

  /**
   * Returns the equivalent milliseconds of this time granularity.
   *
   * @return the equivalent milliseconds of this time granularity.
   */
  public long toMillis() {
    return toMillis(1);
  }

  /**
   * Returns the equivalent milliseconds of the specified number of this time granularity. Highly
   * suggested to use
   * toPeriod instead of this method for handling daylight saving time issue.
   *
   * @param number the specified number of this time granularity.
   * @return the equivalent milliseconds of the specified number of this time granularity.
   */
  public long toMillis(long number) {
    return unit.toMillis(number * size);
  }

  public Duration toDuration() {
    return Duration.ofMillis(toMillis());
  }

  public static TimeGranularity fromDuration(Duration d) {
    return new TimeGranularity(Math.toIntExact(d.toMillis()), TimeUnit.MILLISECONDS);
  }

  /**
   * Return the string representation of this time granularity, in which duration and unit are
   * separated by "-".
   */
  @Override
  public String toString() {
    return size + "-" + unit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, unit);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TimeGranularity)) {
      return false;
    }
    TimeGranularity other = (TimeGranularity) obj;
    return Objects.equals(other.size, this.size) && Objects.equals(other.unit, this.unit);
  }
}
