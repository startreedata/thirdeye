/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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
