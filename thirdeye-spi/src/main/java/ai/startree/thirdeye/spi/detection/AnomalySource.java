/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.datalayer.Predicate;

public enum AnomalySource {
  DATASET {
    @Override
    public Predicate getPredicate(String predicateValue) {
      return Predicate.IN("collection", predicateValue.split(","));
    }
  },
  METRIC {
    @Override
    public Predicate getPredicate(String predicateValue) {
      return Predicate.IN("metric", predicateValue.split(","));
    }
  },
  ANOMALY_FUNCTION {
    @Override
    public Predicate getPredicate(String predicateValue) {
      return Predicate.IN("functionId", predicateValue.split(","));
    }
  };

  AnomalySource() {

  }

  public abstract Predicate getPredicate(String predicateValue);
}
