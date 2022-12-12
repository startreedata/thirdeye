/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.plugins.postprocessor.merger;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyType;
import java.util.Objects;

public class AnomalyKey {

  final String metric;
  final String collection;
  final String patternKey;
  final AnomalyType type;

  private AnomalyKey(final String metric, final String collection, final String patternKey,
      final AnomalyType type) {
    this.metric = metric;
    this.collection = collection;
    this.patternKey = patternKey;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AnomalyKey)) {
      return false;
    }
    AnomalyKey that = (AnomalyKey) o;
    return Objects.equals(metric, that.metric) && Objects.equals(collection, that.collection)
        && Objects.equals(patternKey, that.patternKey) && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metric, collection, patternKey, type);
  }

  public static AnomalyKey create(final MergedAnomalyResultDTO anomaly) {
    final String patternKey = getPatternKey(anomaly);
    return new AnomalyKey(anomaly.getMetric(), anomaly.getCollection(), patternKey,
        anomaly.getType());
  }

  private static String getPatternKey(final MergedAnomalyResultDTO anomaly) {
    String patternKey = "";
    if (!Double.isNaN(anomaly.getAvgBaselineVal()) && !Double.isNaN(anomaly.getAvgCurrentVal())) {
      patternKey = (anomaly.getAvgCurrentVal() > anomaly.getAvgBaselineVal()) ? "UP" : "DOWN";
    }
    return patternKey;
  }
}
