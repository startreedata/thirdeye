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

public class ThresholdPostProcessorSpec {

  private Boolean ignore;
  private Double min;
  private Double max;
  /**
   * The name/description of the value used for thresholding.
   * Used in the label.
   */
  private String valueName;

  public Double getMin() {
    return min;
  }

  public ThresholdPostProcessorSpec setMin(final Double min) {
    this.min = min;
    return this;
  }

  public Double getMax() {
    return max;
  }

  public ThresholdPostProcessorSpec setMax(final Double max) {
    this.max = max;
    return this;
  }

  public String getValueName() {
    return valueName;
  }

  public ThresholdPostProcessorSpec setValueName(final String valueName) {
    this.valueName = valueName;
    return this;
  }

  public Boolean getIgnore() {
    return ignore;
  }

  public ThresholdPostProcessorSpec setIgnore(final Boolean ignore) {
    this.ignore = ignore;
    return this;
  }
}
