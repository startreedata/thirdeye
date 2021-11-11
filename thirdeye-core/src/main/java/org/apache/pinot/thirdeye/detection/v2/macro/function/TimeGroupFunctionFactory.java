package org.apache.pinot.thirdeye.detection.v2.macro.function;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.pinot.thirdeye.spi.datasource.macro.MacroMetadataKeys.GRANULARITY;

import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunction;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunctionContext;
import org.apache.pinot.thirdeye.spi.datasource.macro.MacroFunctionFactory;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

public class TimeGroupFunctionFactory implements MacroFunctionFactory {

  @Override
  public String name() {
    return "__timeGroup";
  }

  @Override
  public MacroFunction build(MacroFunctionContext context) {
    return macroParams -> {
      //parse params
      checkArgument(macroParams.size() == 3,
          "timeGroup macro requires 3 parameters");
      String timeColumn = macroParams.get(0);
      String timeColumnFormat = macroParams.get(1);
      String granularityText = macroParams.get(2);
      Period granularity = Period.parse(granularityText, ISOPeriodFormat.standard());

      //write granularity to metadata
      context.getProperties().put(GRANULARITY.toString(), granularityText);

      //generate SQL expression
      return context.getSqlExpressionBuilder()
          .getTimeGroupExpression(timeColumn, timeColumnFormat, granularity);
    };
  }
}
