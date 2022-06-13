/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.util;

import ai.startree.thirdeye.detection.detector.email.filter.AlertFilter;
import ai.startree.thirdeye.detection.detector.email.filter.AlertFilterFactory;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

