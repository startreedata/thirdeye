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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// todo cyril this class is not used anymore - delete?
public class SimpleDateFormatTimeConverter implements TimeConverter {

  private final SimpleDateFormat sdf;

  public SimpleDateFormatTimeConverter(String timeFormat) {
    sdf = new SimpleDateFormat(timeFormat);
  }

  @Override
  public long convert(final String timeValue) {
    try {
      return sdf.parse(timeValue).getTime();
    } catch (ParseException e) {
      throw new RuntimeException(
          "Unable to parse time value " + timeValue, e);
    }
  }

  @Override
  public String convertMillis(final long time) {
    return sdf.format(new Date(time));
  }
}
