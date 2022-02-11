/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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
