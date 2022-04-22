/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinot;

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class PinotSqlExpressionBuilder implements SqlExpressionBuilder {

  private static final String PERCENTILE_TDIGEST_PREFIX = "PERCENTILETDigest";

  public static final long SECOND_SCALE = 1000; // number of second in milliseconds
  public static final long MINUTE_SCALE = 60 * SECOND_SCALE; // number of second in milliseconds
  public static final long HOUR_SCALE = 60 * MINUTE_SCALE;
  public static final long DAY_SCALE = 24 * HOUR_SCALE;

  @Override
  public String getTimeFilterExpression(final String timeColumn, final long minTimeMillisIncluded,
      final long maxTimeMillisExcluded) {
    return String.format("%s >= %s AND %s < %s",
        timeColumn,
        minTimeMillisIncluded,
        timeColumn,
        maxTimeMillisExcluded);
  }

  @Override
  public String getTimeFilterExpression(final String timeColumn, final long minTimeMillisIncluded,
      final long maxTimeMillisExcluded,
      @Nullable final String timeFormat,
      @Nullable final String timeUnit) {
    if (timeFormat == null) {
      return getTimeFilterExpression(timeColumn, minTimeMillisIncluded, maxTimeMillisExcluded);
    }
    String lowerBound;
    String upperBound;
    if ("EPOCH".equals(timeFormat)) {
      if (TimeUnit.MILLISECONDS.toString().equals(timeUnit)) {
        lowerBound = String.valueOf(minTimeMillisIncluded);
        upperBound = String.valueOf(maxTimeMillisExcluded);
      } else if (timeUnit == null || TimeUnit.SECONDS.toString().equals(timeUnit)) {
        lowerBound = String.valueOf(minTimeMillisIncluded / SECOND_SCALE);
        upperBound = String.valueOf(maxTimeMillisExcluded / SECOND_SCALE);
      } else if (TimeUnit.MINUTES.toString().equals(timeUnit)) {
        lowerBound = String.valueOf(minTimeMillisIncluded / MINUTE_SCALE);
        upperBound = String.valueOf(maxTimeMillisExcluded / MINUTE_SCALE);
      } else if (TimeUnit.HOURS.toString().equals(timeUnit)) {
        lowerBound = String.valueOf(minTimeMillisIncluded / HOUR_SCALE);
        upperBound = String.valueOf(maxTimeMillisExcluded / HOUR_SCALE);
      } else if (TimeUnit.DAYS.toString().equals(timeUnit)) {
        lowerBound = String.valueOf(minTimeMillisIncluded / DAY_SCALE);
        upperBound = String.valueOf(maxTimeMillisExcluded / DAY_SCALE);
      } else {
        throw new UnsupportedOperationException(String.format(
            "Unsupported TimeUnit for filter expression: %s",
            timeUnit));
      }
    } else {
      // case SIMPLE_DATE_FORMAT
      String simpleDateFormatString = removeSimpleDateFormatPrefix(timeFormat);
      final DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(
          simpleDateFormatString);
      lowerBound = inputDataDateTimeFormatter.print(minTimeMillisIncluded);
      upperBound = inputDataDateTimeFormatter.print(maxTimeMillisExcluded);
    }

    return String.format("%s >= %s AND %s < %s", timeColumn, lowerBound, timeColumn, upperBound);
  }

  @Override
  public String getTimeGroupExpression(final String timeColumn, final @Nullable String timeFormat,
      final Period granularity, final @Nullable String timeUnit) {
    if (timeFormat == null) {
      return getTimeGroupExpression(timeColumn, "EPOCH_MILLIS",granularity);
    }
    if ("EPOCH".equals(timeFormat)) {
      return getTimeGroupExpression(timeColumn, "1:" + timeUnit +":EPOCH",granularity);
    }
    // case simple date format
    return getTimeGroupExpression(timeColumn, timeFormat, granularity);
  }

  @Override
  public String getTimeGroupExpression(String timeColumn, String timeColumnFormat,
      Period granularity) {
    return String.format(" DATETIMECONVERT(%s,'%s', '1:MILLISECONDS:EPOCH', '%s') ",
        timeColumn,
        timeColumnFormatToPinotFormat(timeColumnFormat),
        periodToPinotFormat(granularity)
    );
  }

  private String timeColumnFormatToPinotFormat(String timeColumnFormat) {
    switch (timeColumnFormat) {
      case "EPOCH_MILLIS":
      case "1:MILLISECONDS:EPOCH":
        return "1:MILLISECONDS:EPOCH";
      case "EPOCH":
      case "1:SECONDS:EPOCH":
        return "1:SECONDS:EPOCH";
      case "EPOCH_HOURS":
      case "1:HOURS:EPOCH":
        return "1:HOURS:EPOCH";
      default:
        final String simpleDateFormatString = removeSimpleDateFormatPrefix(timeColumnFormat);
        new SimpleDateFormat(simpleDateFormatString);
        return String.format("1:DAYS:SIMPLE_DATE_FORMAT:%s", timeColumnFormat);
    }
  }

  @NonNull
  private String removeSimpleDateFormatPrefix(final String timeColumnFormat) {
    // remove (1:DAYS:)SIMPLE_DATE_FORMAT:
    return timeColumnFormat.replaceFirst("^([0-9]:[A-Z]+:)?SIMPLE_DATE_FORMAT:", "");
  }

  private String periodToPinotFormat(final Period period) {
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
}
