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

public class AbsoluteChangeRuleDetectorSpec extends AbstractSpec {

  private double absoluteChange = Double.NaN;
  private String offset = "wo1w";
  private String pattern = "UP_OR_DOWN";

  public double getAbsoluteChange() {
    return absoluteChange;
  }

  public AbsoluteChangeRuleDetectorSpec setAbsoluteChange(final double absoluteChange) {
    this.absoluteChange = absoluteChange;
    return this;
  }

  public String getOffset() {
    return offset;
  }

  public AbsoluteChangeRuleDetectorSpec setOffset(final String offset) {
    this.offset = offset;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  public AbsoluteChangeRuleDetectorSpec setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }
}
