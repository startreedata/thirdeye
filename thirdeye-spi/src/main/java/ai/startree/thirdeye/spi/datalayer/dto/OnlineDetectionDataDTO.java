/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import java.util.Objects;

public class OnlineDetectionDataDTO extends AbstractDTO {

  private String dataset;

  private String metric;

  private String onlineDetectionData;

  public String getDataset() {
    return dataset;
  }

  public void setDataset(String dataset) {
    this.dataset = dataset;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getOnlineDetectionData() {
    return onlineDetectionData;
  }

  public void setOnlineDetectionData(String onlineDetectionData) {
    this.onlineDetectionData = onlineDetectionData;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof OnlineDetectionDataDTO)) {
      return false;
    }

    OnlineDetectionDataDTO onlineDetectionDataDTO = (OnlineDetectionDataDTO) o;

    return Objects.equals(getId(), onlineDetectionDataDTO.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}
