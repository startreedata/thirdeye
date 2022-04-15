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
import org.joda.time.Period;

public class PinotSqlExpressionBuilder implements SqlExpressionBuilder {

  private static final String PERCENTILE_TDIGEST_PREFIX = "PERCENTILETDigest";

  public String getTimeFilterExpression(String column, long minTimeMillisIncluded,
      long maxTimeMillisExcluded) {
    return String.format("%s >= %s AND %s < %s",
        column,
        minTimeMillisIncluded,
        column,
        maxTimeMillisExcluded);
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
        new SimpleDateFormat(timeColumnFormat);
        return String.format("1:DAYS:SIMPLE_DATE_FORMAT:%s", timeColumnFormat);
    }
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
