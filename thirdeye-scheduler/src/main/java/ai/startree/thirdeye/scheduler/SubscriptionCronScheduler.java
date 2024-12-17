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

import ai.startree.thirdeye.scheduler.job.NotificationPipelineJob;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
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

/**
 * The Subscription group scheduler. Schedule, update, delete subscription group quartz jobs.
 * The scheduled jobs create tasks.
 */
@Singleton
public class SubscriptionCronScheduler implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(SubscriptionCronScheduler.class);
  private static final String QUARTZ_GROUP = TaskType.NOTIFICATION.toString();
  public static final GroupMatcher<JobKey> GROUP_MATCHER = GroupMatcher.jobGroupEquals(
      QUARTZ_GROUP);
  private static final int SUBSCRIPTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE = 10;

  private final Scheduler scheduler;
  private final ScheduledExecutorService executorService;
  private final SubscriptionGroupManager subscriptionGroupManager;

  private final ThirdEyeSchedulerConfiguration configuration;

  @Inject
  public SubscriptionCronScheduler(
      final SubscriptionGroupManager subscriptionGroupManager,
      final ThirdEyeSchedulerConfiguration configuration,
      final GuiceJobFactory guiceJobFactory) {
    try {
      scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.setJobFactory(guiceJobFactory);
    } catch (final SchedulerException e) {
      throw new RuntimeException("Failed to initialize the scheduler", e);
    }
    this.configuration = configuration;
    executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
        .setNameFormat("subscription-scheduler-%d")
        .build());
    
    this.subscriptionGroupManager = subscriptionGroupManager;
  }

  public void start() throws SchedulerException {
    scheduler.start();
    executorService.scheduleWithFixedDelay(this,
        0,
        configuration.getSubscriptionGroupUpdateDelay(),
        TimeUnit.SECONDS);
  }

  public void shutdown() throws SchedulerException {
    shutdownExecutionService(executorService);
    scheduler.shutdown();
  }

  @Override
  public void run() {
    try {
      updateNotificationSchedules();
    } catch (final Exception e) {
      LOG.error("Error running scheduler", e);
    }
  }

  private void updateNotificationSchedules() throws SchedulerException {
    final List<SubscriptionGroupDTO> allSubscriptionGroups = subscriptionGroupManager.findAll();
    
    // schedule active subscription groups
    allSubscriptionGroups.forEach(this::scheduleSubscriptionGroup);
    
    // cleanup schedules of deleted and deactivated subscription groups
    final Map<Long, SubscriptionGroupDTO> idToSubscriptionGroup = allSubscriptionGroups.stream()
        .collect(Collectors.toMap(AbstractDTO::getId, e -> e));
    final Set<JobKey> scheduledJobKeys = scheduler.getJobKeys(GROUP_MATCHER);
    for (final JobKey jobKey : scheduledJobKeys) {
      try {
        final Long id = getIdFromJobKey(jobKey);
        final SubscriptionGroupDTO subscriptionGroup = idToSubscriptionGroup.get(id);
        if (subscriptionGroup == null) {
          LOG.info("Subscription Group with id {} does not exist anymore. Stopping the scheduled subscription group job.",
              id);
          stopJob(jobKey);
        } else if (!subscriptionGroup.isActive()) {
          LOG.info("Subscription group with id {} is deactivated. Stopping the scheduled subscription group job.", id);
          stopJob(jobKey);
        }
      } catch (final Exception e) {
        LOG.error("Error removing job key {}", jobKey, e);
      }
    }
  }

  private void scheduleSubscriptionGroup(final SubscriptionGroupDTO subscriptionGroup) {
    if (!subscriptionGroup.isActive()) {
      LOG.debug("Subscription Group: {} is inactive. Skipping.", subscriptionGroup.getId());
      return;
    }

    // schedule detection job: add or update job
    try {
      final String jobName = TaskType.NOTIFICATION + "_" + subscriptionGroup.getId();
      final JobKey jobKey = new JobKey(jobName, QUARTZ_GROUP);
      if (scheduler.checkExists(jobKey)) {
        LOG.debug("Subscription group {} is already scheduled", jobKey.getName());
        final String currentCron = currentCron(scheduler, jobKey);
        if (!subscriptionGroup.getCronExpression().equals(currentCron)) {
          LOG.info("Cron expression of subscription group {} has been changed from {} to {}. "
                  + "Restarting schedule",
              subscriptionGroup.getId(), currentCron, subscriptionGroup.getCronExpression());
          stopJob(jobKey);
          startJob(subscriptionGroup, jobKey);
        }
      } else {
        startJob(subscriptionGroup, jobKey);
      }
    } catch (final Exception e) {
      LOG.error("Error creating/updating job key for subscription group {}", subscriptionGroup.getId(), e);
    }
  }

  private void startJob(final SubscriptionGroupDTO config, final JobKey jobKey)
      throws SchedulerException {
    final Trigger trigger = buildTrigger(config);
    final JobDetail job = JobBuilder.newJob(NotificationPipelineJob.class)
        .withIdentity(jobKey)
        .build();
    scheduler.scheduleJob(job, trigger);
    LOG.info("Scheduled notification job {}", jobKey);
  }

  private void stopJob(final JobKey jobKey) throws SchedulerException {
    if (!scheduler.checkExists(jobKey)) {
      LOG.error("Could not find job to delete {}, {} in the job scheduler. This should never happen. Please reach out to StarTree support.", jobKey.getName(), jobKey.getGroup());
    }
    scheduler.deleteJob(jobKey);
    LOG.info("Stopped job " + jobKey.getName());
  }


  private static Trigger buildTrigger(final SubscriptionGroupDTO config) {
    final String cron = config.getCronExpression();
    final int maxTriggersPerMinute = maximumTriggersPerMinute(cron);
    checkArgument(maxTriggersPerMinute <= SUBSCRIPTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE,
        "Attempting to schedule a notification job for subscription group %s that can trigger up to %s times per minute. The limit is %s. Please update the cron %s",
        config.getId(),
        maxTriggersPerMinute, SUBSCRIPTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE, cron
    );
    final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
        .cronSchedule(cron)
        .inTimeZone(TimeZone.getTimeZone(CRON_TIMEZONE));
    return TriggerBuilder.newTrigger()
        .withSchedule(cronScheduleBuilder)
        .build();
  }
}
