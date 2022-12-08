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
package ai.startree.thirdeye.plugins.postprocessor;

import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.PostProcessorSpec;

public class AnomalyMergerPostProcessorSpec extends PostProcessorSpec {

  /**
   * ISO-8601 format. Max gap between to anomalies for the anomalies to be merged.
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
  private MergedAnomalyResultManager mergedAnomalyResultManager;

  /**
   * Set by the detection pipeline at runtime.
   */
  private DetectionPipelineUsage usage;

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

  public MergedAnomalyResultManager getMergedAnomalyResultManager() {
    return mergedAnomalyResultManager;
  }

  public AnomalyMergerPostProcessorSpec setMergedAnomalyResultManager(
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
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
}
