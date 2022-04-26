/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import java.util.List;
import java.util.Map;

/**
 * Interface for synthetic baselines constructed from one or multiple raw metric slices pointing to
 * different time periods. The offsets to chose for these time slices are determined by the
 * implementation.
 * All implementations must support multi-indexed data frames with an index consisting of at least
 * a
 * time column (and possibly additional columns) and a value column.
 *
 * The interface supports a scatter-gather flow of data - turning 1 base slice into 1..N data
 * slices
 * (scatter), then filtering a N..M sized set of results down to the specifically required N slices
 * and reduce the N results down to 1 aggregate result (gather).
 */
public interface Baseline {

  String COL_TIME = Constants.COL_TIME;
  String COL_VALUE = Constants.COL_VALUE;

  /**
   * Returns the set of raw data slices required to compute the synthetic baseline for the given
   * input slice.
   *
   * @param slice base metric slice
   * @return set of raw metric slices
   */
  List<MetricSlice> scatter(MetricSlice slice);

  /**
   * Returns the synthetic baseline computed from a set of inputs.
   * If a series is provided for slice, aligns timestamps for coarse grained data.
   *
   * <br/><b>NOTE:</b> Must filter out non-matching slices from a potentially large pool of results.
   *
   * @param slice base metric slice
   * @param data map of intermediate result dataframes (keyed by metric slice)
   * @return synthetic result dataframe
   */
  DataFrame gather(MetricSlice slice, Map<MetricSlice, DataFrame> data);
}
