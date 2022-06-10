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
package ai.startree.thirdeye.util;

import org.joda.time.Period;

public class AnomalyOffset {

  Period preOffsetPeriod;
  Period postOffsetPeriod;

  public AnomalyOffset(Period preOffsetPeriod, Period postOffsetPeriod) {
    this.preOffsetPeriod = preOffsetPeriod;
    this.postOffsetPeriod = postOffsetPeriod;
  }

  public Period getPreOffsetPeriod() {
    return preOffsetPeriod;
  }

  public void setPreOffsetPeriod(Period preOffsetPeriod) {
    this.preOffsetPeriod = preOffsetPeriod;
  }

  public Period getPostOffsetPeriod() {
    return postOffsetPeriod;
  }

  public void setPostOffsetPeriod(Period postOffsetPeriod) {
    this.postOffsetPeriod = postOffsetPeriod;
  }
}
