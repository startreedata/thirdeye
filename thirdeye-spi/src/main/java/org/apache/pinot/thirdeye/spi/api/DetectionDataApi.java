/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.spi.api;

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
