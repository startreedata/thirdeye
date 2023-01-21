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
package ai.startree.thirdeye.subscriptiongroup.filter;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Detection alert filter result.
 */
public class SubscriptionGroupFilterResult {

  /**
   * The Result.
   */
  private final Map<DetectionAlertFilterNotification, Set<AnomalyDTO>> result;

  /**
   * Instantiates a new Detection alert filter result.
   */
  public SubscriptionGroupFilterResult() {
    result = new HashMap<>();
  }

  /**
   * Gets result.
   *
   * @return the result
   */
  public Map<DetectionAlertFilterNotification, Set<AnomalyDTO>> getResult() {
    return result;
  }

  /**
   * Gets all anomalies.
   *
   * @return the all anomalies
   */
  public List<AnomalyDTO> getAllAnomalies() {
    final List<AnomalyDTO> allAnomalies = new ArrayList<>();
    for (final Set<AnomalyDTO> anomalies : result.values()) {
      allAnomalies.addAll(anomalies);
    }
    return allAnomalies;
  }

  /**
   * Add a mapping from anomalies to recipients in this detection alert filter result.
   *
   * @param alertProp the alert properties
   * @param anomalies the anomalies
   * @return the detection alert filter result
   */
  public SubscriptionGroupFilterResult addMapping(final DetectionAlertFilterNotification alertProp,
      final Set<AnomalyDTO> anomalies) {
    if (!result.containsKey(alertProp)) {
      result.put(alertProp, new HashSet<>());
    }
    result.get(alertProp).addAll(anomalies);
    return this;
  }
}
