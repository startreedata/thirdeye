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
  private Long DetectionTaskQuota;
  /**
   * Monthly quota for number of notification task runs
   */
  private Long NotificationTaskQuota;

  public Long getDetectionTaskQuota() {
    return DetectionTaskQuota;
  }

  public TaskQuotasConfigurationDTO setDetectionTaskQuota(final Long detectionTaskQuota) {
    this.DetectionTaskQuota = detectionTaskQuota;
    return this;
  }

  public Long getNotificationTaskQuota() {
    return NotificationTaskQuota;
  }

  public TaskQuotasConfigurationDTO setNotificationTaskQuota(final Long notificationTaskQuota) {
    this.NotificationTaskQuota = notificationTaskQuota;
    return this;
  }
}
