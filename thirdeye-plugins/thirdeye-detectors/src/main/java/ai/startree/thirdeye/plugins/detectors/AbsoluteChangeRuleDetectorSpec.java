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
package ai.startree.thirdeye.plugins.detectors;

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
