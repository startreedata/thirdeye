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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AlertInsightsApi {

  private AlertTemplateApi templateWithProperties;
  private long datasetStartTime;
  private long datasetEndTime;
  /**
   * Recommended start time and end time to use in the UI time selector if no time
   * is set.
   */
  private long defaultStartTime;
  private long defaultEndTime;

  public long getDatasetStartTime() {
    return datasetStartTime;
  }

  public AlertInsightsApi setDatasetStartTime(final long datasetStartTime) {
    this.datasetStartTime = datasetStartTime;
    return this;
  }

  public long getDatasetEndTime() {
    return datasetEndTime;
  }

  public AlertInsightsApi setDatasetEndTime(final long datasetEndTime) {
    this.datasetEndTime = datasetEndTime;
    return this;
  }

  public AlertTemplateApi getTemplateWithProperties() {
    return templateWithProperties;
  }

  public AlertInsightsApi setTemplateWithProperties(final AlertTemplateApi templateWithProperties) {
    this.templateWithProperties = templateWithProperties;
    return this;
  }

  public long getDefaultStartTime() {
    return defaultStartTime;
  }

  public AlertInsightsApi setDefaultStartTime(final long defaultStartTime) {
    this.defaultStartTime = defaultStartTime;
    return this;
  }

  public long getDefaultEndTime() {
    return defaultEndTime;
  }

  public AlertInsightsApi setDefaultEndTime(final long defaultEndTime) {
    this.defaultEndTime = defaultEndTime;
    return this;
  }
}
