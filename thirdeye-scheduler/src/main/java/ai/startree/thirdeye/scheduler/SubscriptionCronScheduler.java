/*
 * Copyright 2023 StarTree Inc
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
import static ai.startree.thirdeye.spi.util.ExecutorUtils.shutdownExecutionService;
import static ai.startree.thirdeye.spi.util.TimeUtils.maximumTriggersPerMinute;
import static java.util.stream.Collectors.toList;

import ai.startree.thirdeye.scheduler.job.NotificationPipelineJob;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
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
  private static final String Q_JOB_GROUP = TaskType.NOTIFICATION.toString();
  private static final Duration INTERVAL = Duration.ofMinutes(1);
  // todo cyril make this a config file parameter, and throw when it is not respected
  private static final int SUBSCRIPTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE = 10;

  private final Scheduler scheduler;
  private final ScheduledExecutorService executorService;
  private final SubscriptionGroupManager subscriptionGroupManager;

  @Inject
  public SubscriptionCronScheduler(final SubscriptionGroupManager subscriptionGroupManager) {
    this(subscriptionGroupManager, createScheduler());
  }

  @VisibleForTesting
  SubscriptionCronScheduler(final SubscriptionGroupManager subscriptionGroupManager,
      final Scheduler scheduler) {
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.scheduler = scheduler;
    executorService = createExecutorService();
  }

  private static ScheduledExecutorService createExecutorService() {
    return Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
        .setNameFormat("subscription-scheduler-%d")
        .build());
  }

  private static Scheduler createScheduler() {
    try {
      return StdSchedulerFactory.getDefaultScheduler();
    } catch (final SchedulerException e) {
      throw new RuntimeException("Failed to initialize the scheduler", e);
    }
  }

  private static JobDetail buildJobDetail(final JobKey jobKey) {
    return JobBuilder.newJob(NotificationPipelineJob.class)
        .withIdentity(jobKey)
        .build();
  }

  private static Trigger buildTrigger(final String cron) {
    final int maxTriggersPerMinute = maximumTriggersPerMinute(cron);
    if (maxTriggersPerMinute > SUBSCRIPTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE) {
      LOG.warn(
          "Scheduling a subscription job that can trigger up to {} times per minute. The limit is {}."
              + "This will be forbidden and throw an exception in the future. Please update the cron {}",
          maxTriggersPerMinute, SUBSCRIPTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE, cron);
    }
    return TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule(cron)
            .inTimeZone(TimeZone.getTimeZone(CRON_TIMEZONE)))
        .build();
  }

  @VisibleForTesting
  static JobKey jobKey(final Long id) {
    return new JobKey(String.format("%s_%d", TaskType.NOTIFICATION, id), Q_JOB_GROUP);
  }

  public void addToContext(final String identifier, final Object instance) {
    try {
      scheduler.getContext().put(identifier, instance);
    } catch (final SchedulerException e) {
      throw new RuntimeException("Failed to add to scheduler context", e);
    }
  }

  public void start() throws SchedulerException {
    scheduler.start();
    executorService.scheduleWithFixedDelay(this, 0, INTERVAL.toMinutes(), TimeUnit.MINUTES);
  }

  public void shutdown() throws SchedulerException {
    shutdownExecutionService(executorService);
    scheduler.shutdown();
  }

  @Override
  public void run() {
    try {
      updateScheduledJobs();
    } catch (final Exception e) {
      LOG.error("Error running scheduler", e);
    }
  }

  private void updateScheduledJobs() throws SchedulerException {
    final Set<JobKey> scheduledJobs = getScheduledJobs();
    LOG.info("Scheduled jobs {}", scheduledJobs.stream()
        .map(Key::getName)
        .collect(toList()));

    final List<SubscriptionGroupDTO> subscriptionGroups = subscriptionGroupManager.findAll();
    subscriptionGroups.forEach(sg -> processSubscriptionGroup(sg, scheduledJobs));
    scheduledJobs.forEach(this::deleteIfNotInDatabase);
  }

  @VisibleForTesting
  void processSubscriptionGroup(final SubscriptionGroupDTO sg,
      final Set<JobKey> scheduledJobs) {
    try {
      final Long id = sg.getId();
      final JobKey jobKey = jobKey(id);
      final boolean isScheduled = scheduledJobs.contains(jobKey);
      handleJobScheduling(sg, isScheduled, jobKey);
    } catch (final Exception e) {
      LOG.error("Could not process subscription group ({}): {}", sg.getId(), sg, e);
    }
  }

  private void handleJobScheduling(final SubscriptionGroupDTO sg,
      final boolean isScheduled,
      final JobKey jobKey) throws SchedulerException {
    if (sg.isActive()) {
      if (!isScheduled) {
        startJob(jobKey, sg.getCronExpression());
      } else {
        updateJobIfNecessary(jobKey, sg.getCronExpression());
      }
    } else if (isScheduled) {
      // stop the job if it is no longer active and is scheduled
      stopJob(jobKey);
    }
  }

  private void startJob(final JobKey jobKey, final String cron)
      throws SchedulerException {
    final JobDetail job = buildJobDetail(jobKey);
    final Trigger trigger = buildTrigger(cron);
    scheduler.scheduleJob(job, trigger);
    LOG.info("Started job: {}", jobKey);
  }

  private void updateJobIfNecessary(final JobKey jobKey, final String cron)
      throws SchedulerException {
    final List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
    if (!triggers.isEmpty()) {
      final CronTrigger cronTrigger = (CronTrigger) triggers.get(0);
      final String currentCron = cronTrigger.getCronExpression();
      if (!currentCron.equals(cron)) {
        stopJob(jobKey);
        startJob(jobKey, cron);
        LOG.info("Updated job: {}", jobKey);
      }
    }
  }

  @VisibleForTesting
  void deleteIfNotInDatabase(final JobKey jobKey) {
    try {
      final Long id = getIdFromJobKey(jobKey.getName());
      if (subscriptionGroupManager.findById(id) == null) {
        stopJob(jobKey);
        LOG.info("Deleted job not in database: {}", jobKey);
      }
    } catch (final SchedulerException e) {
      LOG.error("Failed to delete job '{}'", jobKey, e);
    }
  }

  private void stopJob(final JobKey jobKey) throws SchedulerException {
    scheduler.deleteJob(jobKey);
    LOG.info("Stopped job: {}", jobKey);
  }

  private Set<JobKey> getScheduledJobs() throws SchedulerException {
    return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Q_JOB_GROUP));
  }
}
