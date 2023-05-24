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
package ai.startree.thirdeye.detectionpipeline.sql.macro.function;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.spi.api.TimeColumnApi;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunction;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunctionContext;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.joda.time.Period;

/**
 * This macro is tightly coupled with {@link TimeGroupFunction}.
 * When doing queries like:
 * SELECT __timeGroup(timeColumn, 'timeFormat', 'granularity') as ts FROM ... GROUP BY ts
 * the GROUP BY ts is an expensive operation, because the __timeGroup is applied to all rows.
 * It can be replaced by GROUP BY timeColumn if and only if the 'granularity' parameter is equal to
 * the timeColumn granularity.
 * For instance, if the __timeGroup granularity parameter is P1D, and the timeColum format is
 * yyyy-MM-dd, then the timeColumn granularity
 * is daily, and
 * SELECT __timeGroup(timeColumn, 'yyyy-MM-dd', 'P1D') as ts FROM ... GROUP BY ts
 * is equivalent to
 * SELECT __timeGroup(timeColumn, 'yyyy-MM-dd', 'P1D') as ts FROM ... GROUP BY timeColumn
 * but the latter is much faster because __timeGroup is only applied to the aggregated rows
 *
 * Note: some time formats like yyyy-MM-dd do not contain the timezone information.
 * We assume the timezone of a timeFormat that does not contain the timezone information is the
 * timezone of the alert.
 *
 * Most of the time, this optimization won't be applied, even if it could be possible.
 * For instance, if the datasetConfigDto has a timeUnit of DAY, but the timeColumn format is in
 * epoch milliseconds,
 * we won't try to perform the optimization. This is because timeUnit and granularity is deprecated
 * in Pinot and tends to be incorrect.
 * Also, nothing prevents a user from inserting a time in epoch milliseconds that does not
 * correspond to DAY granularity
 *
 * The granularity of a timeFormat is obtained via {@link ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder#granularityOfTimeFormat(String)}.
 *
 * // TODO explain potential timezone issue
 *
 * See https://docs.google.com/document/d/1ntSEuESUEEwpYQbaH8GwvWU204WB3eNE9v_-eb6f8CY/edit for more
 * info.
 */

public class TimeGroupKeyFunction implements MacroFunction {

  @Override
  public String name() {
    return "__timeGroupKey";
  }

  @Override
  public String expandMacro(final List<String> macroParams, final MacroFunctionContext context) {
    //parse params
    checkArgument(macroParams.size() == 4,
        "__timeGroupKey macro requires 4 parameters. Eg: __timeGroupKey(timeColumn, 'timeFormat', 'granularity', timeGroupAlias)");
    String timeColumn = macroParams.get(0);
    String timeColumnFormat = context.getLiteralUnquoter().apply(macroParams.get(1));
    final String granularityText = context.getLiteralUnquoter().apply(macroParams.get(2));
    final Period granularity = isoPeriod(granularityText);
    final String timeGroupAlias = macroParams.get(3);

    if (isAutoTimeConfiguration(timeColumn)) {
      final DatasetConfigDTO datasetConfigDTO = context.getDatasetConfigDTO();
      Objects.requireNonNull(datasetConfigDTO, "Cannot use AUTO mode for macro. dataset table name is not defined.");
      // use directly an exact bucket time column if available
      final Optional<String> exactBucketTimeColumn = optional(datasetConfigDTO.getTimeColumns()).orElse(
              Collections.emptyList())
          .stream()
          .filter(c -> c.getGranularity() != null && c.getGranularity().equals(granularityText))
          // assume timezone is UTC TODO CYRIL IMPLEMENT TIMEZONE SUPPORT
          // assume format is epoch milliseconds TODO CYRIL IMPLEMENT SUPPORT FOR OTHER FORMATS
          .findFirst()
          .map(TimeColumnApi::getName)
          .map(context.getIdentifierQuoter());
      if (exactBucketTimeColumn.isPresent()) {
        return exactBucketTimeColumn.get();
      }
      // else use the main time column
      timeColumn = context.getIdentifierQuoter().apply(datasetConfigDTO.getTimeColumn());
      timeColumnFormat = datasetConfigDTO.getTimeFormat();
    }

    final Period timeColumnGranularity = context.getSqlExpressionBuilder()
        .granularityOfTimeFormat(timeColumnFormat);
    if (timeColumnGranularity != null && timeColumnGranularity.equals(granularity)) {
      // optimization happens
      return timeColumn;
    }

    // optimization does not happen
    return timeGroupAlias;
  }
}
