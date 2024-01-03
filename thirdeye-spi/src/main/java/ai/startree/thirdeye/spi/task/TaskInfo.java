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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface for task info of the various types of tasks
 */
public interface TaskInfo {

  /**
   * refId is the id of the entity that the task is associated with.
   * For example, if the task is to run a detection pipeline, the refId is the id of the alert. In
   * case of a notification task, the refId is the id of the subscription group.
   * @return the id of the reference entity
   */
  @JsonIgnore
  default Long getRefId() {
    return null;
  }
}
