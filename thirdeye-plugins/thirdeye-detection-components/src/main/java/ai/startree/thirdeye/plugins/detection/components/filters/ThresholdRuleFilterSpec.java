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
package ai.startree.thirdeye.plugins.detection.components.filters;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class ThresholdRuleFilterSpec extends AbstractSpec {

  private double minValueHourly = Double.NaN;
  private double minValueDaily = Double.NaN;
  private double maxValueHourly = Double.NaN;
  private double maxValueDaily = Double.NaN;
  private double minValue = Double.NaN;
  private double maxValue = Double.NaN;

  public double getMinValueHourly() {
    return minValueHourly;
  }

  public void setMinValueHourly(double minValueHourly) {
    this.minValueHourly = minValueHourly;
  }

  public double getMinValueDaily() {
    return minValueDaily;
  }

  public void setMinValueDaily(double minValueDaily) {
    this.minValueDaily = minValueDaily;
  }

  public double getMaxValueHourly() {
    return maxValueHourly;
  }

  public void setMaxValueHourly(double maxValueHourly) {
    this.maxValueHourly = maxValueHourly;
  }

  public double getMaxValueDaily() {
    return maxValueDaily;
  }

  public void setMaxValueDaily(double maxValueDaily) {
    this.maxValueDaily = maxValueDaily;
  }

  public double getMinValue() {
    return minValue;
  }

  public void setMinValue(double minValue) {
    this.minValue = minValue;
  }

  public double getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(double maxValue) {
    this.maxValue = maxValue;
  }
}
