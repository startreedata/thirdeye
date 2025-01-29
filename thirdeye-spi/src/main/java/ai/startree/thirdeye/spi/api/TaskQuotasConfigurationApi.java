/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.spi.api;

import java.util.Objects;

public class TaskQuotasConfigurationApi {

  private Long maximumDetectionTasksPerMonth;
  private Long maximumNotificationTasksPerMonth;

  public Long getMaximumDetectionTasksPerMonth() {
    return maximumDetectionTasksPerMonth;
  }

  public TaskQuotasConfigurationApi setMaximumDetectionTasksPerMonth(
      final Long maximumDetectionTasksPerMonth) {
    this.maximumDetectionTasksPerMonth = maximumDetectionTasksPerMonth;
    return this;
  }

  public Long getMaximumNotificationTasksPerMonth() {
    return maximumNotificationTasksPerMonth;
  }

  public TaskQuotasConfigurationApi setMaximumNotificationTasksPerMonth(
      final Long maximumNotificationTasksPerMonth) {
    this.maximumNotificationTasksPerMonth = maximumNotificationTasksPerMonth;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TaskQuotasConfigurationApi that = (TaskQuotasConfigurationApi) o;
    return Objects.equals(maximumDetectionTasksPerMonth, that.maximumDetectionTasksPerMonth)
        && Objects.equals(maximumNotificationTasksPerMonth,
        that.maximumNotificationTasksPerMonth);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maximumDetectionTasksPerMonth, maximumNotificationTasksPerMonth);
  }
}
