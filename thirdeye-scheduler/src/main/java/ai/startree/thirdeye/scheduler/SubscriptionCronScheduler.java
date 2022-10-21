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

import ai.startree.thirdeye.scheduler.job.DetectionAlertJob;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
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
import java.util.stream.Collectors;
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
import org.quartz.utils.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Detection alert scheduler. Schedule new detection alert jobs or update existing detection
 * alert jobs
 * in the cron scheduler.
 */
@Singleton
public class SubscriptionCronScheduler implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(SubscriptionCronScheduler.class);
  private static final String QUARTZ_SUBSCRIPTION_GROUPER = TaskType.NOTIFICATION
      .toString();
  private static final int DEFAULT_ALERT_DELAY = 1;
  private static final TimeUnit DEFAULT_ALERT_DELAY_UNIT = TimeUnit.MINUTES;

  private final Scheduler scheduler;
  private final ScheduledExecutorService scheduledExecutorService;
  private final SubscriptionGroupManager subscriptionGroupManager;

  @Inject
  public SubscriptionCronScheduler(final SubscriptionGroupManager subscriptionGroupManager) {
    this.subscriptionGroupManager = subscriptionGroupManager;
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(
        "subscription-scheduler-%d").build());
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
    scheduledExecutorService
        .scheduleWithFixedDelay(this, 0, DEFAULT_ALERT_DELAY, DEFAULT_ALERT_DELAY_UNIT);
  }

  @Override
  public void run() {
    try {
      // read all alert configs
      LOG.info("Scheduling all the subscription configs");
      final List<SubscriptionGroupDTO> alertConfigs = subscriptionGroupManager.findAll();

      // get active jobs
      final Set<JobKey> scheduledJobs = getScheduledJobs();
      LOG.info("Scheduled jobs {}",
          scheduledJobs.stream().map(Key::getName).collect(Collectors.toList()));

      for (final SubscriptionGroupDTO alertConfig : alertConfigs) {
        try {
          createOrUpdateAlertJob(scheduledJobs, alertConfig);
        } catch (final Exception e) {
          LOG.error("Could not write job for alert config id {}. Skipping. {}", alertConfig.getId(),
              alertConfig, e);
        }
      }

      // for any scheduled jobs, not having a function in the database,
      // stop the schedule, as function has been deleted
      for (final JobKey scheduledJobKey : scheduledJobs) {
        try {
          deleteAlertJob(scheduledJobKey);
        } catch (final Exception e) {
          LOG.error("Could not delete alert job '{}'. Skipping.", scheduledJobKey, e);
        }
      }
    } catch (final Exception e) {
      LOG.error("Error running scheduler", e);
    }
  }

  public Set<JobKey> getScheduledJobs() throws SchedulerException {
    return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(QUARTZ_SUBSCRIPTION_GROUPER));
  }

  public void shutdown() throws SchedulerException {
    ThirdEyeUtils.shutdownExecutionService(scheduledExecutorService);
    scheduler.shutdown();
  }

  public void startJob(final AbstractDTO config, final JobDetail job) throws SchedulerException {
    final Trigger trigger = TriggerBuilder.newTrigger().withSchedule(
            CronScheduleBuilder.cronSchedule(((SubscriptionGroupDTO) config).getCronExpression())
                .inTimeZone(TimeZone.getTimeZone(CRON_TIMEZONE)))
        .build();
    scheduler.scheduleJob(job, trigger);
    LOG.info(String.format("scheduled subscription pipeline job %s", job.getKey().getName()));
  }

  public void stopJob(final JobKey key) throws SchedulerException {
    if (!scheduler.checkExists(key)) {
      throw new IllegalStateException(
          "Cannot stop alert config " + key + ", it has not been scheduled");
    }
    scheduler.deleteJob(key);
    LOG.info("Stopped alert config {}", key);
  }

  public String getJobKey(final Long id, final TaskType taskType) {
    return String.format("%s_%d", taskType, id);
  }

  private void deleteAlertJob(final JobKey scheduledJobKey) throws SchedulerException {
    final Long configId = getIdFromJobKey(scheduledJobKey.getName());
    final SubscriptionGroupDTO alertConfigSpec = subscriptionGroupManager.findById(configId);
    if (alertConfigSpec == null) {
      LOG.info("Found scheduled, but not in database {}", configId);
      stopJob(scheduledJobKey);
    }
  }

  private void createOrUpdateAlertJob(final Set<JobKey> scheduledJobs,
      final SubscriptionGroupDTO subscriptionGroupDTO)
      throws SchedulerException {
    final Long id = subscriptionGroupDTO.getId();
    final boolean isActive = subscriptionGroupDTO.isActive();

    final JobKey key = new JobKey(getJobKey(id, TaskType.NOTIFICATION),
        QUARTZ_SUBSCRIPTION_GROUPER);
    final JobDetail job = JobBuilder.newJob(DetectionAlertJob.class).withIdentity(key).build();
    final boolean isScheduled = scheduledJobs.contains(key);

    if (isActive) {
      if (isScheduled) {
        final String cronInDatabase = subscriptionGroupDTO.getCronExpression();

        final List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(key);
        final CronTrigger cronTrigger = (CronTrigger) triggers.get(0);
        final String cronInSchedule = cronTrigger.getCronExpression();
        // cron expression has been updated, restart this job
        if (!cronInDatabase.equals(cronInSchedule)) {
          LOG.info(
              "Cron expression for config {} with jobKey {} has been changed from {}  to {}. "
                  + "Restarting schedule",
              id, key, cronInSchedule, cronInDatabase);
          stopJob(key);
          startJob(subscriptionGroupDTO, job);
        }
      } else {
        LOG.info("Found active but not scheduled {}", id);
        startJob(subscriptionGroupDTO, job);
      }
    } else {
      if (isScheduled) {
        LOG.info("Found inactive but scheduled {}", id);
        stopJob(key);
      }
      // for all jobs with not isActive, and not isScheduled, no change required
    }
  }
}
