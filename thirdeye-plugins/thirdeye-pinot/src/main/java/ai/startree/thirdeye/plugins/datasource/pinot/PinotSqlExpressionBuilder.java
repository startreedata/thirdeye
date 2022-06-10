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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.spi.Constants.UTC_LIKE_TIMEZONES;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import com.google.common.annotations.VisibleForTesting;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class PinotSqlExpressionBuilder implements SqlExpressionBuilder {

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

  private static final String escapeLiteralQuote(String s) {
    return s.replace(STRING_LITERAL_QUOTE, ESCAPED_STRING_LITERAL_QUOTE);
  }

  @Override
  public String getTimeFilterExpression(final String timeColumn, final Interval filterInterval,
      @NonNull final String timeColumnFormat) {
    final TimeFormat timeFormat = new TimeFormat(timeColumnFormat);

    return String.format("%s >= %s AND %s < %s",
        timeColumn,
        timeFormat.timeFormatter.apply(filterInterval.getStart()),
        timeColumn,
        timeFormat.timeFormatter.apply(filterInterval.getEnd()));
  }

  @Override
  public String getTimeFilterExpression(final String timeColumn, final Interval filterInterval,
      @Nullable final String timeFormat, @Nullable final String timeUnit) {
    if (timeFormat == null) {
      return getTimeFilterExpression(timeColumn, filterInterval, "EPOCH_MILLIS");
    }
    if ("EPOCH".equals(timeFormat)) {
      Objects.requireNonNull(timeUnit);
      return getTimeFilterExpression(timeColumn, filterInterval, "1:" + timeUnit + ":EPOCH");
    }
    // case simple date format
    return getTimeFilterExpression(timeColumn, filterInterval, timeFormat);
  }

  @Override
  public String getTimeGroupExpression(final String timeColumn, final @Nullable String timeFormat,
      final Period granularity, final @Nullable String timeUnit, @Nullable final String timezone) {
    if (timeFormat == null) {
      return getTimeGroupExpression(timeColumn, "EPOCH_MILLIS", granularity, timezone);
    }
    if ("EPOCH".equals(timeFormat)) {
      Objects.requireNonNull(timeUnit);
      return getTimeGroupExpression(timeColumn, "1:" + timeUnit + ":EPOCH", granularity, timezone);
    }
    // case simple date format
    return getTimeGroupExpression(timeColumn, timeFormat, granularity, timezone);
  }

  @Override
  public String getTimeGroupExpression(String timeColumn, @NonNull String timeColumnFormat,
      Period granularity, @Nullable final String timezone) {
    final TimeFormat timeFormat = new TimeFormat(timeColumnFormat);
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
    return timeColumnFormat.replaceFirst("^([0-9]:[A-Z]+:)?SIMPLE_DATE_FORMAT:", "");
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
        return new StringBuilder().append(PERCENTILE_TDIGEST_PREFIX)
            .append("(")
            .append(operands.get(0))
            .append(",")
            .append(percentile)
            .append(")")
            .toString();
      default:
        throw new UnsupportedOperationException();
    }
  }

  /**
   * Class that allows to match multiple user facing time format strings to a given time format.
   * Can return the timeformat for different Pinot functions.
   */
  private static class TimeFormat {

    private final String dateTimeConvertString;
    private final String dateTruncString;
    private final boolean isEpochFormat;
    private final Function<DateTime, String> timeFormatter;

    TimeFormat(String userFacingTimeColumnFormat) {
      switch (userFacingTimeColumnFormat) {
        case "EPOCH_MILLIS":
        case "1:MILLISECONDS:EPOCH":
          dateTimeConvertString = "1:MILLISECONDS:EPOCH";
          dateTruncString = "MILLISECONDS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis());
          break;
        case "EPOCH":
        case "1:SECONDS:EPOCH":
          dateTimeConvertString = "1:SECONDS:EPOCH";
          dateTruncString = "SECONDS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() / SECOND_MILLIS);
          break;
        case "EPOCH_MINUTES":
        case "1:MINUTES:EPOCH":
          dateTimeConvertString = "1:MINUTES:EPOCH";
          dateTruncString = "MINUTES";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() / MINUTE_MILLIS);
          break;
        case "EPOCH_HOURS":
        case "1:HOURS:EPOCH":
          dateTimeConvertString = "1:HOURS:EPOCH";
          dateTruncString = "HOURS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() / HOUR_MILLIS);
          break;
        case "EPOCH_DAYS":
        case "1:DAYS:EPOCH":
          dateTimeConvertString = "1:DAYS:EPOCH";
          dateTruncString = "DAYS";
          isEpochFormat = true;
          timeFormatter = d -> String.valueOf(d.getMillis() / DAY_MILLIS);
          break;
        default:
          // assume simple date format
          final String cleanSimpleDateFormat = removeSimpleDateFormatPrefix(
              userFacingTimeColumnFormat);
          // fail if invalid format
          new SimpleDateFormat(cleanSimpleDateFormat);
          dateTimeConvertString = String.format("1:DAYS:SIMPLE_DATE_FORMAT:%s",
              cleanSimpleDateFormat);
          dateTruncString = null;
          isEpochFormat = false;
          timeFormatter = d -> {
            final DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(
                cleanSimpleDateFormat).withChronology(d.getChronology());
            return STRING_LITERAL_QUOTE + inputDataDateTimeFormatter.print(d) + STRING_LITERAL_QUOTE;
          };
      }
    }
  }
}
