package org.apache.pinot.thirdeye.spi.util;

import org.apache.pinot.thirdeye.spi.datalayer.pojo.MetricConfigBean;

public class SpiUtils {
  private SpiUtils() {}

  public static String constructMetricAlias(String datasetName, String metricName) {
    return datasetName + MetricConfigBean.ALIAS_JOINER + metricName;
  }
}
