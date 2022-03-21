/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

/**
 * The class for evaluation metrics.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvaluationDTO extends AbstractDTO {

  private long detectionConfigId; // the detection config id
  private long startTime; // the start time for the detection window being monitored
  private long endTime; // the end time for the detection window being monitored
  private String detectorName; // the name for the detector
  private Double mape; //  the mean absolute percentage error (MAPE)
  private String metricUrn; // the metric urn

  public long getDetectionConfigId() {
    return detectionConfigId;
  }

  public void setDetectionConfigId(long detectionConfigId) {
    this.detectionConfigId = detectionConfigId;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public String getDetectorName() {
    return detectorName;
  }

  public void setDetectorName(String detectorName) {
    this.detectorName = detectorName;
  }

  public Double getMape() {
    return mape;
  }

  public void setMape(Double mape) {
    this.mape = mape;
  }

  public String getMetricUrn() {
    return metricUrn;
  }

  public void setMetricUrn(String metricUrn) {
    this.metricUrn = metricUrn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvaluationDTO)) {
      return false;
    }
    EvaluationDTO that = (EvaluationDTO) o;
    return detectionConfigId == that.detectionConfigId && startTime == that.startTime
        && endTime == that.endTime
        && Double.compare(that.mape, mape) == 0 && Objects.equals(detectorName, that.detectorName)
        && Objects.equals(
        metricUrn, that.metricUrn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(detectionConfigId, startTime, endTime, detectorName, mape, metricUrn);
  }

  @Override
  public String toString() {
    return "EvaluationBean{" + "detectionConfigId=" + detectionConfigId + ", startTime=" + startTime
        + ", endTime="
        + endTime + ", detectorName='" + detectorName + '\'' + ", mape=" + mape + ", metricUrn='"
        + metricUrn + '\''
        + '}';
  }
}
