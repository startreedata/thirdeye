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
package ai.startree.thirdeye.service.alert;

import static ai.startree.thirdeye.service.alert.AlertInsightsProvider.defaultChartTimeframe;
import static ai.startree.thirdeye.service.alert.AlertInsightsProvider.defaultCronFor;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AlertInsightsProviderTest {

  private final static long JANUARY_1_2022_2AM = 1641002400000L;
  private final static long JANUARY_2_2022_0AM = 1641081600000L;
  private final static long JANUARY_3_2022_0AM = 1641168000000L;
  private final static long JAN_1_2022_11PM = 1641078000000L;
  private final static long JULY_2_2021_0AM = 1625184000000L;
  private final static long JULY_1_2021_4AM = 1625112000000L;
  private final static long JANUARY_1_2019_OAM = 1546300800000L;
  private final static Period DAILY_GRANULARITY = Period.days(1);
  public static final DateTimeZone PARIS_TIMEZONE = DateTimeZone.forID("Europe/Paris");

  @Test
  public void testDefaultIntervalWithDailyGranularity() {
    final Interval datasetInterval = new Interval(JANUARY_1_2019_OAM, JANUARY_1_2022_2AM,
        DateTimeZone.UTC);
    final Interval res = AlertInsightsProvider.getDefaultChartInterval(datasetInterval,
        DAILY_GRANULARITY);

    // end of end bucket
    final DateTime expectedEnd = new DateTime(JANUARY_2_2022_0AM, DateTimeZone.UTC);
    // 6 months from start of end bucket
    final DateTime expectedStart = expectedEnd.minus(DAILY_GRANULARITY).minus(Period.months(6));
    final Interval expected = new Interval(expectedStart, expectedEnd);
    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testDefaultIntervalWithDailyGranularityWithParisTimezone() {
    final Interval datasetInterval = new Interval(JANUARY_1_2019_OAM, JANUARY_1_2022_2AM,
        PARIS_TIMEZONE);
    final Interval res = AlertInsightsProvider.getDefaultChartInterval(datasetInterval,
        DAILY_GRANULARITY);

    // end of end bucket
    final DateTime expectedEnd = new DateTime(JAN_1_2022_11PM, PARIS_TIMEZONE);
    // 6 months from start of end bucket
    final DateTime expectedStart = expectedEnd.minus(DAILY_GRANULARITY).minus(Period.months(6));
    final Interval expected = new Interval(expectedStart, expectedEnd);
    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testDefaultIntervalWithDailyGranularityWithStartOfDatasetBeforeDefaultStart() {
    final Interval datasetInterval = new Interval(JULY_1_2021_4AM, JANUARY_1_2022_2AM,
        DateTimeZone.UTC);
    final Interval res = AlertInsightsProvider.getDefaultChartInterval(datasetInterval,
        DAILY_GRANULARITY);

    final Interval expected = new Interval(JULY_2_2021_0AM, JANUARY_2_2022_0AM, DateTimeZone.UTC);
    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testDefaultChartTimeframe() {
    // test all possible output results
    assertThat(defaultChartTimeframe(Period.minutes(1))).isEqualTo(Period.days(2));
    assertThat(defaultChartTimeframe(Period.minutes(5))).isEqualTo(Period.days(7));
    assertThat(defaultChartTimeframe(Period.minutes(15))).isEqualTo(Period.days(14));
    assertThat(defaultChartTimeframe(Period.hours(1))).isEqualTo(Period.months(2));
    assertThat(defaultChartTimeframe(Period.days(1))).isEqualTo(Period.months(6));
    assertThat(defaultChartTimeframe(Period.days(14))).isEqualTo(Period.years(3));
    assertThat(defaultChartTimeframe(Period.weeks(15))).isEqualTo(Period.years(3));

    // test input contains variable length period units: month and year
    assertThat(defaultChartTimeframe(Period.months(2))).isEqualTo(Period.years(4));
    assertThat(defaultChartTimeframe(Period.years(1))).isEqualTo(Period.years(4));
  }

  @DataProvider
  public static Object[][] defaultCronCases() {
    final ISOChronology utc_tz = ISOChronology.getInstanceUTC();
    // 30 minute positive offset
    final ISOChronology india_tz = ISOChronology.getInstance(DateTimeZone.forID("Asia/Calcutta"));
    // positive offset
    final ISOChronology france_tz = ISOChronology.getInstance(DateTimeZone.forID("Europe/Paris"));
    // negative offset
    final ISOChronology pst_tz = ISOChronology.getInstance(DateTimeZone.forID("America/Los_Angeles"));
    // 30 minute negative offset
    final ISOChronology st_johns_tz = ISOChronology.getInstance(DateTimeZone.forID("America/St_Johns"));
    // 45 minute offset
    final ISOChronology aus_eucla_tz = ISOChronology.getInstance(DateTimeZone.forID("Australia/Eucla"));
    return new Object[][]{
        // granularity, timezone, completenessDelay, expectedCron
        {"PT1M", utc_tz, Period.millis(300), "1 * * * * ? *"},
        {"PT1M", utc_tz, Period.ZERO, "0 * * * * ? *"},
        {"PT1M", utc_tz, Period.seconds(25), "25 * * * * ? *"},
        {"PT1M", utc_tz, Period.minutes(10), "0 * * * * ? *"},
        {"PT1M", utc_tz, Period.seconds(10).withMinutes(2), "10 * * * * ? *"},

        {"PT5M", utc_tz, Period.ZERO, "0 0/5 * * * ? *"},
        {"PT5M", utc_tz, Period.hours(1), "0 0/5 * * * ? *"},
        {"PT5M", utc_tz, Period.seconds(25), "25 0/5 * * * ? *"},
        {"PT5M", utc_tz, Period.minutes(10), "0 0/5 * * * ? *"},
        {"PT5M", utc_tz, Period.minutes(8), "0 3/5 * * * ? *"},
        {"PT5M", utc_tz, Period.seconds(10).withMinutes(2), "10 2/5 * * * ? *"},

        {"PT10M", utc_tz, Period.ZERO, "0 0/10 * * * ? *"},
        {"PT10M", utc_tz, Period.hours(1), "0 0/10 * * * ? *"},
        {"PT10M", utc_tz, Period.seconds(25), "25 0/10 * * * ? *"},
        {"PT10M", utc_tz, Period.minutes(10), "0 0/10 * * * ? *"},
        {"PT10M", utc_tz, Period.minutes(12), "0 2/10 * * * ? *"},
        {"PT10M", utc_tz, Period.seconds(10).withMinutes(2), "10 2/10 * * * ? *"},

        {"PT15M", utc_tz, Period.ZERO, "0 0/15 * * * ? *"},
        {"PT15M", utc_tz, Period.hours(1), "0 0/15 * * * ? *"},
        {"PT15M", utc_tz, Period.seconds(25), "25 0/15 * * * ? *"},
        {"PT15M", utc_tz, Period.minutes(30), "0 0/15 * * * ? *"},
        {"PT15M", utc_tz, Period.minutes(21), "0 6/15 * * * ? *"},
        {"PT15M", utc_tz, Period.seconds(10).withMinutes(2), "10 2/15 * * * ? *"},

        {"PT30M", utc_tz, Period.ZERO, "0 0/30 * * * ? *"},
        {"PT30M", utc_tz, Period.hours(1), "0 0/30 * * * ? *"},
        {"PT30M", utc_tz, Period.seconds(25), "25 0/30 * * * ? *"},
        {"PT30M", utc_tz, Period.minutes(30), "0 0/30 * * * ? *"},
        {"PT30M", utc_tz, Period.minutes(47), "0 17/30 * * * ? *"},
        {"PT30M", utc_tz, Period.seconds(10).withMinutes(2), "10 2/30 * * * ? *"},

        // for france, pst and utc, crons should be the same
        {"PT1H", utc_tz, Period.ZERO, "0 0 * * * ? *"},
        {"PT1H", utc_tz, Period.days(1), "0 0 * * * ? *"},
        {"PT1H", utc_tz, Period.seconds(25), "25 0 * * * ? *"},
        {"PT1H", utc_tz, Period.minutes(30), "0 30 * * * ? *"},
        {"PT1H", utc_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 2 * * * ? *"},
        {"PT1H", france_tz, Period.ZERO, "0 0 * * * ? *"},
        {"PT1H", france_tz, Period.days(1), "0 0 * * * ? *"},
        {"PT1H", france_tz, Period.seconds(25), "25 0 * * * ? *"},
        {"PT1H", france_tz, Period.minutes(30), "0 30 * * * ? *"},
        {"PT1H", france_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 2 * * * ? *"},
        {"PT1H", pst_tz, Period.ZERO, "0 0 * * * ? *"},
        {"PT1H", pst_tz, Period.days(1), "0 0 * * * ? *"},
        {"PT1H", pst_tz, Period.seconds(25), "25 0 * * * ? *"},
        {"PT1H", pst_tz, Period.minutes(30), "0 30 * * * ? *"},
        {"PT1H", pst_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 2 * * * ? *"},
        // for india and st jones, crons should be offset by 30 minutes
        {"PT1H", india_tz, Period.ZERO, "0 30 * * * ? *"},
        {"PT1H", india_tz, Period.days(1), "0 30 * * * ? *"},
        {"PT1H", india_tz, Period.seconds(25), "25 30 * * * ? *"},
        {"PT1H", india_tz, Period.minutes(30), "0 0 * * * ? *"},
        {"PT1H", india_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 32 * * * ? *"},
        {"PT1H", st_johns_tz, Period.ZERO, "0 30 * * * ? *"},
        {"PT1H", st_johns_tz, Period.days(1), "0 30 * * * ? *"},
        {"PT1H", st_johns_tz, Period.seconds(25), "25 30 * * * ? *"},
        {"PT1H", st_johns_tz, Period.minutes(30), "0 0 * * * ? *"},
        {"PT1H", st_johns_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 32 * * * ? *"},
        // for australia eucla, crons should be offset by 45 minutes
        {"PT1H", aus_eucla_tz, Period.ZERO, "0 15 * * * ? *"},
        {"PT1H", aus_eucla_tz, Period.days(1), "0 15 * * * ? *"},
        {"PT1H", aus_eucla_tz, Period.seconds(25), "25 15 * * * ? *"},
        {"PT1H", aus_eucla_tz, Period.minutes(30), "0 45 * * * ? *"},
        {"PT1H", aus_eucla_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 17 * * * ? *"},

        {"P1D", utc_tz, Period.ZERO, "0 0 0 * * ? *"},
        {"P1D", utc_tz, Period.days(1), "0 0 0 * * ? *"},
        {"P1D", utc_tz, Period.seconds(25), "25 0 0 * * ? *"},
        {"P1D", utc_tz, Period.minutes(30), "0 30 0 * * ? *"},
        {"P1D", utc_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 2 3 * * ? *"},
        {"P1D", france_tz, Period.ZERO, "0 0 23 * * ? *"},
        {"P1D", france_tz, Period.days(1), "0 0 23 * * ? *"},
        {"P1D", france_tz, Period.seconds(25), "25 0 23 * * ? *"},
        {"P1D", france_tz, Period.minutes(30), "0 30 23 * * ? *"},
        {"P1D", france_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 2 2 * * ? *"},
        {"P1D", pst_tz, Period.ZERO, "0 0 8 * * ? *"},
        {"P1D", pst_tz, Period.days(1), "0 0 8 * * ? *"},
        {"P1D", pst_tz, Period.seconds(25), "25 0 8 * * ? *"},
        {"P1D", pst_tz, Period.minutes(30), "0 30 8 * * ? *"},
        {"P1D", pst_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 2 11 * * ? *"},
        {"P1D", india_tz, Period.ZERO, "0 30 18 * * ? *"},
        {"P1D", india_tz, Period.days(1), "0 30 18 * * ? *"},
        {"P1D", india_tz, Period.seconds(25), "25 30 18 * * ? *"},
        {"P1D", india_tz, Period.minutes(30), "0 0 19 * * ? *"},
        {"P1D", india_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 32 21 * * ? *"},
        {"P1D", st_johns_tz, Period.ZERO, "0 30 3 * * ? *"},
        {"P1D", st_johns_tz, Period.days(1), "0 30 3 * * ? *"},
        {"P1D", st_johns_tz, Period.seconds(25), "25 30 3 * * ? *"},
        {"P1D", st_johns_tz, Period.minutes(30), "0 0 4 * * ? *"},
        {"P1D", st_johns_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 32 6 * * ? *"},
        {"P1D", aus_eucla_tz, Period.ZERO, "0 15 15 * * ? *"},
        {"P1D", aus_eucla_tz, Period.days(1), "0 15 15 * * ? *"},
        {"P1D", aus_eucla_tz, Period.seconds(25), "25 15 15 * * ? *"},
        {"P1D", aus_eucla_tz, Period.minutes(30), "0 45 15 * * ? *"},
        {"P1D", aus_eucla_tz, Period.seconds(10).withMinutes(2).withHours(3), "10 17 18 * * ? *"},

        // negative completenessDelay cases
        {"PT1M", utc_tz, Period.millis(-300), "0 * * * * ? *"},
        {"PT1M", utc_tz, Period.seconds(-25), "35 * * * * ? *"},
        {"PT1M", utc_tz, Period.minutes(-10), "0 * * * * ? *"},
        {"PT1M", utc_tz, Period.seconds(-10).withMinutes(-2), "50 * * * * ? *"},
        
        {"PT5M", utc_tz, Period.hours(-1), "0 0/5 * * * ? *"},
        {"PT5M", utc_tz, Period.seconds(-25), "35 4/5 * * * ? *"},
        {"PT5M", utc_tz, Period.minutes(-10), "0 0/5 * * * ? *"},
        {"PT5M", utc_tz, Period.minutes(-8), "0 2/5 * * * ? *"},
        {"PT5M", utc_tz, Period.seconds(-10).withMinutes(-2), "50 2/5 * * * ? *"},
        
        {"PT10M", utc_tz, Period.hours(-1), "0 0/10 * * * ? *"},
        {"PT10M", utc_tz, Period.seconds(-25), "35 9/10 * * * ? *"},
        {"PT10M", utc_tz, Period.minutes(-10), "0 0/10 * * * ? *"},
        {"PT10M", utc_tz, Period.minutes(-12), "0 8/10 * * * ? *"},
        {"PT10M", utc_tz, Period.seconds(-10).withMinutes(-2), "50 7/10 * * * ? *"},
        
        {"PT15M", utc_tz, Period.hours(-1), "0 0/15 * * * ? *"},
        {"PT15M", utc_tz, Period.seconds(-25), "35 14/15 * * * ? *"},
        {"PT15M", utc_tz, Period.minutes(-30), "0 0/15 * * * ? *"},
        {"PT15M", utc_tz, Period.minutes(-21), "0 9/15 * * * ? *"},
        {"PT15M", utc_tz, Period.seconds(-10).withMinutes(-2), "50 12/15 * * * ? *"},

        {"PT30M", utc_tz, Period.hours(-1), "0 0/30 * * * ? *"},
        {"PT30M", utc_tz, Period.seconds(-25), "35 29/30 * * * ? *"},
        {"PT30M", utc_tz, Period.minutes(-30), "0 0/30 * * * ? *"},
        {"PT30M", utc_tz, Period.minutes(-47), "0 13/30 * * * ? *"},
        {"PT30M", utc_tz, Period.seconds(-10).withMinutes(-2), "50 27/30 * * * ? *"},

        // negative case - for france, pst and utc, crons should be the same
        {"PT1H", utc_tz, Period.days(-1), "0 0 * * * ? *"},
        {"PT1H", utc_tz, Period.seconds(-25), "35 59 * * * ? *"},
        {"PT1H", utc_tz, Period.minutes(-30), "0 30 * * * ? *"},
        {"PT1H", utc_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 57 * * * ? *"},
        {"PT1H", france_tz, Period.days(-1), "0 0 * * * ? *"},
        {"PT1H", france_tz, Period.seconds(-25), "35 59 * * * ? *"},
        {"PT1H", france_tz, Period.minutes(-30), "0 30 * * * ? *"},
        {"PT1H", france_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 57 * * * ? *"},
        {"PT1H", pst_tz, Period.days(-1), "0 0 * * * ? *"},
        {"PT1H", pst_tz, Period.seconds(-25), "35 59 * * * ? *"},
        {"PT1H", pst_tz, Period.minutes(-30), "0 30 * * * ? *"},
        {"PT1H", pst_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 57 * * * ? *"},
        // negative case - for india and st jones, crons should be offset by 30 minutes
        {"PT1H", india_tz, Period.days(-1), "0 30 * * * ? *"},
        {"PT1H", india_tz, Period.seconds(-25), "35 29 * * * ? *"},
        {"PT1H", india_tz, Period.minutes(-30), "0 0 * * * ? *"},
        {"PT1H", india_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 27 * * * ? *"},
        {"PT1H", st_johns_tz, Period.days(-1), "0 30 * * * ? *"},
        {"PT1H", st_johns_tz, Period.seconds(-25), "35 29 * * * ? *"},
        {"PT1H", st_johns_tz, Period.minutes(-30), "0 0 * * * ? *"},
        {"PT1H", st_johns_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 27 * * * ? *"},
        // negative case for - australia eucla, crons should be offset by 45 minutes
        {"PT1H", aus_eucla_tz, Period.days(-1), "0 15 * * * ? *"},
        {"PT1H", aus_eucla_tz, Period.seconds(-25), "35 14 * * * ? *"},
        {"PT1H", aus_eucla_tz, Period.minutes(-30), "0 45 * * * ? *"},
        {"PT1H", aus_eucla_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 12 * * * ? *"},

        {"P1D", utc_tz, Period.days(-1), "0 0 0 * * ? *"},
        {"P1D", utc_tz, Period.seconds(-25), "35 59 23 * * ? *"},
        {"P1D", utc_tz, Period.minutes(-30), "0 30 23 * * ? *"},
        {"P1D", utc_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 57 20 * * ? *"},
        {"P1D", france_tz, Period.days(-1), "0 0 23 * * ? *"},
        {"P1D", france_tz, Period.seconds(-25), "35 59 22 * * ? *"},
        {"P1D", france_tz, Period.minutes(-30), "0 30 22 * * ? *"},
        {"P1D", france_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 57 19 * * ? *"},
        {"P1D", pst_tz, Period.days(-1), "0 0 8 * * ? *"},
        {"P1D", pst_tz, Period.seconds(-25), "35 59 7 * * ? *"},
        {"P1D", pst_tz, Period.minutes(-30), "0 30 7 * * ? *"},
        {"P1D", pst_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 57 4 * * ? *"},
        {"P1D", india_tz, Period.days(-1), "0 30 18 * * ? *"},
        {"P1D", india_tz, Period.seconds(-25), "35 29 18 * * ? *"},
        {"P1D", india_tz, Period.minutes(-30), "0 0 18 * * ? *"},
        {"P1D", india_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 27 15 * * ? *"},
        {"P1D", st_johns_tz, Period.days(-1), "0 30 3 * * ? *"},
        {"P1D", st_johns_tz, Period.seconds(-25), "35 29 3 * * ? *"},
        {"P1D", st_johns_tz, Period.minutes(-30), "0 0 3 * * ? *"},
        {"P1D", st_johns_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 27 0 * * ? *"},
        {"P1D", aus_eucla_tz, Period.days(-1), "0 15 15 * * ? *"},
        {"P1D", aus_eucla_tz, Period.seconds(-25), "35 14 15 * * ? *"},
        {"P1D", aus_eucla_tz, Period.minutes(-30), "0 45 14 * * ? *"},
        {"P1D", aus_eucla_tz, Period.seconds(-10).withMinutes(-2).withHours(-3), "50 12 12 * * ? *"},
    };
  }

  @Test(dataProvider = "defaultCronCases")
  public void testDefaultCronFor(final String granularity, final Chronology chronology,
      final Period completenessDelay, final String expectedCron) {
    assertThat(defaultCronFor(granularity, chronology, completenessDelay)).isEqualTo(expectedCron);
  }
}
