/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.alert;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Detection alert filter result.
 */
public class DetectionAlertFilterResult {

  /**
   * The Result.
   */
  private final Map<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result;

  /**
   * Instantiates a new Detection alert filter result.
   */
  public DetectionAlertFilterResult() {
    this.result = new HashMap<>();
  }

  /**
   * Instantiates a new Detection alert filter result.
   *
   * @param result the result
   */
  public DetectionAlertFilterResult(
      Map<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result) {
    Preconditions.checkNotNull(result);
    this.result = result;
  }

  /**
   * Gets result.
   *
   * @return the result
   */
  public Map<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> getResult() {
    return result;
  }

  /**
   * Gets all anomalies.
   *
   * @return the all anomalies
   */
  public List<MergedAnomalyResultDTO> getAllAnomalies() {
    List<MergedAnomalyResultDTO> allAnomalies = new ArrayList<>();
    for (Set<MergedAnomalyResultDTO> anomalies : result.values()) {
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
  public DetectionAlertFilterResult addMapping(DetectionAlertFilterNotification alertProp,
      Set<MergedAnomalyResultDTO> anomalies) {
    if (!this.result.containsKey(alertProp)) {
      this.result.put(alertProp, new HashSet<MergedAnomalyResultDTO>());
    }
    this.result.get(alertProp).addAll(anomalies);
    return this;
  }
}
