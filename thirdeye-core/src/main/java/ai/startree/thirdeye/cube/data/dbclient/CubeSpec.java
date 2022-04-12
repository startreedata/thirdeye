/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbclient;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Interval;

/**
 * The spec that specifies the metric and its time range to be retrieved from the data base.
 */
public class CubeSpec {

  private CubeTag tag;
  private MetricConfigDTO metricConfigDTO;
  private Interval interval;

  public CubeSpec(final CubeTag tag, final MetricConfigDTO metricConfigDTO, final Interval interval) {
    setTag(tag);
    setMetric(metricConfigDTO);
    setInterval(interval);
  }

  public CubeTag getTag() {
    return tag;
  }

  public void setTag(CubeTag tag) {
    Preconditions.checkNotNull(tag);
    this.tag = tag;
  }
  public @NonNull MetricConfigDTO getMetric() {
    return metricConfigDTO;
  }

  public void setMetric(@NonNull final MetricConfigDTO metricConfigDTO) {
    this.metricConfigDTO = metricConfigDTO;
  }

  public Interval getInterval() {
    return interval;
  }

  public void setInterval(Interval interval) {
    Preconditions.checkNotNull(interval);
    this.interval = interval;
  }
}
