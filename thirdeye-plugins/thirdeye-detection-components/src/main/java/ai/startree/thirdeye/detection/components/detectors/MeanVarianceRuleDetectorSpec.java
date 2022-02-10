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

package ai.startree.thirdeye.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.Pattern;

public class MeanVarianceRuleDetectorSpec extends AbstractSpec {

  private int lookback = 52; //default look back of 52 units
  private double sensitivity = 5; //default sensitivity of 5, equals +/- 1 sigma
  private Pattern pattern = Pattern.UP_OR_DOWN;

  public int getLookback() {
    return lookback;
  }

  public MeanVarianceRuleDetectorSpec setLookback(final int lookback) {
    this.lookback = lookback;
    return this;
  }

  public double getSensitivity() {
    return sensitivity;
  }

  public MeanVarianceRuleDetectorSpec setSensitivity(final double sensitivity) {
    this.sensitivity = sensitivity;
    return this;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public MeanVarianceRuleDetectorSpec setPattern(
      final Pattern pattern) {
    this.pattern = pattern;
    return this;
  }
}
