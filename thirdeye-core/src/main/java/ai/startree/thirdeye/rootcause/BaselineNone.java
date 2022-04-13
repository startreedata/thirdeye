/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.detection.Baseline;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Baseline that always returns an empty set of data
 */
public class BaselineNone implements Baseline {

  @Override
  public List<MetricSlice> scatter(MetricSlice slice) {
    return Collections.emptyList();
  }

  @Override
  public DataFrame gather(MetricSlice slice, Map<MetricSlice, DataFrame> data) {
    return new DataFrame(COL_TIME, LongSeries.empty())
        .addSeries(COL_VALUE, DoubleSeries.empty());
  }
}
