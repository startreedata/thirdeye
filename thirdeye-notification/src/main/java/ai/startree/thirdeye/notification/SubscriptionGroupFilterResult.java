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
package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Detection alert filter result.
 */
public class SubscriptionGroupFilterResult {

  private final Map<SubscriptionGroupDTO, Set<AnomalyDTO>> result = new HashMap<>();

  /**
   * Gets result.
   *
   * @return the result
   */
  public Map<SubscriptionGroupDTO, Set<AnomalyDTO>> getResult() {
    return result;
  }

  /**
   * Gets all anomalies.
   *
   * @return the all anomalies
   */
  public List<AnomalyDTO> getAllAnomalies() {
    return result.values()
        .stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  /**
   * Add a mapping from anomalies to recipients in this detection alert filter result.
   *
   * @param sg the alert properties
   * @param anomalies the anomalies
   * @return the detection alert filter result
   */
  public SubscriptionGroupFilterResult addMapping(final SubscriptionGroupDTO sg,
      final Set<AnomalyDTO> anomalies) {
    if (!result.containsKey(sg)) {
      result.put(sg, new HashSet<>());
    }
    result.get(sg).addAll(anomalies);
    return this;
  }
}
