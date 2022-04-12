/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.loader;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import java.util.List;

public interface AggregationLoader {

  /**
   * Returns a de-aggregation data frame for a given slice with 3 columns:
   * dimension name, dimension value, and metric value.
   *
   * @param slice metric slice
   * @param limit top k element limit per dimension name ({@code -1} for default)
   * @return de-aggregation data frame
   */
  DataFrame loadBreakdown(MetricSlice slice, int limit) throws Exception;

  /**
   * Returns metric aggregates grouped by the given dimensions (or none).
   *
   * @param slice metric slice
   * @param dimensions dimension names to group by
   * @param limit top k element limit ({@code -1} for default)
   * @return aggregates data frame
   */
  DataFrame loadAggregate(MetricSlice slice, List<String> dimensions, int limit) throws Exception;
}
