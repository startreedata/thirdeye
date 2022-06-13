/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomalydetection.context;

import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;

public class TimeSeriesKey {

  private String metricName = "";
  private DimensionMap dimensionMap = new DimensionMap();

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public DimensionMap getDimensionMap() {
    return dimensionMap;
  }

  public void setDimensionMap(DimensionMap dimensionMap) {
    this.dimensionMap = dimensionMap;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TimeSeriesKey) {
      TimeSeriesKey other = (TimeSeriesKey) o;
      return ObjectUtils.equals(metricName, other.metricName)
          && ObjectUtils.equals(dimensionMap, other.dimensionMap);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(metricName, dimensionMap);
  }
}
