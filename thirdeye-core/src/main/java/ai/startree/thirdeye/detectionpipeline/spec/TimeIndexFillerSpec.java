/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.spec;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import java.util.HashMap;
import java.util.Map;

public class TimeIndexFillerSpec extends AbstractSpec {

  /**
   * Inference strategy for the min time constraint.
   */
  private String minTimeInference;

  /**
   * Inference strategy for the max time constraint.
   */
  private String maxTimeInference;

  /**
   * Method to use to fill null values in other columns.
   * Index filling can generate nulls in other columns.
   */
  private String fillNullMethod = "FILL_WITH_ZEROES";

  /**
   * Params for the FillNullMethod.
   */
  private Map<String, Object> fillNullParams = new HashMap<>();

  /**
   * Period Java ISO8601 standard. Used when time inference uses a lookback time.
   * Eg minTime=startTime - lookback
   */
  private String lookback;

  public String getMinTimeInference() {
    return minTimeInference;
  }

  public TimeIndexFillerSpec setMinTimeInference(final String minTimeInference) {
    this.minTimeInference = minTimeInference;
    return this;
  }

  public String getMaxTimeInference() {
    return maxTimeInference;
  }

  public TimeIndexFillerSpec setMaxTimeInference(final String maxTimeInference) {
    this.maxTimeInference = maxTimeInference;
    return this;
  }

  public String getFillNullMethod() {
    return fillNullMethod;
  }

  public TimeIndexFillerSpec setFillNullMethod(final String fillNullMethod) {
    this.fillNullMethod = fillNullMethod;
    return this;
  }

  public Map<String, Object> getFillNullParams() {
    return fillNullParams;
  }

  public TimeIndexFillerSpec setFillNullParams(
      final Map<String, Object> fillNullParams) {
    this.fillNullParams = fillNullParams;
    return this;
  }

  public String getLookback() {
    return lookback;
  }

  public TimeIndexFillerSpec setLookback(final String lookback) {
    this.lookback = lookback;
    return this;
  }
}
