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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.spi.Constants.UTC_LIKE_TIMEZONES;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import com.google.common.annotations.VisibleForTesting;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotSqlExpressionBuilder implements SqlExpressionBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(PinotSqlExpressionBuilder.class);

  public static final Pattern SIMPLE_DATE_FORMAT_PATTERN = Pattern.compile(
      "^([0-9]:[A-Z]+:)?SIMPLE_DATE_FORMAT:");
  private static final Map<Period, String> DATE_TRUNC_COMPATIBLE_PERIOD_TO_DATE_TRUNC_STRING = Map.of(
      Period.years(1), "year",
      Period.months(1), "month",
      Period.weeks(1), "week",
      Period.days(1), "day",
      Period.hours(1), "hour",
      Period.minutes(1), "minute",
      Period.seconds(1), "second",
      Period.millis(1), "millisecond"
  );

  private static final List<Period> DATE_TRUNC_COMPATIBLE_PERIODS = List.copyOf(
      DATE_TRUNC_COMPATIBLE_PERIOD_TO_DATE_TRUNC_STRING.keySet());

  private static final String PERCENTILE_TDIGEST_PREFIX = "PERCENTILETDigest";

  public static final long SECOND_MILLIS = 1000; // number of milliseconds in a second
  public static final long MINUTE_MILLIS =
      60 * SECOND_MILLIS; // number of milliseconds in a minute
  public static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
  public static final long DAY_MILLIS = 24 * HOUR_MILLIS;
  public static final String STRING_LITERAL_QUOTE = "'";
  public static final String ESCAPED_STRING_LITERAL_QUOTE = "''";

  private static String escapeLiteralQuote(String s) {
    return s.replace(STRING_LITERAL_QUOTE, ESCAPED_STRING_LITERAL_QUOTE);
  }

  @Override
  public String getTimeFilterExpression(final String timeColumn, final Interval filterInterval,
      @Nullable final String timeColumnFormat) {
    final TimeFormat timeFormat = TimeFormat.of(timeColumnFormat);

    return timeColumn + " >= " + timeFormat.timeFormatter.apply(filterInterval.getStart()) + " AND "
        + timeColumn + " < " + timeFormat.timeFormatter.apply(filterInterval.getEnd());
  }

  @Override
  public String getTimeGroupExpression(String timeColumn, @Nullable String timeColumnFormat,
      final Period granularity, @Nullable final String timezone) {
    final TimeFormat timeFormat = TimeFormat.of(timeColumnFormat);
    if (timezone == null || UTC_LIKE_TIMEZONES.contains(timezone)) {
      return String.format(" DATETIMECONVERT(%s, '%s', '1:MILLISECONDS:EPOCH', '%s') ",
          timeColumn,
          escapeLiteralQuote(timeFormat.dateTimeConvertString),
          periodToDateTimeConvertFormat(granularity)
      );
    }

    if (timeFormat.isEpochFormat && DATE_TRUNC_COMPATIBLE_PERIODS.contains(granularity)) {
      // optimized expression for client use case - can be removed once https://github.com/apache/pinot/issues/8581 is closed
      return String.format(
          " DATETRUNC('%s', %s, '%s', '%s', 'MILLISECONDS') ",
          DATE_TRUNC_COMPATIBLE_PERIOD_TO_DATE_TRUNC_STRING.get(granularity),
          timeColumn,
          timeFormat.dateTruncString,
          timezone
      );
    }

    // workaround to bucket with a custom timezone - see https://github.com/apache/pinot/issues/8581
    return String.format(
        "FromDateTime(DATETIMECONVERT(%s, '%s', '1:DAYS:SIMPLE_DATE_FORMAT:yyyy-MM-dd HH:mm:ss.SSSZ tz(%s)', '%s'), 'yyyy-MM-dd HH:mm:ss.SSSZ') ",
        timeColumn,
        escapeLiteralQuote(timeFormat.dateTimeConvertString),
        timezone,
        periodToDateTimeConvertFormat(granularity));
  }

  @NonNull
  @VisibleForTesting
  protected static String removeSimpleDateFormatPrefix(final String timeColumnFormat) {
    // remove (1:DAYS:)SIMPLE_DATE_FORMAT:
    return SIMPLE_DATE_FORMAT_PATTERN.matcher(timeColumnFormat).replaceFirst("");
  }

  private String periodToDateTimeConvertFormat(final Period period) {
    // see https://docs.pinot.apache.org/configuration-reference/functions/datetimeconvert
    if (period.getYears() > 0) {
      throw new RuntimeException(String.format(
          "Pinot datasource cannot round to yearly granularity: %s",
          period));
    } else if (period.getMonths() > 0) {
      throw new RuntimeException(String.format(
          "Pinot datasource cannot round to monthly granularity: %s",
          period));
    } else if (period.getWeeks() > 0) {
      throw new RuntimeException(String.format(
          "Pinot datasource cannot round to weekly granularity: %s",
          period));
    } else if (period.getDays() > 0) {
      return String.format("%s:DAYS", period.getDays());
    } else if (period.getHours() > 0) {
      return String.format("%s:HOURS", period.getHours());
    } else if (period.getMinutes() > 0) {
      return String.format("%s:MINUTES", period.getMinutes());
    } else if (period.getSeconds() > 0) {
      return String.format("%s:SECONDS", period.getSeconds());
    } else if (period.getMillis() > 0) {
      return String.format("%s:MILLISECONDS", period.getMillis());
    }
    throw new RuntimeException(String.format("Could not translate Period to Pinot granularity: %s",
        period));
  }

  @Override
  public boolean needsCustomDialect(final MetricAggFunction metricAggFunction) {
    return SqlExpressionBuilder.super.needsCustomDialect(metricAggFunction);
  }

  @Override
  public String getCustomDialectSql(final MetricAggFunction metricAggFunction,
      final List<String> operands,
      final String quantifier) {
    switch (metricAggFunction) {
      case PCT50:
      case PCT90:
      case PCT95:
      case PCT99:
        String aggFunctionString = metricAggFunction.name();
        int percentile = Integer.parseInt(aggFunctionString.substring(3));
        checkArgument(operands.size() == 1,
            "Incorrect number of operands for percentile sql generation. Expected: 1. Got: %s",
            operands.size());
        return PERCENTILE_TDIGEST_PREFIX + "(" + operands.get(0) + "," + percentile + ")";
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public @Nullable Period granularityOfTimeFormat(final String timeFormat) {
    final TimeFormat format = TimeFormat.of(timeFormat);
    return format.exactGranularity;
  }

  /**
   * Class that allows to match multiple user facing time format strings to a given time format.
   * Can return the timeformat for different Pinot functions.
   */
  private static class TimeFormat {

    // a concurrent map is necessary because Map.computeIfAbsent used below could raise ConcurrentModificationException
    private static final ConcurrentMap<String, TimeFormat> cache = new ConcurrentHashMap<>();

    private final String dateTimeConvertString;
    private final String dateTruncString;
    private final boolean isEpochFormat;
    private final Function<DateTime, String> timeFormatter;
    // set to null for epoch formats because nothing in Pinot ensures a LONG epoch time column respects a granularity
    private final @Nullable Period exactGranularity;

    static TimeFormat of(final @Nullable String userFacingTimeColumnFormat) {
      final String nullSafeFormat = optional(userFacingTimeColumnFormat).orElse("EPOCH_MILLIS");
      return cache.computeIfAbsent(nullSafeFormat, TimeFormat::new);
    }

    private TimeFormat(final @NonNull String userFacingTimeColumnFormat) {
      switch (userFacingTimeColumnFormat) {
        case "EPOCH_NANOS":
        case "1:NANOSECONDS:EPOCH":
          dateTimeConvertString = "1:NANOSECONDS:EPOCH";
          dateTruncString = "NANOSECONDS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() * 1_000_000);
          exactGranularity = null;
          break;
        case "EPOCH_MICROS":
        case "1:MICROSECONDS:EPOCH":
          dateTimeConvertString = "1:MICROSECONDS:EPOCH";
          dateTruncString = "MICROSECONDS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() * 1000);
          exactGranularity = null;
          break;
        case "EPOCH_MILLIS":
        case "1:MILLISECONDS:EPOCH":
          dateTimeConvertString = "1:MILLISECONDS:EPOCH";
          dateTruncString = "MILLISECONDS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis());
          exactGranularity = null;
          break;
        case "EPOCH":
        case "1:SECONDS:EPOCH":
          dateTimeConvertString = "1:SECONDS:EPOCH";
          dateTruncString = "SECONDS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() / SECOND_MILLIS);
          exactGranularity = null;
          break;
        case "EPOCH_MINUTES":
        case "1:MINUTES:EPOCH":
          dateTimeConvertString = "1:MINUTES:EPOCH";
          dateTruncString = "MINUTES";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() / MINUTE_MILLIS);
          exactGranularity = null;
          break;
        case "EPOCH_HOURS":
        case "1:HOURS:EPOCH":
          dateTimeConvertString = "1:HOURS:EPOCH";
          dateTruncString = "HOURS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() / HOUR_MILLIS);
          exactGranularity = null;
          break;
        case "EPOCH_DAYS":
        case "1:DAYS:EPOCH":
          dateTimeConvertString = "1:DAYS:EPOCH";
          dateTruncString = "DAYS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() / DAY_MILLIS);
          exactGranularity = null;
          break;
        default:
          // assume simple date format
          final String cleanSimpleDateFormat = removeSimpleDateFormatPrefix(
              userFacingTimeColumnFormat);
          // fail if invalid format - note: this is slow because this instantiate calendars - maybe extract
          new SimpleDateFormat(cleanSimpleDateFormat);
          dateTimeConvertString = "1:DAYS:SIMPLE_DATE_FORMAT:" + cleanSimpleDateFormat;
          dateTruncString = null;
          isEpochFormat = false;
          timeFormatter = d -> {
            final DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(
                cleanSimpleDateFormat).withChronology(d.getChronology());
            return STRING_LITERAL_QUOTE + inputDataDateTimeFormatter.print(d)
                + STRING_LITERAL_QUOTE;
          };
          exactGranularity = simpleDateFormatGranularity(cleanSimpleDateFormat);
      }
    }

    // determines the granularity of a simpelDateFormat.
    // for instance, 'yyyy-MM-dd' is daily = P1D
    private Period simpleDateFormatGranularity(final String cleanSimpleDateFormat) {
      final Set<Character> chars = cleanSimpleDateFormat.chars()
          .mapToObj(chr -> (char) chr)
          .collect(Collectors.toSet());

      // see https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
      if (chars.contains('S')) {
        return Period.millis(1);
      } else if (chars.contains('s')) {
        return Period.seconds(1);
      } else if (chars.contains('m')) {
        return Period.minutes(1);
      } else if (chars.contains('h') || chars.contains('H') || chars.contains('k')
          || chars.contains('K')) {
        return Period.hours(1);
      } else if (chars.contains('d')) {
        return Period.days(1);
      } // some fancy formats are skipped
      else if (chars.contains('M') || chars.contains('L')) {
        return Period.months(1);
      } else if (chars.contains('y')) {
        return Period.years(1);
      }
      return null;
    }
  }
}
