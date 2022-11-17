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
   * A period added before the holiday that corresponds to a period that is also impacted
   * by the holiday.
   * Eg if spillover is P1D, if event happens on [Dec 24 0:00, Dec 25 0:00[
   * with spillover the postProcessor will apply on [Dec 23 0:00 and Dec 25 0:00[
   **/
  private String beforeHolidayMargin;

  /**Same as above after the holiday.*/
  private String afterHolidayMargin;

  public String getBeforeHolidayMargin() {
    return beforeHolidayMargin;
  }

  public EventPostProcessorSpec setBeforeHolidayMargin(final String beforeHolidayMargin) {
    this.beforeHolidayMargin = beforeHolidayMargin;
    return this;
  }

  public String getAfterHolidayMargin() {
    return afterHolidayMargin;
  }

  public EventPostProcessorSpec setAfterHolidayMargin(final String afterHolidayMargin) {
    this.afterHolidayMargin = afterHolidayMargin;
    return this;
  }
}
