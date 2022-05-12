/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.sql.macro.function;

import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.GRANULARITY;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datasource.macro.MacroFunction;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunctionContext;
import java.util.List;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

public class TimeGroupFunction implements MacroFunction {

  @Override
  public String name() {
    return "__timeGroup";
  }

  @Override
  public String expandMacro(final List<String> macroParams, final MacroFunctionContext context) {
    //parse params
    checkArgument(macroParams.size() == 3, "timeGroup macro requires 3 parameters");
    final String timeColumn = macroParams.get(0);
    final String timeColumnFormat = context.getLiteralUnquoter().apply(macroParams.get(1));
    final String granularityText = context.getLiteralUnquoter().apply(macroParams.get(2));
    Period granularity = Period.parse(granularityText, ISOPeriodFormat.standard());
    final String timezone = context.getDetectionInterval().getChronology().getZone().toString();

    //write granularity to metadata
    context.getProperties().put(GRANULARITY.toString(), granularityText);

    //generate SQL expression
    return context.getSqlExpressionBuilder()
        .getTimeGroupExpression(timeColumn, timeColumnFormat, granularity, timezone);
  }
}
