package org.apache.pinot.thirdeye.datasource.pinot;

import java.text.SimpleDateFormat;
import org.apache.pinot.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import org.joda.time.Period;

public class PinotSqlExpressionBuilder implements SqlExpressionBuilder {

  @Override
  public String getTimeGroupExpression(String timeColumn, String timeColumnFormat, Period granularity) {
    return String.format(" DATETIMECONVERT(%s,'%s', '1:MILLISECONDS:EPOCH', '%s') ",
        timeColumn,
        timeColumnFormatToPinotFormat(timeColumnFormat),
        periodToPinotFormat(granularity)
    );
  }

  private String timeColumnFormatToPinotFormat(String timeColumnFormat) {
    switch (timeColumnFormat) {
      case "EPOCH_MILLIS": case "1:MILLISECONDS:EPOCH":
        return "1:MILLISECONDS:EPOCH";
      case "EPOCH": case "1:SECONDS:EPOCH":
        return "1:SECONDS:EPOCH";
      default:
        new SimpleDateFormat(timeColumnFormat);
        // FIXME CYRIL 1:DAYS is hardcoded ? Is this okay  - I think yes
        return String.format("1:DAYS:SIMPLE_DATE_FORMAT:%s", timeColumnFormat);
    }
  }

  private String periodToPinotFormat(final Period period) {
    if (period.getYears() > 0) {
      return String.format("%s:YEARS", period.getYears());
    } else if (period.getMonths() > 0) {
      return String.format("%s:MONTHS", period.getMonths());
    } else if (period.getWeeks() > 0) {
      return String.format("%s:WEEKS", period.getWeeks());
    } else if (period.getDays() > 0) {
      return String.format("%s:DAYS", period.getDays());
    } else if (period.getMinutes() > 0) {
      return String.format("%s:MINUTES", period.getMinutes());
    } else if (period.getSeconds() > 0) {
      return String.format("%s:SECONDS", period.getSeconds());
    } else if (period.getMillis() > 0) {
      return String.format("%s:MILLISECONDS", period.getMillis());
    }
    throw new RuntimeException(String.format("Could not translate Period to Pinot granularity: %s", period));
  }
}
