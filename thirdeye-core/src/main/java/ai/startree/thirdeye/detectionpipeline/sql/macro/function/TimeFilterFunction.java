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
package ai.startree.thirdeye.detectionpipeline.sql.macro.function;

import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.MAX_TIME_MILLIS;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.MIN_TIME_MILLIS;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunction;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunctionContext;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

/**
 * Note: To be correct, TimeFilterFunction must take into account the granularity.
 * This is the case because TimeFilterFunction uses the context.detectionInterval,
 * which is computed by taking into account the granularity and the completeness delay.
 * In the same way, timezone is respected when applying lookback because context.detectionInterval
 * contains the timezone information.
 */
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
    checkArgument(numParams >= 2 && numParams <= 4,
        "timeFilter macro requires 2, 3 or 4 parameters. Eg: __timeFilter(timeColumn, 'timeFormat', ['lookbackFromStartTime'], ['lookbackFromEndTime'])");
    String timeColumn = macroParams.get(0);
    final String timeColumnFormat = context.getLiteralUnquoter().apply(macroParams.get(1));
    String lookbackFromStart = NO_LOOKBACK;
    String lookbackFromEnd = NO_LOOKBACK;
    if (macroParams.size() >= 3) {
      lookbackFromStart = context.getLiteralUnquoter().apply(macroParams.get(2));
    }
    if (macroParams.size() >= 4) {
      lookbackFromEnd = context.getLiteralUnquoter().apply(macroParams.get(3));
    }

    // compute timeLimits
    final Interval detectionInterval = context.getDetectionInterval();
    final DateTime filterLowerBound = detectionInterval.getStart()
        .minus(Period.parse(lookbackFromStart, ISOPeriodFormat.standard()));
    final DateTime filterUpperBound = detectionInterval.getEnd()
        .minus(Period.parse(lookbackFromEnd, ISOPeriodFormat.standard()));
    final Interval filterInterval = new Interval(filterLowerBound, filterUpperBound);

    //write time limits to metadata
    Map<String, String> properties = context.getProperties();
    properties.put(MIN_TIME_MILLIS.toString(), String.valueOf(filterLowerBound.getMillis()));
    properties.put(MAX_TIME_MILLIS.toString(), String.valueOf(filterUpperBound.getMillis()));

    if (timeColumn.equals(AUTO_TIME_CONFIG)) {
      final DatasetConfigDTO datasetConfigDTO = context.getDatasetConfigDTO();
      return context.getSqlExpressionBuilder()
          .getTimeFilterExpression(datasetConfigDTO.getTimeColumn(),
              filterInterval,
              datasetConfigDTO.getTimeFormat(),
              datasetConfigDTO.getTimeUnit().toString());
    }

    // generate SQL expression
    return context.getSqlExpressionBuilder()
        .getTimeFilterExpression(timeColumn, filterInterval, timeColumnFormat);
  }
}
