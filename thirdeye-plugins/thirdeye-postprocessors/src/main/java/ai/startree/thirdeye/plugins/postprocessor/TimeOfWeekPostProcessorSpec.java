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
import java.util.List;
import java.util.Map;

public class TimeOfWeekPostProcessorSpec extends PostProcessorSpec {

  private List<String> daysOfWeek;

  private List<Integer> hoursOfDay;

  private Map<String, List<Integer>> dayHoursOfWeek;

  public List<String> getDaysOfWeek() {
    return daysOfWeek;
  }

  public TimeOfWeekPostProcessorSpec setDaysOfWeek(final List<String> daysOfWeek) {
    this.daysOfWeek = daysOfWeek;
    return this;
  }

  public List<Integer> getHoursOfDay() {
    return hoursOfDay;
  }

  public TimeOfWeekPostProcessorSpec setHoursOfDay(final List<Integer> hoursOfDay) {
    this.hoursOfDay = hoursOfDay;
    return this;
  }

  public Map<String, List<Integer>> getDayHoursOfWeek() {
    return dayHoursOfWeek;
  }

  public TimeOfWeekPostProcessorSpec setDayHoursOfWeek(
      final Map<String, List<Integer>> dayHoursOfWeek) {
    this.dayHoursOfWeek = dayHoursOfWeek;
    return this;
  }
}
