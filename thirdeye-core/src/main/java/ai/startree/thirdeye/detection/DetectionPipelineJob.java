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
package ai.startree.thirdeye.detection;

import static ai.startree.thirdeye.detection.TaskUtils.createTaskDto;

import ai.startree.thirdeye.scheduler.JobSchedulerService;
import ai.startree.thirdeye.scheduler.ThirdEyeAbstractJob;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.DetectionPipelineTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.TimeUnit;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectionPipelineJob extends ThirdEyeAbstractJob {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineJob.class);
  private static final long DETECTION_TASK_TIMEOUT = TimeUnit.DAYS.toMillis(1);

  @Override
  public void execute(JobExecutionContext ctx) {
    final JobSchedulerService service = getInstance(ctx, JobSchedulerService.class);
    final DetectionPipelineTaskInfo taskInfo = service.buildTaskInfo(ctx.getJobDetail().getKey(),
        ctx.getScheduledFireTime().getTime());

    if (taskInfo == null) {
      // Possible if the alert has been deleted, the task has no use.
      return;
    }

    // if a task is pending and not time out yet, don't schedule more
    String jobName = String.format("%s_%d", TaskType.DETECTION, taskInfo.getConfigId());
//    String jobName = ctx.getJobDetail().getKey().getName();
    if (service.taskAlreadyRunning(jobName)) {
      LOG.info(
          "Skip scheduling detection task for {} with start time {} and end time {}. Task is already in the queue.",
          jobName,
          taskInfo.getStart(),
          taskInfo.getEnd());
      return;
    }

    try {
      final TaskDTO taskDTO = createTaskDto(taskInfo.getConfigId(), taskInfo, TaskType.DETECTION);
      final long taskId = service.saveTask(taskDTO);
      LOG.info("Created {} task {} with settings {}", TaskType.DETECTION, taskId, taskDTO);
    } catch (JsonProcessingException e) {
      LOG.error("Exception when converting DetectionPipelineTaskInfo {} to jsonString",
          taskInfo,
          e);
    }
  }
}


