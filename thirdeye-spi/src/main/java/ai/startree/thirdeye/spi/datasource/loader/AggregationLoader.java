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
package ai.startree.thirdeye.spi.datasource.loader;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import java.util.List;
import java.util.concurrent.Future;

public interface AggregationLoader {

  String COL_DIMENSION_NAME = "dimName";
  String COL_DIMENSION_VALUE = "dimValue";
  String COL_AGGREGATION_ONLY_ROWS_COUNT = "rowsCount";
  String COL_AGGREGATION_ONLY_NON_NULL_ROWS_COUNT = "nonNullRowsCount";

  /**
   * Returns a de-aggregation data frame for a given slice with 3 columns:
   * dimension name, dimension value, and metric value.
   * Values are in COL_DIMENSION_NAME, COL_DIMENSION_VALUE, Constants.COL_VALUE
   * @param slice metric slice
   * @param limit top k element limit per dimension name ({@code -1} for default)
   * @return de-aggregation data frame
   */
  DataFrame loadBreakdown(MetricSlice slice, int limit) throws Exception;

  /**
   * Returns metric aggregates grouped by the given dimensions (or none).
   *
   * If there is no dimensions grouping (dimensions is empty), some datasource can have special behaviors.
   * For Pinot, non aggregation queries can return default values. The default value depend on the aggregation function, it is not always zero.
   * See https://docs.pinot.apache.org/users/user-guide-query/supported-aggregations.
   *
   * If dimensions is empty, the DataFrame returned by this method will return 2 additional columns COL_AGGREGATION_ONLY_ROWS_COUNT, COL_AGGREGATION_ONLY_NON_NULL_ROWS_COUNT.
   * These columns are count(*) and count(metricColumn). They will return 0 if there is resp. no rows / no non-null rows.
   *
   * @param slice metric slice
   * @param dimensions dimension names to group by
   * @param limit top k element limit ({@code -1} for default)
   * @return aggregates data frame
   */
  Future<DataFrame> loadAggregateAsync(MetricSlice slice, List<String> dimensions, int limit) throws Exception;
}
