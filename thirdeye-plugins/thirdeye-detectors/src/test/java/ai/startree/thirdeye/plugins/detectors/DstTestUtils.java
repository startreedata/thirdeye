/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.detectors;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.Test;

/**
 * Provide DST data edge cases. Detectors should not raise an exception on these inputs.
 * Detectors should work correctly with: 
 * dataframe={@link DstTestUtils#FORWARD_CLOCK_DATA_INPUT, detectionInterval={@link DstTestUtils#FORWARD_CLOK_DETECTION_INTERVAL}
 * dataframe={@link DstTestUtils#BACKWARD_CLOCK_DATA_INPUT, detectionInterval={@link DstTestUtils#BACKWARD_CLOK_DETECTION_INTERVAL}
 * */
public class DstTestUtils {

  public static final DataFrame FORWARD_CLOCK_DATA_INPUT = new DataFrame()
      .addSeries(Constants.COL_TIME,
          estTime(2023, 3, 11, 23, 0, 0),
          estTime(2023, 3, 11, 23, 15, 0),
          estTime(2023, 3, 11, 23, 30, 0),
          estTime(2023, 3, 11, 23, 45, 0),
          estTime(2023, 3, 12, 0, 0, 0),
          estTime(2023, 3, 12, 0, 15, 0),
          estTime(2023, 3, 12, 0, 30, 0),
          estTime(2023, 3, 12, 0, 45, 0),
          estTime(2023, 3, 12, 1, 0, 0),
          estTime(2023, 3, 12, 1, 15, 0),
          estTime(2023, 3, 12, 1, 30, 0),
          estTime(2023, 3, 12, 1, 45, 0),
          // jump from 2:00 to 3:00 here
          estTime(2023, 3, 12, 3, 0, 0),
          estTime(2023, 3, 12, 3, 15, 0),
          estTime(2023, 3, 12, 3, 30, 0),
          estTime(2023, 3, 12, 3, 45, 0),
          estTime(2023, 3, 12, 4, 0, 0)
      )
      .addSeries(Constants.COL_VALUE,
          1.1, 2.2, 3.3, 4.4,
          1., 2., 3., 4.,
          1.2, 2.3, 3.4, 4.5,
          1.1, 2.2, 3.1, 4.3,
          1.)
      .sortedBy(Constants.COL_TIME);

  public final static Interval FORWARD_CLOK_DETECTION_INTERVAL = new Interval(
      estTime(2023, 3, 12, 3, 0, 0),
      estTime(2023, 3, 12, 4, 15, 0),
      DateTimeZone.forID("America/New_York"));

  private static final  Period PT1M = Period.minutes(1);
  public static final Period PT15M = Period.minutes(15);
  private static final DateTime BACKWARD_DST_CHANGE = new DateTime(2023, 11, 5, 1, 59, 0,
      DateTimeZone.forID("America/New_York")).plus(PT1M);
  public static final DataFrame BACKWARD_CLOCK_DATA_INPUT =  new DataFrame()
      .addSeries(Constants.COL_TIME,
          estTime(2023, 11, 4, 23, 0, 0),
          estTime(2023, 11, 4, 23, 15, 0),
          estTime(2023, 11, 4, 23, 30, 0),
          estTime(2023, 11, 4, 23, 45, 0),
          estTime(2023, 11, 5, 0, 0, 0),
          estTime(2023, 11, 5, 0, 15, 0),
          estTime(2023, 11, 5, 0, 30, 0),
          estTime(2023, 11, 5, 0, 45, 0),
          estTime(2023, 11, 5, 1, 0, 0),
          estTime(2023, 11, 5, 1, 15, 0),
          estTime(2023, 11, 5, 1, 30, 0),
          estTime(2023, 11, 5, 1, 45, 0),
          // jump from 1:59-04:00 to 1:00-05:00 here
          BACKWARD_DST_CHANGE.getMillis(),
          BACKWARD_DST_CHANGE.plus(PT15M).getMillis(),
          BACKWARD_DST_CHANGE.plus(PT15M.multipliedBy(2)).getMillis(),
          BACKWARD_DST_CHANGE.plus(PT15M.multipliedBy(3)).getMillis(),
          BACKWARD_DST_CHANGE.plus(PT15M.multipliedBy(4)).getMillis(),
          estTime(2023, 11, 5, 2, 15, 0),
          estTime(2023, 11, 5, 2, 30, 0),
          estTime(2023, 11, 5, 2, 45, 0),
          estTime(2023, 11, 5, 3, 0, 0)
      )
      .addSeries(Constants.COL_VALUE,
          1.1, 2.2, 3.3, 4.4,
          1., 2., 3., 4.,
          1.2, 2.3, 3.4, 4.5,
          1.1, 2.2, 3.1, 4.3,
          1.11, 2.22, 3.11, 4.33,
          1.)
      .sortedBy(Constants.COL_TIME);

  public final static Interval BACKWARD_CLOK_DETECTION_INTERVAL = new Interval(
      BACKWARD_DST_CHANGE.getMillis(),
      estTime(2023, 11, 5, 3, 15, 0),
      DateTimeZone.forID("America/New_York"));

  private static long estTime(final int y, final int m, final int d, final int h, final int min,
      final int s) {
    final DateTimeZone estTimezone = DateTimeZone.forID("America/New_York");
    return new DateTime(y, m, d, h, min, s, estTimezone).getMillis();
  }

  @Test
  public void ensureDstDataInputsAreCorrect() {
    // ensure the static dataframes of this class, used by other test classes, are correct
    // physical time buckets should always be spaced by 15 minutes 
    final long[] forwardTimes = FORWARD_CLOCK_DATA_INPUT.getLongs(Constants.COL_TIME).values();
    for (int i = 1; i < forwardTimes.length; i++) {
      assertThat(forwardTimes[i] - forwardTimes[i - 1]).isEqualTo(15 * 60 * 1000);
    }
    final long[] backwardTimes = BACKWARD_CLOCK_DATA_INPUT.getLongs(Constants.COL_TIME).values();
    for (int i = 1; i < backwardTimes.length; i++) {
      assertThat(backwardTimes[i] - backwardTimes[i - 1]).isEqualTo(15 * 60 * 1000);
    }
  }
}
