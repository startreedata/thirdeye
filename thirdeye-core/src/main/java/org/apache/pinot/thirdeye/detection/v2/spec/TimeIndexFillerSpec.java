package org.apache.pinot.thirdeye.detection.v2.spec;

import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.components.filler.TimeIndexFiller.TimeLimitInferenceStrategy;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;

public class TimeIndexFillerSpec extends AbstractSpec {

  /**
   * Inference strategy for the min time constraint.
   * Default is to look at the first value of the time index.
   */
  private String minTimeInference = TimeLimitInferenceStrategy.FROM_DATA.toString();

  /**
   * Inference strategy for the max time constraint.
   * Default is to consider the endTime of the detection.
   */
  private String maxTimeInference = TimeLimitInferenceStrategy.FROM_DETECTION_TIME.toString();

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
  private String lookback = "";

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
