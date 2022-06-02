/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import java.time.Duration;
import java.util.List;
import java.util.Set;

public interface TaskManager extends AbstractManager<TaskDTO> {

  List<TaskDTO> findByJobIdStatusNotIn(Long jobId, TaskStatus status);

  List<TaskDTO> findByNameOrderByCreateTime(String name, int fetchSize, boolean asc);

  List<TaskDTO> findByStatusWithinDays(TaskStatus status, int days);

  List<TaskDTO> findByStatusesAndTypeWithinDays(List<TaskStatus> statuses,
      TaskType type, int days);

  List<TaskDTO> findTimeoutTasksWithinDays(int days, long maxTaskTime);

  List<TaskDTO> findByStatusOrderByCreateTime(TaskStatus status, int fetchSize, boolean asc);

  List<TaskDTO> findByStatusAndWorkerId(Long workerId, TaskStatus status);

  boolean updateStatusAndWorkerId(Long workerId, Long id, Set<TaskStatus> allowedOldStatus,
      int expectedVersion);

  void updateStatusAndTaskEndTime(Long id, TaskStatus oldStatus, TaskStatus newStatus,
      Long taskEndTime, String message);

  void updateTaskStartTime(Long id, Long taskStartTime);

  void updateTaskLastActiveTime(Long id, Long taskActiveTime);

  int deleteRecordsOlderThanDaysWithStatus(int days, TaskStatus status);

  void purge(Duration expiryDuration, Integer limitOptional);
}
