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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.alert.AlertInsightsProvider.defaultChartTimeframe;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.Test;

public class AlertInsightsProviderTest {

  private final static long JANUARY_1_2022_2AM = 1641002400000L;
  private final static long JANUARY_1_2022_0AM = 1640995200000L;
  private final static long DECEMBER_31_2022_11PM = 1640991600000L;
  private final static long JULY_2_2021_0AM = 1625184000000L;
  private final static long JULY_1_2021_4AM = 1625112000000L;
  private final static long JANUARY_1_2021_0AM = 1609459200000L;
  private final static long DECEMBER_31_2020_11PM = 1609455600000L;
  private final static long JANUARY_1_2019_OAM = 1546300800000L;
  private final static Period DAILY_GRANULARITY = Period.days(1);
  public static final DateTimeZone PARIS_TIMEZONE = DateTimeZone.forID("Europe/Paris");

  @Test
  public void testDefaultIntervalWithDailyGranularity() {
    final Interval datasetInterval = new Interval(JANUARY_1_2019_OAM,
        JANUARY_1_2022_2AM,
        DateTimeZone.UTC);
    final Interval res = AlertInsightsProvider.getDefaultChartInterval(datasetInterval,
        DAILY_GRANULARITY);

    final Interval expected = new Interval(JANUARY_1_2021_0AM,
        JANUARY_1_2022_0AM,
        DateTimeZone.UTC);
    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testDefaultIntervalWithDailyGranularityWithParisTimezone() {
    final Interval datasetInterval = new Interval(JANUARY_1_2019_OAM,
        JANUARY_1_2022_2AM,
        PARIS_TIMEZONE);
    final Interval res = AlertInsightsProvider.getDefaultChartInterval(datasetInterval,
        DAILY_GRANULARITY);

    final Interval expected = new Interval(DECEMBER_31_2020_11PM,
        DECEMBER_31_2022_11PM,
        PARIS_TIMEZONE);
    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testDefaultIntervalWithDailyGranularityWithStartOfDatasetBeforeDefaultStart() {
    final Interval datasetInterval = new Interval(JULY_1_2021_4AM,
        JANUARY_1_2022_2AM,
        DateTimeZone.UTC);
    final Interval res = AlertInsightsProvider.getDefaultChartInterval(datasetInterval,
        DAILY_GRANULARITY);

    final Interval expected = new Interval(JULY_2_2021_0AM, JANUARY_1_2022_0AM, DateTimeZone.UTC);
    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testDefaultChartTimeframe() {
    // test all possible output results
    assertThat(defaultChartTimeframe(Period.minutes(15))).isEqualTo(Period.months(1));
    assertThat(defaultChartTimeframe(Period.hours(1))).isEqualTo(Period.months(6));
    assertThat(defaultChartTimeframe(Period.days(1))).isEqualTo(Period.years(1));
    assertThat(defaultChartTimeframe(Period.days(14))).isEqualTo(Period.years(2));
    assertThat(defaultChartTimeframe(Period.weeks(15))).isEqualTo(Period.years(4));

    // test input contains variable length period units: month and year
    assertThat(defaultChartTimeframe(Period.months(2))).isEqualTo(Period.years(4));
    assertThat(defaultChartTimeframe(Period.years(1))).isEqualTo(Period.years(4));
  }
}
