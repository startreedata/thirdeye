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
 */
package org.apache.pinot.thirdeye.detection.components.detectors;

import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.Pattern;

public class HoltWintersDetectorSpec extends AbstractSpec {

  private double alpha = -1;
  private double beta = -1;
  private double gamma = -1;
  private int period = 7;
  private double sensitivity = 5;
  private Pattern pattern = Pattern.UP_OR_DOWN;
  private String weekStart = "WEDNESDAY";
  private Integer lookback = null;

  public double getAlpha() {
    return alpha;
  }

  public HoltWintersDetectorSpec setAlpha(final double alpha) {
    this.alpha = alpha;
    return this;
  }

  public double getBeta() {
    return beta;
  }

  public HoltWintersDetectorSpec setBeta(final double beta) {
    this.beta = beta;
    return this;
  }

  public double getGamma() {
    return gamma;
  }

  public HoltWintersDetectorSpec setGamma(final double gamma) {
    this.gamma = gamma;
    return this;
  }

  public int getPeriod() {
    return period;
  }

  public HoltWintersDetectorSpec setPeriod(final int period) {
    this.period = period;
    return this;
  }

  public double getSensitivity() {
    return sensitivity;
  }

  public HoltWintersDetectorSpec setSensitivity(final double sensitivity) {
    this.sensitivity = sensitivity;
    return this;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public HoltWintersDetectorSpec setPattern(final Pattern pattern) {
    this.pattern = pattern;
    return this;
  }

  public String getWeekStart() {
    return weekStart;
  }

  public HoltWintersDetectorSpec setWeekStart(final String weekStart) {
    this.weekStart = weekStart;
    return this;
  }

  public Integer getLookback() {
    return lookback;
  }

  public HoltWintersDetectorSpec setLookback(final Integer lookback) {
    this.lookback = lookback;
    return this;
  }
}
