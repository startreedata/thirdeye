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
package ai.startree.thirdeye.spi.datalayer.dto;

public class TaskQuotasConfigurationDTO {

  /**
   * Monthly quota for number of detection task runs
   */
  private Long maximumDetectionTasksPerMonth;
  /**
   * Monthly quota for number of notification task runs
   */
  private Long maximumNotificationTasksPerMonth;

  public Long getMaximumDetectionTasksPerMonth() {
    return maximumDetectionTasksPerMonth;
  }

  public TaskQuotasConfigurationDTO setMaximumDetectionTasksPerMonth(
      final Long maximumDetectionTasksPerMonth) {
    this.maximumDetectionTasksPerMonth = maximumDetectionTasksPerMonth;
    return this;
  }

  public Long getMaximumNotificationTasksPerMonth() {
    return maximumNotificationTasksPerMonth;
  }

  public TaskQuotasConfigurationDTO setMaximumNotificationTasksPerMonth(final Long maximumNotificationTasksPerMonth) {
    this.maximumNotificationTasksPerMonth = maximumNotificationTasksPerMonth;
    return this;
  }
}
