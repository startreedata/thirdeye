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

package org.apache.pinot.thirdeye.detection.anomaly.alert.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.detector.email.filter.AlertFilter;
import org.apache.pinot.thirdeye.detection.detector.email.filter.AlertFilterFactory;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertFilterHelper {

  private static final Logger LOG = LoggerFactory.getLogger(AlertFilterHelper.class);

  /**
   * Each function has a filtration rule which let alert module decide if an anomaly should be
   * included in the alert email. This method applies respective filtration rule on list of
   * anomalies.
   */
  public static List<MergedAnomalyResultDTO> applyFiltrationRule(
      List<MergedAnomalyResultDTO> results, AlertFilterFactory alertFilterFactory) {
    if (results.size() == 0) {
      return results;
    }
    // Function ID to Alert Filter
    Map<Long, AlertFilter> functionAlertFilter = new HashMap<>();

    List<MergedAnomalyResultDTO> qualifiedAnomalies = new ArrayList<>();
    for (MergedAnomalyResultDTO result : results) {
      // Lazy initiates alert filter for anomalies of the same anomaly function
      AnomalyFunctionDTO anomalyFunctionSpec = result.getAnomalyFunction();
      long functionId = anomalyFunctionSpec.getId();
      AlertFilter alertFilter = functionAlertFilter.get(functionId);
      if (alertFilter == null) {
        // Get filtration rule from anomaly function configuration
        alertFilter = alertFilterFactory.fromSpec(anomalyFunctionSpec.getAlertFilter());
        functionAlertFilter.put(functionId, alertFilter);
        LOG.info("Using filter {} for anomaly function {} (dataset: {}, topic metric: {})",
            alertFilter,
            functionId, anomalyFunctionSpec.getCollection(), anomalyFunctionSpec.getTopicMetric());
      }
      if (alertFilter.isQualified(result)) {
        qualifiedAnomalies.add(result);
      }
    }
    LOG.info(
        "Found [{}] anomalies qualified to alert after applying filtration rule on [{}] anomalies",
        qualifiedAnomalies.size(), results.size());
    return qualifiedAnomalies;
  }
}

