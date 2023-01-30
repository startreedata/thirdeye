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
package ai.startree.thirdeye.plugins.postprocessor;

import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;

public class AnomalyMergerPostProcessorSpec {

  /**
   * ISO-8601 format. Max gap between two anomalies for the anomalies to be merged.
   */
  private String mergeMaxGap;
  /**
   * ISO-8601 format. Max duration of a merger of anomalies.
   */
  private String mergeMaxDuration;

  /**
   * Set by the detection pipeline at runtime.
   */
  private Long alertId;

  /**
   * Set by the detection pipeline at runtime.
   */
  private EnumerationItemDTO enumerationItemDTO;

  /**
   * Set by the detection pipeline at runtime.
   */
  private AnomalyManager anomalyManager;

  /**
   * Set by the detection pipeline at runtime.
   */
  private DetectionPipelineUsage usage;

  /**
   * If the difference between an existing anomaly and a new anomaly on the same time frame
   * is above this threshold, renotify. Combined with the absolute threshold below.
   * Both thresholds have to pass to be renotified.
   * If zero, always renotify.
   * If null or negative, never renotifies.
   **/
  private Double reNotifyPercentageThreshold;

  /**
   * If the difference between an existing anomaly and a new anomaly on the same time frame
   * is above this threshold, renotify. Combined with the percentage threshold above.
   * Both thresholds have to pass to be renotified.
   * If zero, always renotify.
   * If null or negative, never renotifies.
   **/
  private Double reNotifyAbsoluteThreshold;

  public String getMergeMaxGap() {
    return mergeMaxGap;
  }

  public AnomalyMergerPostProcessorSpec setMergeMaxGap(final String mergeMaxGap) {
    this.mergeMaxGap = mergeMaxGap;
    return this;
  }

  public String getMergeMaxDuration() {
    return mergeMaxDuration;
  }

  public AnomalyMergerPostProcessorSpec setMergeMaxDuration(final String mergeMaxDuration) {
    this.mergeMaxDuration = mergeMaxDuration;
    return this;
  }

  public Long getAlertId() {
    return alertId;
  }

  public AnomalyMergerPostProcessorSpec setAlertId(final Long alertId) {
    this.alertId = alertId;
    return this;
  }

  public AnomalyManager getAnomalyManager() {
    return anomalyManager;
  }

  public AnomalyMergerPostProcessorSpec setAnomalyManager(
      final AnomalyManager anomalyManager) {
    this.anomalyManager = anomalyManager;
    return this;
  }

  public DetectionPipelineUsage getUsage() {
    return usage;
  }

  public AnomalyMergerPostProcessorSpec setUsage(final DetectionPipelineUsage usage) {
    this.usage = usage;
    return this;
  }

  public EnumerationItemDTO getEnumerationItemDTO() {
    return enumerationItemDTO;
  }

  public AnomalyMergerPostProcessorSpec setEnumerationItemDTO(
      final EnumerationItemDTO enumerationItemDTO) {
    this.enumerationItemDTO = enumerationItemDTO;
    return this;
  }

  public Double getReNotifyPercentageThreshold() {
    return reNotifyPercentageThreshold;
  }

  public AnomalyMergerPostProcessorSpec setReNotifyPercentageThreshold(
      final Double reNotifyPercentageThreshold) {
    this.reNotifyPercentageThreshold = reNotifyPercentageThreshold;
    return this;
  }

  public Double getReNotifyAbsoluteThreshold() {
    return reNotifyAbsoluteThreshold;
  }

  public AnomalyMergerPostProcessorSpec setReNotifyAbsoluteThreshold(
      final Double reNotifyAbsoluteThreshold) {
    this.reNotifyAbsoluteThreshold = reNotifyAbsoluteThreshold;
    return this;
  }
}
