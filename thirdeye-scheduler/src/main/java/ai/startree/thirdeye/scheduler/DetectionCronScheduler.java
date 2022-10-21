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
package ai.startree.thirdeye.scheduler;

import static ai.startree.thirdeye.scheduler.JobSchedulerService.getIdFromJobKey;
import static ai.startree.thirdeye.spi.Constants.CRON_TIMEZONE;

import ai.startree.thirdeye.scheduler.job.DetectionPipelineJob;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionCronScheduler implements Runnable {

  public static final TimeUnit ALERT_DELAY_UNIT = TimeUnit.SECONDS;
  public static final String QUARTZ_DETECTION_GROUPER = TaskType.DETECTION.toString();

  private static final Logger LOG = LoggerFactory.getLogger(DetectionCronScheduler.class);

  private final AlertManager alertManager;
  private final Scheduler scheduler;
  private final ScheduledExecutorService executorService;
  private final int alertDelay;

  @Inject
  public DetectionCronScheduler(final ThirdEyeSchedulerConfiguration thirdEyeSchedulerConfiguration, final AlertManager alertManager) {
    this.alertManager = alertManager;
    this.alertDelay = thirdEyeSchedulerConfiguration.getAlertUpdateDelay();
    executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("detection-cron-%d").build());
    try {
      scheduler = StdSchedulerFactory.getDefaultScheduler();
    } catch (final SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  public void addToContext(final String identifier, final Object instance) {
    try {
      scheduler.getContext().put(identifier, instance);
    } catch (final SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  public void start() throws SchedulerException {
    scheduler.start();
    executorService
        .scheduleWithFixedDelay(this, 0, alertDelay, ALERT_DELAY_UNIT);
  }

  public void run() {
    try {
      alertManager.findAll().forEach(this::processAlert);
      processScheduledJobs();
    } catch (final SchedulerException e) {
      LOG.error("Error while scheduling detection pipeline", e);
    }
  }

  private void processAlert(final AlertDTO alert) {
    if (!alert.isActive()) {
      LOG.debug("Alert: " + alert.getId() + " is inactive. Skipping.");
      return;
    }

    // add or update
    try {
      // Schedule detection jobs
      final String jobKeyString = getJobKey(alert.getId(), TaskType.DETECTION);
      final JobKey alertJobKey = new JobKey(jobKeyString, QUARTZ_DETECTION_GROUPER);
      final JobDetail detectionJob = JobBuilder.newJob(DetectionPipelineJob.class)
          .withIdentity(alertJobKey)
          .build();
      if (scheduler.checkExists(alertJobKey)) {
        LOG.info(String.format("Alert %s is already scheduled for detection",
            alertJobKey.getName()));

        if (isJobUpdated(alert, alertJobKey)) {
          restartJob(alert, detectionJob);
        }
      } else {
        startJob(alert, detectionJob);
      }
    } catch (final Exception e) {
      LOG.error("Error creating/updating job key for detection config {}", alert.getId());
    }
  }

  private void processScheduledJobs() throws SchedulerException {
    final Set<JobKey> scheduledJobs = getScheduledJobs();
    for (final JobKey jobKey : scheduledJobs) {
      try {
        final Long id = getIdFromJobKey(jobKey.getName());
        final AlertDTO detectionDTO = alertManager.findById(id);
        if (detectionDTO == null) {
          LOG.info("Found a scheduled detection config task, but not found in the database {}",
              id);
          stopJob(jobKey);
        } else if (!detectionDTO.isActive()) {
          LOG.info("Found a scheduled detection config task, but has been deactivated {}", id);
          stopJob(jobKey);
        }
      } catch (final Exception e) {
        LOG.error("Error removing job key {}", jobKey);
      }
    }
  }

  private void restartJob(final AlertDTO config, final JobDetail job) throws SchedulerException {
    stopJob(job.getKey());
    startJob(config, job);
  }

  public Set<JobKey> getScheduledJobs() throws SchedulerException {
    return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(QUARTZ_DETECTION_GROUPER));
  }

  public void shutdown() throws SchedulerException {
    ThirdEyeUtils.shutdownExecutionService(executorService);
    scheduler.shutdown();
  }

  public void startJob(final AbstractDTO config, final JobDetail job) throws SchedulerException {
    final String cron = ((AlertDTO) config).getCron();
    final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
        .cronSchedule(cron)
        .inTimeZone(TimeZone.getTimeZone(CRON_TIMEZONE));

    final Trigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(cronScheduleBuilder)
        .build();
    scheduler.scheduleJob(job, trigger);
    LOG.info(String.format("scheduled detection pipeline job %s", job.getKey().getName()));
  }

  public void stopJob(final JobKey jobKey) throws SchedulerException {
    if (!scheduler.checkExists(jobKey)) {
      throw new IllegalStateException(
          "Cannot stop detection pipeline " + jobKey.getName() + ", it has not been scheduled");
    }
    scheduler.deleteJob(jobKey);
    LOG.info("Stopped detection pipeline {}", jobKey.getName());
  }

  public String getJobKey(final Long id, final TaskType taskType) {
    return String.format("%s_%d", taskType, id);
  }

  @SuppressWarnings("unchecked")
  private boolean isJobUpdated(final AlertDTO config, final JobKey key) throws SchedulerException {
    final List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(key);
    final CronTrigger cronTrigger = (CronTrigger) triggers.get(0);
    final String cronInSchedule = cronTrigger.getCronExpression();

    if (!config.getCron().equals(cronInSchedule)) {
      LOG.info("Cron expression for detection pipeline {} has been changed from {}  to {}. "
              + "Restarting schedule",
          config.getId(), cronInSchedule, config.getCron());
      return true;
    }
    return false;
  }
}
