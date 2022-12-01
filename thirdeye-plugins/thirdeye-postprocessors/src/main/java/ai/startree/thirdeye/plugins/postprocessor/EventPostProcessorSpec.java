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
package ai.startree.thirdeye.plugins.postprocessor;

import ai.startree.thirdeye.spi.detection.PostProcessorSpec;

public class EventPostProcessorSpec extends PostProcessorSpec {

  /**
   * A period added before the event that corresponds to a period that is also impacted
   * by the event.
   * Eg if beforeEventMargin is P1D, if event happens on [Dec 24 0:00, Dec 25 0:00[
   * with beforeEventMargin the postProcessor will apply on [Dec 23 0:00 and Dec 25 0:00[
   **/
  private String beforeEventMargin;

  /**Same as above after the event.*/
  private String afterEventMargin;

  public String getBeforeEventMargin() {
    return beforeEventMargin;
  }

  public EventPostProcessorSpec setBeforeEventMargin(final String beforeEventMargin) {
    this.beforeEventMargin = beforeEventMargin;
    return this;
  }

  public String getAfterEventMargin() {
    return afterEventMargin;
  }

  public EventPostProcessorSpec setAfterEventMargin(final String afterEventMargin) {
    this.afterEventMargin = afterEventMargin;
    return this;
  }
}
