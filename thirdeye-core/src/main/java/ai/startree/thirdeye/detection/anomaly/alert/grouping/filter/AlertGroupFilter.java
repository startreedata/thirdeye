/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
