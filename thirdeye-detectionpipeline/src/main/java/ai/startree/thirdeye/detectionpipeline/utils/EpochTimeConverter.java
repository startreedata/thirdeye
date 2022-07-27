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
package ai.startree.thirdeye.detectionpipeline.utils;

import ai.startree.thirdeye.spi.detection.TimeConverter;
import java.util.concurrent.TimeUnit;

public class EpochTimeConverter implements TimeConverter {

  private final TimeUnit timeUnit;

  public EpochTimeConverter(String timeUnit) {
    this.timeUnit = TimeUnit.valueOf(timeUnit);
  }

  @Override
  public long convert(final String timeValue) {
    return timeUnit.toMillis(Long.parseLong(timeValue));
  }

  @Override
  public String convertMillis(final long time) {
    return String.valueOf(timeUnit.convert(time, TimeUnit.MILLISECONDS));
  }
}
