/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.calcite;

import ai.startree.thirdeye.spi.datalayer.Predicate;

// todo rename - this is not Timeseries specific
public class QueryPredicate {

  private final Predicate predicate;
  private final DimensionType metricType;
  private final String dataset;

  private QueryPredicate(final Predicate predicate,
      final DimensionType metricType, final String dataset) {
    this.predicate = predicate;
    this.metricType = metricType;
    this.dataset = dataset;
  }

  public static QueryPredicate of(final Predicate predicate, final DimensionType metricType, final String dataset) {
    return new QueryPredicate(predicate, metricType, dataset);
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

  // fixme cyril - alert evaluator getDimensionType is hardcoed to STRING - dimension type is not implemented correctly in onboarder
  public enum DimensionType {
    STRING,
    NUMERIC,
    BOOLEAN
  }
}
