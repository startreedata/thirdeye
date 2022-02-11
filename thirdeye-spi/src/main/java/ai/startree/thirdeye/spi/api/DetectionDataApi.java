/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class DetectionDataApi {

  private Map<String, List> rawData;
  private List<Long> timestamp;
  private List<Double> upperBound;
  private List<Double> lowerBound;
  private List<Double> current;
  private List<Double> expected;

  public List<Long> getTimestamp() {
    return timestamp;
  }

  public DetectionDataApi setTimestamp(final List<Long> timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public List<Double> getUpperBound() {
    return upperBound;
  }

  public DetectionDataApi setUpperBound(final List<Double> upperBound) {
    this.upperBound = upperBound;
    return this;
  }

  public List<Double> getLowerBound() {
    return lowerBound;
  }

  public DetectionDataApi setLowerBound(final List<Double> lowerBound) {
    this.lowerBound = lowerBound;
    return this;
  }

  public List<Double> getCurrent() {
    return current;
  }

  public DetectionDataApi setCurrent(final List<Double> current) {
    this.current = current;
    return this;
  }

  public List<Double> getExpected() {
    return expected;
  }

  public DetectionDataApi setExpected(final List<Double> expected) {
    this.expected = expected;
    return this;
  }

  public Map<String, List> getRawData() {
    return rawData;
  }

  public DetectionDataApi setRawData(final Map<String, List> rawData) {
    this.rawData = rawData;
    return this;
  }
}
