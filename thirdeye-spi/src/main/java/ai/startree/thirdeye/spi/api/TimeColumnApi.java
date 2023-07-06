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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.Duration;

@JsonInclude(Include.NON_NULL)
public class TimeColumnApi {

  private String name;
  /**
   * Exists because format is an incomplete definition of the time format.
   * Eg for EPOCH_MILLIS, format=EPOCH and Duration=1ms.
   * Do not confuse with granularity.
   * TODO cyril refactor to simplify TimeSpec/TimeGranularity
   */
  private Duration interval;
  private String format;
  private String timezone;
  /**
   * Granularity of the measurements in ISO-8601 format.
   * For instance, a time column can be in format epoch millis, but only have value corresponding to
   * buckets of 1 minute.
   * In this case, granularity would be 1-minute = PT1M.
   */
  private String granularity;

  public String getName() {
    return name;
  }

  public TimeColumnApi setName(final String name) {
    this.name = name;
    return this;
  }

  public Duration getInterval() {
    return interval;
  }

  public TimeColumnApi setInterval(final Duration interval) {
    this.interval = interval;
    return this;
  }

  public String getFormat() {
    return format;
  }

  public TimeColumnApi setFormat(final String format) {
    this.format = format;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public TimeColumnApi setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }

  public String getGranularity() {
    return granularity;
  }

  public TimeColumnApi setGranularity(final String granularity) {
    this.granularity = granularity;
    return this;
  }
}
