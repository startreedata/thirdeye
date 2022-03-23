/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The grouped anomaly results for alerter. Each group of anomalies should be sent through the same
 * email.
 */
public class GroupedAnomalyResultsDTO extends AbstractDTO {

  private long alertConfigId;
  private DimensionMap dimensions = new DimensionMap();
  private List<Long> anomalyResultsId = new ArrayList<>();
  // the max endTime among all its merged anomaly results
  private boolean isNotified = false;

  public long getAlertConfigId() {
    return alertConfigId;
  }

  public void setAlertConfigId(long alertConfigId) {
    this.alertConfigId = alertConfigId;
  }

  public DimensionMap getDimensions() {
    return dimensions;
  }

  public void setDimensions(DimensionMap dimensions) {
    this.dimensions = dimensions;
  }

  public List<Long> getAnomalyResultsId() {
    return anomalyResultsId;
  }

  public void setAnomalyResultsId(List<Long> anomalyResultsId) {
    this.anomalyResultsId = anomalyResultsId;
  }

  public boolean isNotified() {
    return isNotified;
  }

  public void setNotified(boolean notified) {
    isNotified = notified;
  }

  @JsonIgnore
  private List<MergedAnomalyResultDTO> anomalyResults = new ArrayList<>();

  public List<MergedAnomalyResultDTO> getAnomalyResults() {
    return anomalyResults;
  }

  public void setAnomalyResults(List<MergedAnomalyResultDTO> anomalyResults) {
    this.anomalyResults = anomalyResults;
  }

  public long getEndTime() {
    if (anomalyResults == null || anomalyResults.isEmpty()) {
      return 0;
    }
    Collections.sort(anomalyResults, (o1, o2) -> (int) (o1.getEndTime() - o2.getEndTime()));
    return anomalyResults.get(anomalyResults.size() - 1).getEndTime();
  }
}
