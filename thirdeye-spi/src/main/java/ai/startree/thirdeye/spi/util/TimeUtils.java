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
package ai.startree.thirdeye.spi.util;

import static ai.startree.thirdeye.spi.Constants.UTC_TIMEZONE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

public class TimeUtils {

  private final static LoadingCache<Set<String>, Boolean> equivalentTimezonesCache = CacheBuilder.newBuilder()
      // see computeTimezonesAreEquivalent
      .expireAfterWrite(7, TimeUnit.DAYS)
      .build(new CacheLoader<>() {
        @Override
        public Boolean load(@NonNull Set<String> key) {
          final Iterator<String> iterator = key.iterator();
          return computeTimezonesAreEquivalent(iterator.next(), iterator.next());
        }
      });

  public static boolean timezonesAreEquivalent(final @NonNull String tz1, final @NonNull String tz2) {
    if (tz1.equals(tz2)) {
      return true;
    }
    final Set<String> cacheKey = Set.of(tz1, tz2);
    return equivalentTimezonesCache.getUnchecked(cacheKey);
  }

  /**
   * Defining two timezones as equal is not straightforward, because one country could decide to
   * change its timezone offsets at any time. So two timezones can be equivalent at some
   * point in time but not equivalent at another point in time.
   * We still need this fuzzy equivalence because sometimes in ThirdEye we have to check if a
   * timezone matches another one.
   * We consider two timezones are equivalent if they have the same offset every hour for a year,
   * around the date of execution of the function.
   * The computation is pretty slow (a few milliseconds) so we cache the results.
   * Results are cached for 7 days, so we compute the timezone equivalence on (now+7days - 1 year, now + 7 days).
   * In effect, this means if there are two timezone tz1, tz2, such that tz1=tz2 currently, and
   * this equality is exploited by the user, then any change to one of the tz such that tz1!=tz2
   * should be anticipated in TE at least 7 days prior. In effect this may never happen in the life
   * of the product.
   * One example of the case above is one client interchanges Europe/Amsterdam and Europe/Paris
   * because they are equivalent.
   */
  private static boolean computeTimezonesAreEquivalent(final String tz1, final String tz2) {
    final DateTimeZone dtZone1 = DateTimeZone.forID(tz1);
    final DateTimeZone dtZone2 = DateTimeZone.forID(tz2);
    // to check if two timezones are equivalent - we look at every hour every year
    final DateTime currentTime = new DateTime(dtZone1);
    final DateTime startDate = currentTime.plusDays(7).minusYears(1);
    final DateTime endDate = currentTime.plusDays(7);
    DateTime checkDate = startDate;
    while (checkDate.isBefore(endDate)) {
      final int offset1 = dtZone1.getOffset(checkDate);
      final int offset2 = dtZone2.getOffset(checkDate);
      if (offset1 != offset2) {
        // Time zones have different offsets for this date --> timezones are not equivalent.
        return false;
      }
      checkDate = checkDate.plusHours(1);
    }
    return true;
  }

  public static @NonNull Period isoPeriod(@NonNull final String period) {
    return Period.parse(period, ISOPeriodFormat.standard());
  }

  public static Period isoPeriod(@Nullable final String period, final Period defaultIfNullOrEmpty) {
    if (Strings.isNullOrEmpty(period)) {
      return defaultIfNullOrEmpty;
    }
    return isoPeriod(period);
  }

  /**
   * See https://stackoverflow.com/questions/8933158/how-do-i-round-a-datetime-to-the-nearest-period
   * Floors correctly only if 1 Time unit is used in the Period.
   * Takes the DateTimeZone in account. See testFloorByPeriodWithCustomTimezone.
   */
  public static DateTime floorByPeriod(final DateTime dt, final Period period) {
    if (period.getYears() != 0) {
      return dt.yearOfEra().roundFloorCopy().minusYears(dt.getYearOfEra() % period.getYears());
    } else if (period.getMonths() != 0) {
      return dt.monthOfYear()
          .roundFloorCopy()
          .minusMonths((dt.getMonthOfYear() - 1) % period.getMonths());
    } else if (period.getWeeks() != 0) {
      return dt.weekOfWeekyear()
          .roundFloorCopy()
          .minusWeeks((dt.getWeekOfWeekyear() - 1) % period.getWeeks());
    } else if (period.getDays() != 0) {
      return pinotFloorByDay(dt, period);
    } else if (period.getHours() != 0) {
      return dt.hourOfDay().roundFloorCopy().minusHours(dt.getHourOfDay() % period.getHours());
    } else if (period.getMinutes() != 0) {
      return dt.minuteOfHour()
          .roundFloorCopy()
          .minusMinutes(dt.getMinuteOfHour() % period.getMinutes());
    } else if (period.getSeconds() != 0) {
      return dt.secondOfMinute()
          .roundFloorCopy()
          .minusSeconds(dt.getSecondOfMinute() % period.getSeconds());
    }
    return dt.millisOfSecond()
        .roundCeilingCopy()
        .minusMillis(dt.getMillisOfSecond() % period.getMillis());
  }

  /**
   * Context: datetimeconvert function in Pinot does not implement weekly bucketing.
   * So users use P7D instead, but P7D in datetimeconvert rounds from Thursday to Thursday, by
   * epoch.
   *
   * Moreover, datetimeconvert does not implement timezone.
   * So datetimetrunc may be used when a custom timezone is used.
   *
   * The correct way to solve this is to have Pinot implement timezone and proper weekly bucketing
   * in datetimeconvert.
   */
  private static DateTime pinotFloorByDay(final DateTime dt, final Period period) {
    if (period.getDays() > 1 && timezonesAreEquivalent(UTC_TIMEZONE, dt.getChronology().getZone().getID())) {
      // assumes datetimeconverter was used in Pinot query. groups from thursday to thursday by doing direct operation on the millis
      // see BaseDateTimeTransformer.transformToOutputGranularity
      final long epochRounded =
          (dt.getMillis() / period.toStandardDuration().getMillis()) * period.toStandardDuration()
              .getMillis();
      return new DateTime(epochRounded, dt.getChronology());
    } else {
      // if P1D --> no issue. if timezone is not UTC: assumes datetrunc was used - operate on dayOfMonth
      // note: this logic returns buckets of variable length, which is most-likely an issue
      return dt.dayOfMonth()
          .roundFloorCopy()
          .minusDays((dt.getDayOfMonth() - 1) % period.getDays());
    }
  }

  /**
   * Returns the smallest datetime that is of the given period, and that is >=minTimeConstraint.
   * eg: minTimeConstraint is >= Thursday 3 am - period is 1 DAY --> returns Friday.
   * eg: inclusion: minTimeConstraint is >= Thursday 0 am - period is 1 DAY --> returns Thursday.
   */
  @VisibleForTesting
  public static DateTime getSmallestDatetime(DateTime minTimeConstraint, Period timePeriod) {
    DateTime dateTimeFloored = TimeUtils.floorByPeriod(minTimeConstraint, timePeriod.toPeriod());
    if (minTimeConstraint.equals(dateTimeFloored)) {
      return dateTimeFloored;
    }
    // dateTimeConstraint bigger than floored --> add 1 period
    //fixme cyril asap is this even correct? depends on where comes from te minTime not sure of plus - constraint is on the colum, not the aggregation not sure .plus() is required
    return dateTimeFloored.plus(timePeriod);
  }

  /**
   * Returns the biggest datetime that is of the given period, and that is <maxTimeConstraint.
   * eg: maxTimeConstraint is < Monday 4 am - period is 1 DAY --> returns Monday.
   * eg: exclusion: maxTimeConstraint is < Monday 0 am - period is 1 DAY --> returns Sunday.
   */
  @VisibleForTesting
  public static DateTime getBiggestDatetime(final DateTime maxTimeConstraint, final Period timePeriod) {
    final DateTime dateTimeFloored = TimeUtils.floorByPeriod(maxTimeConstraint, timePeriod.toPeriod());

    if (maxTimeConstraint.equals(dateTimeFloored)) {
      // dateTimeConstraint equals floor --> should be excluded
      return dateTimeFloored.minus(timePeriod);
    }
    return dateTimeFloored;
  }
}
