/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.scheduler.ThirdEyeAbstractJob;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.DetectionPipelineTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectionPipelineJob extends ThirdEyeAbstractJob {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineJob.class);
  private static final long DETECTION_TASK_TIMEOUT = TimeUnit.DAYS.toMillis(1);

  @Override
  public void execute(JobExecutionContext ctx) {
    final DetectionPipelineTaskInfo taskInfo = buildTaskInfo(ctx,
        getInstance(ctx, AlertManager.class));

    if (taskInfo == null) {
      // Possible if the alert has been deleted, the task has no use.
      return;
    }

    final TaskManager taskDAO = getInstance(ctx, TaskManager.class);

    // if a task is pending and not time out yet, don't schedule more
    String jobName = String.format("%s_%d", TaskType.DETECTION, taskInfo.getConfigId());
    if (taskAlreadyRunning(jobName, taskInfo, taskDAO)) {
      LOG.info(
          "Skip scheduling detection task for {} with start time {} and end time {}. Task is already in the queue.",
          jobName,
          taskInfo.getStart(),
          taskInfo.getEnd());
      return;
    }

    try {
      TaskUtils.createTask(taskDAO, taskInfo.getConfigId(), taskInfo, TaskType.DETECTION);
    } catch (JsonProcessingException e) {
      LOG.error("Exception when converting DetectionPipelineTaskInfo {} to jsonString",
          taskInfo,
          e);
    }
  }

  private static DetectionPipelineTaskInfo buildTaskInfo(JobExecutionContext jobExecutionContext,
      final AlertManager alertDAO) {
    final JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
    final Long id = TaskUtils.getIdFromJobKey(jobKey.getName());
    final AlertDTO alert = alertDAO.findById(id);

    if (alert == null) {
      // no task needs to be created if alert does not exist!
      return null;
    }

    // start and end are corrected with delay and granularity at execution time
    long start = alert.getLastTimestamp();
    long end = jobExecutionContext.getScheduledFireTime().getTime();
    return new DetectionPipelineTaskInfo(alert.getId(), start, end);
  }

  private static boolean taskAlreadyRunning(String jobName, DetectionPipelineTaskInfo taskInfo,
      final TaskManager taskDAO) {
    // check if a task for this detection pipeline is already scheduled
    List<TaskDTO> scheduledTasks = taskDAO
        .findByPredicate(Predicate.AND(
                Predicate.EQ("name", jobName),
                Predicate.OR(
                    Predicate.EQ("status", TaskStatus.RUNNING.toString()),
                    Predicate.EQ("status", TaskStatus.WAITING.toString()))
            )
        );

    List<DetectionPipelineTaskInfo> scheduledTaskInfos = scheduledTasks.stream().map(taskDTO -> {
      try {
        return OBJECT_MAPPER.readValue(taskDTO.getTaskInfo(), DetectionPipelineTaskInfo.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toList());
    Optional<DetectionPipelineTaskInfo> latestScheduledTask = scheduledTaskInfos.stream()
        .reduce((taskInfo1, taskInfo2) -> taskInfo1.getEnd() > taskInfo2.getEnd() ? taskInfo1
            : taskInfo2);
    return latestScheduledTask.isPresent()
        && taskInfo.getEnd() - latestScheduledTask.get().getEnd()
        < DetectionPipelineJob.DETECTION_TASK_TIMEOUT;
  }
}


