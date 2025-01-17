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
package ai.startree.thirdeye.spi.task;

/**
 * Provides finer grained information about the task type.
 * Note: prefix the TaskSubType with the task type.
 * TaskTypes should not share TaskSubTypes.
 * For instance, use NOTIFICATION_MANUAL and DETECTION_MANUAL, not MANUAL. 
 */
public enum TaskSubType {
  DETECTION_HISTORICAL_DATA_AFTER_CREATE,
  DETECTION_HISTORICAL_DATA_AFTER_UPDATE,
  DETECTION_HISTORICAL_DATA_MANUAL,
  DETECTION_TRIGGERED_BY_CRON,
  NOTIFICATION_TRIGGERED_BY_CRON
}
