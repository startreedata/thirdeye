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
package ai.startree.thirdeye.util;

import static ai.startree.thirdeye.spi.util.TimeUtils.floorByPeriod;
import static ai.startree.thirdeye.spi.util.TimeUtils.getBiggestDatetime;
import static ai.startree.thirdeye.spi.util.TimeUtils.getSmallestDatetime;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class TimeUtilsTest {

  private static final DateTimeFormatter DATE_PARSER = DateTimeFormat.forPattern(
      "yyyy-MM-dd HH:mm:ss.SSS z");

  @Test
  public void testFloorByPeriodIsIdempotent() {
    // test that floorByPeriod · floorByPeriod = floorByPeriod
    final DateTime input1 = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime input2 = DATE_PARSER.parseDateTime("2020-08-27 11:22:33.444 UTC");

    final List<DateTime> inputs = List.of(input1, input2);

    for (var input : inputs) {

      final List<Period> periodList = List.of(Period.years(1), Period.years(2), Period.months(1),
          Period.months(2), Period.days(1), Period.days(2), Period.days(7), Period.minutes(1),
          Period.minutes(2), Period.seconds(1), Period.seconds(2), Period.millis(1),
          Period.millis(2));

      for (Period p : periodList) {
        DateTime floored = floorByPeriod(input, p);
        assertThat(floorByPeriod(floored, p)).isEqualTo(floored);
      }
    }
  }

  @Test
  public void testFloorByPeriodRoundByYear() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-01-01 00:00:00.000 UTC");
    final Period oneYear = Period.years(1);
    final DateTime output = floorByPeriod(input, oneYear);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy2Years() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2020-01-01 00:00:00.000 UTC");
    final Period twoYears = Period.years(2);
    final DateTime output = floorByPeriod(input, twoYears);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundByMonth() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-01 00:00:00.000 UTC");
    final Period oneMonth = Period.months(1);
    final DateTime output = floorByPeriod(input, oneMonth);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy2Months() {
    // rounding by 2 months falls on odd numbered months: Jan, Mar, May, etc...
    final DateTime input = DATE_PARSER.parseDateTime("2021-10-01 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-9-01 00:00:00.000 UTC");
    final Period twoMonths = Period.months(2);
    final DateTime output = floorByPeriod(input, twoMonths);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundByWeek() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-24 11:22:33.444 UTC"); //wednesday
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 00:00:00.000 UTC"); //monday
    final Period oneWeek = Period.weeks(1);
    final DateTime output = floorByPeriod(input, oneWeek);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy2Weeks() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-12-1 11:22:33.444 UTC");  //wednesday december
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 00:00:00.000 UTC"); //monday nov
    final Period twoWeeks = Period.weeks(2);
    final DateTime output = floorByPeriod(input, twoWeeks);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundByDay() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 00:00:00.000 UTC");
    final Period oneDay = Period.days(1);
    final DateTime output = floorByPeriod(input, oneDay);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy2Days() {
    // rounding by 2 days falls on odd numbered days
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-21 00:00:00.000 UTC");
    final Period twoDays = Period.days(2);
    final DateTime output = floorByPeriod(input, twoDays);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy3DaysInUTC() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-03 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-02 00:00:00.000 UTC");
    final Period twoDays = Period.days(3);
    final DateTime output = floorByPeriod(input, twoDays);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy3DaysInNonUTC() {
    // rounding by 3 days for first bucket of month
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-03 11:22:33.444 PST");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-01 00:00:00.000 PST");
    final Period twoDays = Period.days(3);
    final DateTime output = floorByPeriod(input, twoDays);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundByHour() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:00:00.000 UTC");
    final Period oneHour = Period.hours(1);
    final DateTime output = floorByPeriod(input, oneHour);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy2Hours() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 10:00:00.000 UTC");
    final Period twoHours = Period.hours(2);
    final DateTime output = floorByPeriod(input, twoHours);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundByMinute() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:22:00.000 UTC");
    final Period oneMinute = Period.minutes(1);
    final DateTime output = floorByPeriod(input, oneMinute);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy15Minutes() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:15:00.000 UTC");
    final Period fifteenMinutes = Period.minutes(15);
    final DateTime output = floorByPeriod(input, fifteenMinutes);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBySecond() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.000 UTC");
    final Period oneSecond = Period.seconds(1);
    final DateTime output = floorByPeriod(input, oneSecond);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy30Seconds() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:22:30.000 UTC");
    final Period thirtySeconds = Period.seconds(30);
    final DateTime output = floorByPeriod(input, thirtySeconds);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundByMilli() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = input;
    final Period oneMilli = Period.millis(1);
    final DateTime output = floorByPeriod(input, oneMilli);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodRoundBy100Milli() {
    final DateTime input = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.444 UTC");
    final DateTime expected = DATE_PARSER.parseDateTime("2021-11-22 11:22:33.400 UTC");
    final Period hundredMillis = Period.millis(100);
    final DateTime output = floorByPeriod(input, hundredMillis);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetFirstIndexValueForConstraintEqualToFloor() {
    final long inputMinTimeConstraint = 1634860800000L; //2021-10-22 00:00:00.000 UTC
    final Period inputPeriod = Period.days(1);
    final DateTime expected = DATE_PARSER.parseDateTime("2021-10-22 00:00:00.000 UTC");
    final DateTime output = getSmallestDatetime(
        new DateTime(inputMinTimeConstraint, DateTimeZone.UTC), inputPeriod);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetFirstIndexValueForConstraintBiggerThanFloor() {
    // firstIndex must be bigger than time constraint and respect the period
    final long inputMinTimeConstraint = 1634860800001L; //2021-10-22 00:00:00.001 UTC
    final Period inputPeriod = Period.days(1);
    final DateTime expected = DATE_PARSER.parseDateTime("2021-10-23 00:00:00.000 UTC");
    final DateTime output = getSmallestDatetime(
        new DateTime(inputMinTimeConstraint, DateTimeZone.UTC),
        inputPeriod);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetLastIndexValueForConstraintEqualToFloor() {
    // lastIndex must be strictly smaller than time constraint and respect the period
    final long inputMinTimeConstraint = 1634860800000L; //2021-10-22 00:00:00.000 UTC
    final Period inputPeriod = Period.days(1);
    final DateTime expected = DATE_PARSER.parseDateTime("2021-10-21 00:00:00.000 UTC");
    final DateTime output = getBiggestDatetime(
        new DateTime(inputMinTimeConstraint, DateTimeZone.UTC), inputPeriod);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testGetLastIndexValueForConstraintBiggerThanFloor() {
    // firstIndex must be bigger than time constraint and respect period
    final long inputMinTimeConstraint = 1634860800001L; //2021-10-22 00:00:00.001 UTC
    final Period inputPeriod = Period.days(1);
    final DateTime expected = DATE_PARSER.parseDateTime("2021-10-22 00:00:00.000 UTC");
    final DateTime output = getBiggestDatetime(
        new DateTime(inputMinTimeConstraint, DateTimeZone.UTC), inputPeriod);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testFloorByPeriodWithCustomTimezone() {
    // 23H30 utc --> means 1h30 CEST. Flooring by day should not give the same result.
    final long APRIL_1_2022_23H30 = 1648855800000L;
    final DateTime dtUTC = new DateTime(APRIL_1_2022_23H30, DateTimeZone.forID("UTC"));
    final long flooredUTC = floorByPeriod(dtUTC, Period.days(1)).getMillis();
    final long expectedUTC = 1648771200000L; //APRIL_1_2022_00H00 UTC
    assertThat(flooredUTC).isEqualTo(expectedUTC);

    final DateTime dtCEST = new DateTime(APRIL_1_2022_23H30, DateTimeZone.forID("Europe/Amsterdam"));
    final long flooredCEST = floorByPeriod(dtCEST, Period.days(1)).getMillis();
    final long expectedCEST = 1648850400000L; //APRIL_1_2022_22H00 UTC = APRIL_2_2022_00H00 CEST
    assertThat(flooredCEST).isEqualTo(expectedCEST);
  }
}
