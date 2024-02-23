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

import static ai.startree.thirdeye.scheduler.job.ThirdEyeAbstractJob.BACKPRESSURE_COUNTERS;
import static ai.startree.thirdeye.spi.task.TaskType.NOTIFICATION;

import ai.startree.thirdeye.scheduler.JobSchedulerService;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.worker.task.DetectionAlertTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NotificationPipelineTaskCreator {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationPipelineTaskCreator.class);

  private final JobSchedulerService jobSchedulerService;
  private final TaskManager taskManager;
  

  @Inject
  public NotificationPipelineTaskCreator(final JobSchedulerService jobSchedulerService,
      final TaskManager taskManager) {
    this.jobSchedulerService = jobSchedulerService;
    this.taskManager = taskManager;
  }

  public void createTask(final long subscriptionGroupId) {
    // TODO spyne this name creation logic should be managed by the task manager
    final String taskJobName = String.format("%s_%d", NOTIFICATION, subscriptionGroupId);
    if (jobSchedulerService.taskAlreadyRunning(taskJobName)) {
      LOG.warn("Skipped scheduling notification task for {}. A task for the same entity is already in the queue.",
          taskJobName);
      BACKPRESSURE_COUNTERS.get(NOTIFICATION).increment();
      return;
    }

    try {
      createTask0(subscriptionGroupId);
    } catch (final JsonProcessingException e) {
      LOG.error("Exception in Json Serialization in taskInfo for subscription group: {}",
          subscriptionGroupId,
          e);
    }
  }

  private void createTask0(final long subscriptionGroupId) throws JsonProcessingException {
    final DetectionAlertTaskInfo taskInfo = new DetectionAlertTaskInfo(subscriptionGroupId);
    final TaskDTO t = taskManager.createTaskDto(subscriptionGroupId, taskInfo, NOTIFICATION);
    LOG.info("Created {} task {}. taskInfo: {}", NOTIFICATION, t.getId(), t);
  }
}
