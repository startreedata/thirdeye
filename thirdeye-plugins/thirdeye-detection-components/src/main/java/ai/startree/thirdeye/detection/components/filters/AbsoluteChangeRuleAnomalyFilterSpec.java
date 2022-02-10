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

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class AbsoluteChangeRuleAnomalyFilterSpec extends AbstractSpec {

  private double threshold = Double.NaN;
  private String offset;
  private String pattern = "UP_OR_DOWN";

  public double getThreshold() {
    return threshold;
  }

  public AbsoluteChangeRuleAnomalyFilterSpec setThreshold(final double threshold) {
    this.threshold = threshold;
    return this;
  }

  public String getOffset() {
    return offset;
  }

  public AbsoluteChangeRuleAnomalyFilterSpec setOffset(final String offset) {
    this.offset = offset;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  public AbsoluteChangeRuleAnomalyFilterSpec setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }
}
