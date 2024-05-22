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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;

public interface TaskManager extends AbstractManager<TaskDTO> {

  TaskDTO createTaskDto(final TaskInfo taskInfo, final TaskType taskType, final
  AuthorizationConfigurationDTO auth)
      throws JsonProcessingException;

  TaskDTO findNextTaskToRun();

  boolean isAlreadyRunning(final String taskName);
  
  boolean acquireTaskToRun(TaskDTO taskDTO, final long workerId);

  // fixme authz required ?
  List<TaskDTO> findByStatusAndWorkerId(Long workerId, TaskStatus status);

  void updateStatusAndTaskEndTime(Long id, TaskStatus oldStatus, TaskStatus newStatus,
      Long taskEndTime, String message);

  void updateTaskStartTime(Long id, Long taskStartTime);

  void updateLastActive(Long id);

  void purge(Duration expiryDuration, Integer limitOptional);

  void orphanTaskCleanUp(Timestamp activeThreshold);

  // fixme authz required ?
  long countByStatus(final TaskStatus status);
}
