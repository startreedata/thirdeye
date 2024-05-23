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
package ai.startree.thirdeye.scheduler.job;

import static ai.startree.thirdeye.scheduler.JobUtils.getIdFromJobKey;
import static ai.startree.thirdeye.spi.task.TaskType.NOTIFICATION;

import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.worker.task.DetectionAlertTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Detection alert job that run by the cron scheduler.
 * This job put notification task into database which can be picked up by works later.
 */
public class NotificationPipelineJob extends ThirdEyeAbstractJob {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationPipelineJob.class);

  @Override
  public void execute(final JobExecutionContext ctx) {
    try {
      final JobKey jobKey = ctx.getJobDetail().getKey();
      final long subscriptionGroupId = getIdFromJobKey(jobKey);
      final SubscriptionGroupManager subscriptionGroupManager = getInstance(ctx, SubscriptionGroupManager.class);
      final SubscriptionGroupDTO subscriptionGroup = subscriptionGroupManager.findById(subscriptionGroupId);
      if (subscriptionGroup == null) {
        // possible if the subscription group was deleted - no need to run the task
        LOG.warn("Subscription group with id {} not found. Subscription group was deleted? Skipping notification job scheduling.", subscriptionGroupId);
        return;
      }
      final DetectionAlertTaskInfo taskInfo = new DetectionAlertTaskInfo(subscriptionGroupId);
      
      final String jobName = jobKey.getName();
      final TaskManager taskManager = getInstance(ctx, TaskManager.class);
      if (taskManager.isAlreadyRunning(jobName)) {
        LOG.warn("Skipped scheduling notification task for {}. A task for the same entity is already in the queue.",
            jobName);
        BACKPRESSURE_COUNTERS.get(NOTIFICATION).increment();
        return;
      }
      try {
        final TaskDTO t = taskManager.createTaskDto(taskInfo, NOTIFICATION, subscriptionGroup.getAuth());
        LOG.info("Created {} task {}. taskInfo: {}", NOTIFICATION, t.getId(), t);
      } catch (final JsonProcessingException e) {
        LOG.error("Exception in Json Serialization in taskInfo for subscription group: {}",
            subscriptionGroupId,
            e);
      }
    } catch (Exception e) {
      // Catch all exception to avoid job being stuck in the scheduler.
      // todo cyril - not sure if it is really necessary to catch here - a job failing 
      LOG.error("Exception running notification pipeline job {}. Notification task will not be scheduled.",  ctx.getJobDetail().getKey().getName(), e);
    }
  }
}
