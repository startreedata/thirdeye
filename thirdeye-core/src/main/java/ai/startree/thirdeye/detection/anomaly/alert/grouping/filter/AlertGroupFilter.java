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
package ai.startree.thirdeye.detection.anomaly.alert.grouping.filter;

import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import java.util.Map;

// TODO: Unify merged and grouped anomaly. Afterwards, unify their alert filter.

/**
 * A filter for determining if a given grouped anomaly is qualified for sending an alert.
 */
public interface AlertGroupFilter {

  /**
   * Sets the properties of this grouper.
   *
   * @param props the properties for this grouper.
   */
  void setParameters(Map<String, String> props);

  /**
   * Returns if the given grouped anomaly is qualified for passing through the filter.
   *
   * @param groupedAnomaly the given grouped anomaly.
   * @return true if the given grouped anomaly passes through the filter.
   */
  boolean isQualified(GroupedAnomalyResultsDTO groupedAnomaly);
}
