/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import java.util.Objects;

/**
 * Bean to store the detection status
 */
public class DetectionStatusDTO extends AbstractDTO implements Comparable<DetectionStatusDTO> {

  private long functionId;
  private String dataset;
  private long dateToCheckInMS;
  private String dateToCheckInSDF;
  private boolean detectionRun = false;

  public String getDataset() {
    return dataset;
  }

  public void setDataset(String dataset) {
    this.dataset = dataset;
  }

  public long getDateToCheckInMS() {
    return dateToCheckInMS;
  }

  public void setDateToCheckInMS(long dateToCheckInMS) {
    this.dateToCheckInMS = dateToCheckInMS;
  }

  public String getDateToCheckInSDF() {
    return dateToCheckInSDF;
  }

  public void setDateToCheckInSDF(String dateToCheckInSDF) {
    this.dateToCheckInSDF = dateToCheckInSDF;
  }

  public long getFunctionId() {
    return functionId;
  }

  public void setFunctionId(long functionId) {
    this.functionId = functionId;
  }

  public boolean isDetectionRun() {
    return detectionRun;
  }

  public void setDetectionRun(boolean detectionRun) {
    this.detectionRun = detectionRun;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DetectionStatusDTO)) {
      return false;
    }
    DetectionStatusDTO ds = (DetectionStatusDTO) o;
    return Objects.equals(getId(), ds.getId()) && Objects.equals(dataset, ds.getDataset())
        && Objects.equals(dateToCheckInMS, ds.getDateToCheckInMS())
        && Objects.equals(dateToCheckInSDF, ds.getDateToCheckInSDF())
        && Objects.equals(functionId, ds.getFunctionId()) && Objects
        .equals(detectionRun, ds.isDetectionRun());
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(getId(), dataset, dateToCheckInMS, dateToCheckInSDF, functionId, detectionRun);
  }

  @Override
  public int compareTo(DetectionStatusDTO o) {
    return dateToCheckInSDF.compareTo(o.getDateToCheckInSDF());
  }
}
