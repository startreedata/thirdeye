/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.spi.Constants.UTC_TIMEZONE;
import static ai.startree.thirdeye.spi.datasource.macro.MacroMetadataKeys.GRANULARITY;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static ai.startree.thirdeye.spi.util.TimeUtils.timezonesAreEquivalent;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import ai.startree.thirdeye.spi.api.TimeColumnApi;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunction;
import ai.startree.thirdeye.spi.datasource.macro.MacroFunctionContext;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.joda.time.Period;

public class TimeGroupFunction implements MacroFunction {

  @Override
  public String name() {
    return "__timeGroup";
  }

  @Override
  public String expandMacro(final List<String> macroParams, final MacroFunctionContext context) {
    //parse params
    checkArgument(macroParams.size() == 3,
        "timeGroup macro requires 3 parameters. Eg: __timeGroup(timeColumn, 'timeFormat', 'granularity')");
    String timeColumn = macroParams.get(0);
    String timeColumnFormat = context.getLiteralUnquoter().apply(macroParams.get(1));
    final Period granularity = isoPeriod(context.getLiteralUnquoter().apply(macroParams.get(2)));
    final String timezone = context.getDetectionInterval().getChronology().getZone().toString();

    //write granularity to metadata
    context.getProperties().put(GRANULARITY.toString(), granularity.toString());
    if (isAutoTimeConfiguration(timeColumn)) {
      final DatasetConfigDTO datasetConfigDTO = context.getDatasetConfigDTO();
      Objects.requireNonNull(datasetConfigDTO,
          "Cannot use AUTO mode for macro. dataset table name is not defined.");
      final String alertTimezone = context.getDetectionInterval().getChronology().getZone().getID();
      final Optional<TimeColumnApi> exactBucketTimeColumn = optional(
          datasetConfigDTO.getTimeColumns()).orElse(
              Collections.emptyList())
          .stream()
          .filter(timeCol -> granularity.toString().equals(timeCol.getGranularity()))
          .filter(c -> timezonesAreEquivalent(optional(c.getTimezone()).orElse(UTC_TIMEZONE), alertTimezone))
          .findFirst();
      if (exactBucketTimeColumn.isPresent()) {
        // use a column of pre-computed exact buckets
        final TimeColumnApi timeColumnApi = exactBucketTimeColumn.get();
        checkNotNull(timeColumnApi.getName(),
            "A custom timeColumn of granularity %s is provided in the %s dataset configuration, but the name field is empty. name is required.",
            granularity, datasetConfigDTO.getDataset());
        checkNotNull(timeColumnApi.getFormat(),
            "A custom timeColumn of granularity %s is provided in the %s dataset configuration, but the format field is empty. format is required.",
            granularity, datasetConfigDTO.getDataset());
        // TODO CYRIL can be optimized further - if timeformat is milliseconds, then there is no need to use the timegroup expression - not sure if pinot optimizes under the hood
        timeColumn = context.getIdentifierQuoter().apply(timeColumnApi.getName());
        timeColumnFormat = timeColumnApi.getFormat();
      } else {
        // use the main time column
        timeColumn = context.getIdentifierQuoter().apply(datasetConfigDTO.getTimeColumn());
        timeColumnFormat = datasetConfigDTO.getTimeFormat();
      }
    }

    //generate SQL expression
    return context.getSqlExpressionBuilder()
        .getTimeGroupExpression(timeColumn, timeColumnFormat, granularity, timezone);
  }
}
