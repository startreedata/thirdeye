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

import static ai.startree.thirdeye.scheduler.JobUtils.BACKPRESSURE_COUNTERS;
import static ai.startree.thirdeye.scheduler.JobUtils.FAILED_TASK_CREATION_COUNTERS;
import static ai.startree.thirdeye.scheduler.JobUtils.getIdFromJobKey;
import static ai.startree.thirdeye.spi.Constants.DEFAULT_CHRONOLOGY;
import static ai.startree.thirdeye.spi.task.TaskType.DETECTION;
import static ai.startree.thirdeye.spi.util.AlertMetadataUtils.getDateTimeZone;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.scheduler.JobUtils;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.spi.util.TimeUtils;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectionPipelineJob implements Job {

  private static final Interval UNUSED_DETECTION_INTERVAL = new Interval(0, 0, DEFAULT_CHRONOLOGY);
  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineJob.class);

  @Override
  public void execute(JobExecutionContext ctx) {
    try {
      final JobKey jobKey = ctx.getJobDetail().getKey();
      final Long alertId = getIdFromJobKey(jobKey);
      final AlertManager alertManager = JobUtils.getInstance(ctx, AlertManager.class);
      final AlertDTO alert = alertManager.findById(alertId);
      if (alert == null) {
        // possible if the alert was deleted - no need to run the task
        LOG.warn(
            "Alert with id {} not found. Alert was deleted? Skipping detection job scheduling.",
            alertId);
        return;
      }
      final long endTime = ctx.getScheduledFireTime().getTime();
      final long start = computeTaskStart(ctx, alert, endTime);
      final DetectionPipelineTaskInfo taskInfo = new DetectionPipelineTaskInfo(alert.getId(), start,
          endTime);

      // if a task is pending and not time out yet, don't schedule more
      final String jobName = jobKey.getName();
      final TaskManager taskManager = JobUtils.getInstance(ctx, TaskManager.class);
      if (taskManager.isAlreadyRunning(jobName)) {
        LOG.warn(
            "Skipped scheduling detection task for {} with start time {} and end time {}. A task for the same entity is already in the queue.",
            jobName,
            taskInfo.getStart(),
            taskInfo.getEnd());
        BACKPRESSURE_COUNTERS.get(DETECTION).increment();
        return;
      }
      final TaskDTO taskDTO = taskManager.createTaskDto(taskInfo, TaskType.DETECTION,
          alert.getAuth());
      LOG.info("Created {} task {} with settings {}", TaskType.DETECTION, taskDTO.getId(),
          taskDTO);
    } catch (Exception e) {
      LOG.error(
          "Exception running detection pipeline job {}. Detection task will not be scheduled.",
          ctx.getJobDetail().getKey().getName(), e);
      FAILED_TASK_CREATION_COUNTERS.get(DETECTION).increment();
    }
  }

  @VisibleForTesting
  protected long computeTaskStart(final JobExecutionContext ctx, final AlertDTO alert,
      final long endTime) {
    try {
      final AlertTemplateRenderer alertTemplateRenderer = JobUtils.getInstance(ctx,
          AlertTemplateRenderer.class);
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alert,
          UNUSED_DETECTION_INTERVAL);
      final Chronology chronology = getDateTimeZone(templateWithProperties.getMetadata());
      final DateTime defaultStartTime = new DateTime(alert.getLastTimestamp(), chronology);
      final DateTime endDateTime = new DateTime(endTime, chronology);
      final Period mutabilityPeriod = getMutabilityPeriod(templateWithProperties);
      final DateTime mutabilityStart = endDateTime.minus(mutabilityPeriod);
      if (mutabilityStart.isBefore(defaultStartTime)) {
        LOG.info(
            "Applied mutability period of {} for alert id {} between {} and {}. Corrected task interval is between {} and {}",
            mutabilityPeriod,
            alert.getId(),
            defaultStartTime,
            endDateTime,
            mutabilityStart,
            endDateTime
        );
        return mutabilityStart.getMillis();
      } else {
        return defaultStartTime.getMillis();
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  private static Period getMutabilityPeriod(final AlertTemplateDTO templateWithProperties) {
    return optional(templateWithProperties.getMetadata())
        .map(AlertMetadataDTO::getDataset)
        .map(DatasetConfigDTO::getMutabilityPeriod)
        .map(TimeUtils::isoPeriod)
        .orElse(Period.ZERO);
  }
}


