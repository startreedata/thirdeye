/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbclient;

import com.google.common.base.Preconditions;
import org.joda.time.Interval;

/**
 * The spec that specifies the metric and its time range to be retrieved from the data base.
 */
public class CubeSpec {
  // todo cyril put the metricConfigDTO directly

  private CubeTag tag;
  private String metric;
  private Interval interval;

  /**
   * Constructs a cube spec.
   *
   * @param tag the field name corresponds to the retrieved metric.
   * @param metric the name of the metric.
   * @param interval the interval time range of the metric.
   */
  public CubeSpec(CubeTag tag, String metric, Interval interval) {
    setTag(tag);
    setMetric(metric);
    setInterval(interval);
  }

  /**
   * Returns the field name corresponds to the retrieved metric.
   *
   * @return the field name corresponds to the retrieved metric.
   */
  public CubeTag getTag() {
    return tag;
  }

  /**
   * Sets the field name corresponds to the retrieved metric.
   *
   * @param tag the field name corresponds to the retrieved metric.
   */
  public void setTag(CubeTag tag) {
    Preconditions.checkNotNull(tag);
    this.tag = tag;
  }

  /**
   * Returns the metric name.
   *
   * @return the metric name.
   */
  public String getMetric() {
    return metric;
  }

  /**
   * Sets the metric name.
   *
   * @param metric the metric name.
   */
  public void setMetric(String metric) {
    Preconditions.checkNotNull(metric);
    this.metric = metric;
  }

  /**
   * Returns the interval time range of the metric.
   */
  public Interval getInterval() {
    return interval;
  }

  /**
   * Sets the interval time range of the metric.
   *
   */
  public void setInterval(Interval interval) {
    Preconditions.checkNotNull(interval);
    this.interval = interval;
  }
}
