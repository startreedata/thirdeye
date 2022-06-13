/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomalydetection.datafilter;

import ai.startree.thirdeye.metric.MetricTimeSeries;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.Map;

public class DummyDataFilter extends BaseDataFilter {

  @Override
  public void setParameters(Map<String, String> props) {
  }

  @Override
  public boolean isQualified(MetricTimeSeries metricTimeSeries, DimensionMap dimensionMap) {
    return true;
  }

  @Override
  public boolean isQualified(MetricTimeSeries metricTimeSeries, DimensionMap dimensionMap,
      long windowStart,
      long windowEnd) {
    return true;
  }
}
