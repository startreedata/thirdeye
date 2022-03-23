/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.datalayer.Predicate;

public class TimeseriesFilter {

  private final Predicate predicate;
  private final DimensionType metricType;
  private final String dataset;

  private TimeseriesFilter(final Predicate predicate,
      final DimensionType metricType, final String dataset) {
    this.predicate = predicate;
    this.metricType = metricType;
    this.dataset = dataset;
  }

  public static TimeseriesFilter of(final Predicate predicate, final DimensionType metricType, final String dataset) {
    return new TimeseriesFilter(predicate, metricType, dataset);
  }

  public Predicate getPredicate() {
    return predicate;
  }

  public DimensionType getMetricType() {
    return metricType;
  }

  public String getDataset() {
    return dataset;
  }

  // fixme cyril - first version only manages STRING dimensions - move this later
  public enum DimensionType {
    STRING
  }
}
