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
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.PostProcessorSpec;

public class AnomalyMergerSpec extends PostProcessorSpec {

  /**
   * ISO-8601 format. Max gap between to anomalies for the anomalies to be merged.
   */
  private String mergeMaxGap;
  /**
   * ISO-8601 format. Max duration of a merger of anomalies.
   */
  private String mergeMaxDuration;

  /**
   * Expected to be set at detection pipeline runtime.
   */
  private Long alertId;

  /**
   * Expected to be set at detection pipeline runtime.
   */
  private MergedAnomalyResultManager mergedAnomalyResultManager;

  /**
   * Expected to be set at detection pipeline runtime.
   */
  private DetectionPipelineUsage usage;

  public String getMergeMaxGap() {
    return mergeMaxGap;
  }

  public AnomalyMergerSpec setMergeMaxGap(final String mergeMaxGap) {
    this.mergeMaxGap = mergeMaxGap;
    return this;
  }

  public String getMergeMaxDuration() {
    return mergeMaxDuration;
  }

  public AnomalyMergerSpec setMergeMaxDuration(final String mergeMaxDuration) {
    this.mergeMaxDuration = mergeMaxDuration;
    return this;
  }

  public Long getAlertId() {
    return alertId;
  }

  public AnomalyMergerSpec setAlertId(final Long alertId) {
    this.alertId = alertId;
    return this;
  }

  public MergedAnomalyResultManager getMergedAnomalyResultManager() {
    return mergedAnomalyResultManager;
  }

  public AnomalyMergerSpec setMergedAnomalyResultManager(
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    return this;
  }

  public DetectionPipelineUsage getUsage() {
    return usage;
  }

  public AnomalyMergerSpec setUsage(final DetectionPipelineUsage usage) {
    this.usage = usage;
    return this;
  }
}
