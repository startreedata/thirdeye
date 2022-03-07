/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.scheduler.ThirdEyeAbstractJob;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.DetectionPipelineTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.TimeUnit;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectionPipelineJob extends ThirdEyeAbstractJob {

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
    String jobName = String.format("%s_%d", TaskType.DETECTION,
        taskInfo.getConfigId());
    if (TaskUtils.checkTaskAlreadyRun(jobName, taskInfo, DETECTION_TASK_TIMEOUT, taskDAO)) {
      LOG.info(
          "Skip scheduling detection task for {} with start time {}. Task is already in the queue.",
          jobName,
          taskInfo.getStart());
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

  public static DetectionPipelineTaskInfo buildTaskInfo(JobExecutionContext jobExecutionContext,
      final AlertManager alertDAO) {
    final JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
    final Long id = TaskUtils.getIdFromJobKey(jobKey.getName());
    final AlertDTO alert = alertDAO.findById(id);

    if (alert == null) {
      // no task needs to be created if alert does not exist!
      return null;
    }

    return new DetectionPipelineTaskInfo(alert.getId(),
        alert.getLastTimestamp(), jobExecutionContext.getScheduledFireTime().getTime());
  }
}


