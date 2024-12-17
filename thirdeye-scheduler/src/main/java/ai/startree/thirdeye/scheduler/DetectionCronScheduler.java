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
package ai.startree.thirdeye.scheduler;

import static ai.startree.thirdeye.scheduler.JobUtils.currentCron;
import static ai.startree.thirdeye.scheduler.JobUtils.getIdFromJobKey;
import static ai.startree.thirdeye.spi.Constants.CRON_TIMEZONE;
import static ai.startree.thirdeye.spi.util.ExecutorUtils.shutdownExecutionService;
import static ai.startree.thirdeye.spi.util.TimeUtils.maximumTriggersPerMinute;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.scheduler.job.DetectionPipelineJob;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.quartz.CronScheduleBuilder;
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

  private static final Logger LOG = LoggerFactory.getLogger(DetectionCronScheduler.class);
  public static final String QUARTZ_GROUP = TaskType.DETECTION.toString();
  public static final GroupMatcher<JobKey> GROUP_MATCHER = GroupMatcher.jobGroupEquals(
      QUARTZ_GROUP);
  private static final int DETECTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE = 10;

  private final Scheduler scheduler;
  private final ScheduledExecutorService executorService;
  private final ThirdEyeSchedulerConfiguration configuration;

  private final AlertManager alertManager;

  @Inject
  public DetectionCronScheduler(
      final AlertManager alertManager,
      final ThirdEyeSchedulerConfiguration configuration,
      final GuiceJobFactory guiceJobFactory) {
    try {
      scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.setJobFactory(guiceJobFactory);
    } catch (final SchedulerException e) {
      throw new RuntimeException("Failed to initialize the scheduler", e);
    }
    this.configuration = configuration;
    executorService = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder().setNameFormat("detection-cron-%d").build());
    
    this.alertManager = alertManager;
  }

  public void start() throws SchedulerException {
    scheduler.start();
    executorService.scheduleWithFixedDelay(this,
        0, 
        configuration.getAlertUpdateDelay(), 
        TimeUnit.SECONDS);
  }

  public void shutdown() throws SchedulerException {
    shutdownExecutionService(executorService);
    scheduler.shutdown();
  }

  @Override
  public void run() {
    // catch all exceptions to prevent unscheduling - this is run in executorService.scheduleWithFixedDelay
    try {
      updateSchedules();
    } catch (final Exception e) {
      LOG.error("Error updating detection task creation schedules", e);
    }
  }

  private void updateSchedules() throws SchedulerException {
    // TODO CYRIL scale - loading all entities is expensive - only the id and the cron are necessary - requires custom SQL (JOOQ)
    // FIXME CYRIL SCALE - No need for a strong isolation level here - the default isolation level lock all alerts until the query is finished, blocking progress of tasks and potentially some update/delete operations in the UI.
    //   dirty reads are be fine, this logic runs every minute
    //   also only fetch active entities directly
    final List<AlertDTO> allAlerts = alertManager.findAll();
    
    // schedule active alerts
    allAlerts.forEach(this::schedule);
    
    // cleanup schedules of deleted and deactivated alerts   
    final Map<Long, AlertDTO> idToAlert = allAlerts.stream()
        .collect(Collectors.toMap(AbstractDTO::getId, e -> e));
    final Set<JobKey> scheduledJobKeys = scheduler.getJobKeys(GROUP_MATCHER);
    for (final JobKey jobKey : scheduledJobKeys) {
      try {
        final Long id = getIdFromJobKey(jobKey);
        final AlertDTO detectionDTO = idToAlert.get(id);
        if (detectionDTO == null) {
          LOG.info("Alert with id {} does not exist anymore. Stopping the scheduled detection job.",
              id);
          stopJob(jobKey);
        } else if (!detectionDTO.isActive()) {
          LOG.info("Alert with id {} is deactivated. Stopping the scheduled detection job.", id);
          stopJob(jobKey);
        }
      } catch (final Exception e) {
        LOG.error("Error removing job key {}", jobKey, e);
      }
    }
  }

  private void schedule(final AlertDTO alert) {
    if (!alert.isActive()) {
      LOG.debug("Alert: {} is inactive. Skipping.", alert.getId());
      return;
    }

    // schedule detection job: add or update job
    try {
      final String jobName = TaskType.DETECTION + "_" + alert.getId();
      final JobKey jobKey = new JobKey(jobName, QUARTZ_GROUP);
      if (scheduler.checkExists(jobKey)) {
        LOG.debug("Alert {} is already scheduled", jobKey.getName());
        final String currentCron = currentCron(scheduler, jobKey);
        if (!alert.getCron().equals(currentCron)) {
          LOG.info("Cron expression of alert {} has been changed from {} to {}. "
                  + "Restarting schedule",
              alert.getId(), currentCron, alert.getCron());
          stopJob(jobKey);
          startJob(alert, jobKey);
        }
      } else {
        startJob(alert, jobKey);
      }
    } catch (final Exception e) {
      LOG.error("Error creating/updating job key for detection config {}", alert.getId(), e);
    }
  }

  public void startJob(final AlertDTO config, final JobKey jobKey) throws SchedulerException {
    final Trigger trigger = buildTrigger(config);
    final JobDetail job = JobBuilder.newJob(DetectionPipelineJob.class)
        .withIdentity(jobKey)
        .build();
    scheduler.scheduleJob(job, trigger);
    LOG.info("Scheduled detection job {}", jobKey.getName());
  }

  private void stopJob(final JobKey jobKey) throws SchedulerException {
    if (!scheduler.checkExists(jobKey)) {
      LOG.error("Could not find job to delete {}, {} in the job scheduler. This should never happen. Please reach out to StarTree support.", jobKey.getName(), jobKey.getGroup());
    }
    scheduler.deleteJob(jobKey);
    LOG.info("Stopped job " + jobKey.getName());
  }


  private static Trigger buildTrigger(final AlertDTO config) {
    final String cron = config.getCron();
    final int maxTriggersPerMinute = maximumTriggersPerMinute(cron);
    checkArgument(maxTriggersPerMinute <= DETECTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE,
        "Attempting to schedule a detection job for alert %s that can trigger up to %s times per minute. The limit is %s. Please update the cron %s",
        config.getId(),
        maxTriggersPerMinute, DETECTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE, cron
    );
    final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
        .cronSchedule(cron)
        .inTimeZone(TimeZone.getTimeZone(CRON_TIMEZONE));
    return TriggerBuilder.newTrigger()
        .withSchedule(cronScheduleBuilder)
        .build();
  }
}
