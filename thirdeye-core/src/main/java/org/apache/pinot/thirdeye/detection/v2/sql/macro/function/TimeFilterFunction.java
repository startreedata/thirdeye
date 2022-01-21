package org.apache.pinot.thirdeye.detection.v2.sql.macro.function;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.pinot.thirdeye.spi.datasource.macro.MacroMetadataKeys.MAX_TIME_MILLIS;
import static org.apache.pinot.thirdeye.spi.datasource.macro.MacroMetadataKeys.MIN_TIME_MILLIS;
import static org.apache.pinot.thirdeye.spi.datasource.macro.MacroMetadataKeys.TIME_COLUMN;

import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunction;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunctionContext;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

public class TimeFilterFunction implements MacroFunction {

  private final String NO_LOOKBACK = "P0D";

  @Override
  public String name() {
    return "__timeFilter";
  }

  @Override
  public String expandMacro(final List<String> macroParams, final MacroFunctionContext context) {
    //parse params
    int numParams = macroParams.size();
    checkArgument(numParams >= 1 && numParams <= 3,
        "timeFilter macro requires 1, 2 or 3 parameters");
    String timeColumn = macroParams.get(0);
    String lookbackFromStart = NO_LOOKBACK;
    String lookbackFromEnd = NO_LOOKBACK;
    if (macroParams.size() >= 2) {
      lookbackFromStart = context.getLiteralUnquoter().apply(macroParams.get(1));
    }
    if (macroParams.size() >= 3) {
      lookbackFromEnd = context.getLiteralUnquoter().apply(macroParams.get(2));
    }

    // compute timeLimits
    Interval detectionInterval = context.getDetectionInterval();
    long minTimeMillis = detectionInterval.getStart()
        .minus(Period.parse(lookbackFromStart, ISOPeriodFormat.standard()))
        .getMillis();
    long maxTimeMillis = detectionInterval.getEnd()
        .minus(Period.parse(lookbackFromEnd, ISOPeriodFormat.standard()))
        .getMillis();

    //write time limits to metadata
    Map<String, String> properties = context.getProperties();
    properties.put(TIME_COLUMN.toString(), timeColumn);
    properties.put(MIN_TIME_MILLIS.toString(), String.valueOf(minTimeMillis));
    properties.put(MAX_TIME_MILLIS.toString(), String.valueOf(maxTimeMillis));

    // generate SQL expression
    return context.getSqlExpressionBuilder()
        .getTimeFilterExpression(timeColumn, minTimeMillis, maxTimeMillis);
  }
}
