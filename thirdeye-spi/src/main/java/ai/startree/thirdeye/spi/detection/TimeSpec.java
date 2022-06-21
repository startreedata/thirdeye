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
package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.util.SpiUtils.TimeFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.concurrent.TimeUnit;

public class TimeSpec {

  private static final TimeGranularity DEFAULT_TIME_GRANULARITY = new TimeGranularity(1,
      TimeUnit.DAYS);
  public static String SINCE_EPOCH_FORMAT = TimeFormat.EPOCH.toString();

  private String columnName;
  private TimeGranularity dataGranularity = DEFAULT_TIME_GRANULARITY;
  private String format = SINCE_EPOCH_FORMAT; //sinceEpoch or yyyyMMdd

  public TimeSpec() {
  }

  public TimeSpec(String columnName, TimeGranularity dataGranularity, String format) {
    this.columnName = columnName;
    this.dataGranularity = dataGranularity;
    this.format = format;
  }

  @JsonProperty
  public String getColumnName() {
    return columnName;
  }

  @JsonProperty
  public TimeGranularity getDataGranularity() {
    return dataGranularity;
  }

  @JsonProperty
  public String getFormat() {
    return format;
  }
}
