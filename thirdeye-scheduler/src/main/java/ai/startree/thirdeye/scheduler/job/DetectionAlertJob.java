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
package ai.startree.thirdeye.scheduler.job;

import static ai.startree.thirdeye.scheduler.JobSchedulerService.getIdFromJobKey;

import ai.startree.thirdeye.scheduler.JobSchedulerService;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.DetectionAlertTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Detection alert job that run by the cron scheduler.
 * This job put notification task into database which can be picked up by works later.
 */
public class DetectionAlertJob extends ThirdEyeAbstractJob {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionAlertJob.class);

  @Override
  public void execute(final JobExecutionContext ctx) {
    final SubscriptionGroupManager alertConfigDAO = getInstance(ctx,
        SubscriptionGroupManager.class);

    final JobSchedulerService service = getInstance(ctx, JobSchedulerService.class);

    final String jobKey = ctx.getJobDetail().getKey().getName();
    final long detectionAlertConfigId = getIdFromJobKey(jobKey);
    final SubscriptionGroupDTO configDTO = alertConfigDAO.findById(detectionAlertConfigId);
    if (configDTO == null) {
      LOG.error("Subscription config {} does not exist", detectionAlertConfigId);
    }

    final DetectionAlertTaskInfo taskInfo = new DetectionAlertTaskInfo(detectionAlertConfigId);

    // if a task is pending and not time out yet, don't schedule more
    final String jobName = String.format("%s_%d", TaskType.NOTIFICATION, detectionAlertConfigId);
    if (service.taskAlreadyRunning(jobName)) {
      LOG.trace("Skip scheduling subscription task {}. Already queued", jobName);
      return;
    }

    if (configDTO != null && !service.needNotification(configDTO)) {
      LOG.trace("Skip scheduling subscription task {}. No anomaly to notify.", jobName);
      return;
    }

    try {
      final TaskManager taskManager = getInstance(ctx, TaskManager.class);
      final TaskDTO taskDTO = taskManager.createTaskDto(detectionAlertConfigId,
          taskInfo,
          TaskType.NOTIFICATION);

      LOG.info("Created {} task {} with settings {}",
          TaskType.NOTIFICATION,
          taskDTO.getId(),
          taskDTO);
    } catch (final JsonProcessingException e) {
      LOG.error("Exception when converting TaskInfo {} to jsonString", taskInfo, e);
    }
  }
}
