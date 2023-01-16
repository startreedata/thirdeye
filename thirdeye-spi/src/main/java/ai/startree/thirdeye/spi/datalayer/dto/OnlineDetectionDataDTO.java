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
