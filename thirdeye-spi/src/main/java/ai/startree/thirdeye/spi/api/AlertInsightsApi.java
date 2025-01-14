/*
 * Copyright 2024 StarTree Inc
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
  private Long datasetStartTime;
  /**
   * Biggest observed value. Does not necessarily correspond to the biggest event true physical
   * time. Ie does not necessarily correspond to the close time of the bucket.
   * In a batch setting it will actually most often correspond to the last bucket start.
   * For instance, if the timeColumn granularity is daily (eg format is yyyy-MM-dd),
   * and data is loaded in batch, then if the biggest observed value is 2023-11-20,
   * it's totally possible than an event was collected on 2023-11-20 13:00.
   * This value should be used carefully, it is mostly present for debugging purpose.
   */
  private Long datasetEndTime;
  /**
   * If the datasetEndTime fetched from the database looks incorrect, it is stored in this field. A
   * safe value is put in the datasetEndTime field.
   */
  private Long suspiciousDatasetEndTime;

  /**
   * If the datasetStartTime fetched from the database looks incorrect, it is stored in this field. A
   * safer value is put in the datasetStartTime field.
   */
  private Long suspiciousDatasetStartTime;
  /**
   * Recommended start time and end time to use in the UI time selector if no time
   * is set.
   * Takes care of all time gotchas, for instance the one described for datasetEndTime.
   */
  private Long defaultStartTime;
  private Long defaultEndTime;
  private String defaultCron;
  private AnalysisRunInfo analysisRunInfo;

  public Long getDatasetStartTime() {
    return datasetStartTime;
  }

  public AlertInsightsApi setDatasetStartTime(final Long datasetStartTime) {
    this.datasetStartTime = datasetStartTime;
    return this;
  }

  public Long getDatasetEndTime() {
    return datasetEndTime;
  }

  public AlertInsightsApi setDatasetEndTime(final Long datasetEndTime) {
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

  public Long getDefaultStartTime() {
    return defaultStartTime;
  }

  public AlertInsightsApi setDefaultStartTime(final Long defaultStartTime) {
    this.defaultStartTime = defaultStartTime;
    return this;
  }

  public Long getDefaultEndTime() {
    return defaultEndTime;
  }

  public AlertInsightsApi setDefaultEndTime(final Long defaultEndTime) {
    this.defaultEndTime = defaultEndTime;
    return this;
  }

  public Long getSuspiciousDatasetEndTime() {
    return suspiciousDatasetEndTime;
  }

  public AlertInsightsApi setSuspiciousDatasetEndTime(final Long suspiciousDatasetEndTime) {
    this.suspiciousDatasetEndTime = suspiciousDatasetEndTime;
    return this;
  }

  public AnalysisRunInfo getAnalysisRunInfo() {
    return analysisRunInfo;
  }

  public AlertInsightsApi setAnalysisRunInfo(
      final AnalysisRunInfo analysisRunInfo) {
    this.analysisRunInfo = analysisRunInfo;
    return this;
  }

  public String getDefaultCron() {
    return defaultCron;
  }

  public AlertInsightsApi setDefaultCron(
      final String defaultCron) {
    this.defaultCron = defaultCron;
    return this;
  }

  public Long getSuspiciousDatasetStartTime() {
    return suspiciousDatasetStartTime;
  }

  public AlertInsightsApi setSuspiciousDatasetStartTime(final Long suspiciousDatasetStartTime) {
    this.suspiciousDatasetStartTime = suspiciousDatasetStartTime;
    return this;
  }
}
