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

import static ai.startree.thirdeye.spi.Constants.METRICS_TIMER_PERCENTILES;
import static ai.startree.thirdeye.spi.util.MetricsUtils.record;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.scheduler.events.HolidayEventsLoaderConfiguration;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoaderScheduler;
import ai.startree.thirdeye.scheduler.taskcleanup.TaskCleanUpConfiguration;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.worker.task.TaskDriverConfiguration;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SchedulerService implements Managed {

  private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

  private static final String PURGE_OLD_TASKS_TIMER_NAME = "thirdeye_old_tasks_purge";
  private static final String PURGE_OLD_TASKS_TIMER_DESCRIPTION = "Start: triggered by schedule, no input. End: the old tasks deletion logic is finished. Tag exception=true means an exception was thrown by the method call.";
  private static final Timer purgeOldTasksTimerOfSuccess = Timer.builder(PURGE_OLD_TASKS_TIMER_NAME)
      .description(PURGE_OLD_TASKS_TIMER_DESCRIPTION)
      .publishPercentiles(METRICS_TIMER_PERCENTILES)
      .tag("exception", "false")
      .register(Metrics.globalRegistry);
  private static final Timer purgeOldTasksTimerOfException = Timer.builder(PURGE_OLD_TASKS_TIMER_NAME)
      .description(PURGE_OLD_TASKS_TIMER_DESCRIPTION)
      .publishPercentiles(METRICS_TIMER_PERCENTILES)
      .tag("exception", "true")
      .register(Metrics.globalRegistry);

  private static final String ORPHAN_TASKS_CLEANUP_TIMER_NAME = "thirdeye_orphan_tasks_cleanup";
  private static final String ORPHAN_TASKS_CLEANUP_TIMER_DESCRIPTION = "Start: triggered by schedule, no input. End: the orphan tasks cleanup logic is finished. Tag exception=true means an exception was thrown by the method call.";
  private static final Timer handleOrphanTasksTimerOfSuccess = Timer.builder(ORPHAN_TASKS_CLEANUP_TIMER_NAME)
      .description(ORPHAN_TASKS_CLEANUP_TIMER_DESCRIPTION)
      .publishPercentiles(METRICS_TIMER_PERCENTILES)
      .tag("exception", "false")
      .register(Metrics.globalRegistry);
  private static final Timer handleOrphanTasksTimerOfException = Timer.builder(ORPHAN_TASKS_CLEANUP_TIMER_NAME)
      .description(ORPHAN_TASKS_CLEANUP_TIMER_DESCRIPTION)
      .publishPercentiles(METRICS_TIMER_PERCENTILES)
      .tag("exception", "true")
      .register(Metrics.globalRegistry);
  

  private final ThirdEyeSchedulerConfiguration config;
  private final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration;
  private final TaskDriverConfiguration taskDriverConfiguration;
  private final HolidayEventsLoaderScheduler holidayEventsLoader;
  private final DetectionCronScheduler detectionScheduler;
  private final SubscriptionCronScheduler subscriptionScheduler;
  private final TaskManager taskManager;

  private final ScheduledExecutorService oldTasksExecutorService;
  private final ScheduledExecutorService orphanTasksExecutorService;

  @Inject
  public SchedulerService(final ThirdEyeSchedulerConfiguration config,
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration,
      final TaskDriverConfiguration taskDriverConfiguration,
      final HolidayEventsLoaderScheduler holidayEventsLoader,
      final DetectionCronScheduler detectionScheduler,
      final SubscriptionCronScheduler subscriptionScheduler,
      final TaskManager taskManager) {
    this.config = config;
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    this.taskDriverConfiguration = taskDriverConfiguration;
    this.holidayEventsLoader = holidayEventsLoader;
    this.detectionScheduler = detectionScheduler;
    this.subscriptionScheduler = subscriptionScheduler;
    this.taskManager = taskManager;

    oldTasksExecutorService = Executors.newScheduledThreadPool(1,
        new ThreadFactoryBuilder().setNameFormat("scheduler-old-tasks-purge-service-%d").build());
    if (taskDriverConfiguration.isRandomWorkerIdEnabled()) {
      orphanTasksExecutorService = Executors.newScheduledThreadPool(1,
          new ThreadFactoryBuilder().setNameFormat("scheduler-orphan-tasks-cleanup-service-%d")
              .build());
    } else {
      // no need to reserve a thread
      orphanTasksExecutorService = null;
    }
  }

  @Override
  public void start() throws Exception {
    if (holidayEventsLoaderConfiguration.isEnabled()) {
      holidayEventsLoader.start();
    }
    if (config.isDetectionPipeline()) {
      detectionScheduler.start();
    }
    if (config.isDetectionAlert()) {
      subscriptionScheduler.start();
    }

    // schedule task maintenance operations
    final TaskCleanUpConfiguration taskCleanUpConfiguration = config.getTaskCleanUpConfiguration();
    if (taskCleanUpConfiguration.getIntervalInMinutes() > 0) {
      oldTasksExecutorService.scheduleWithFixedDelay(this::purgeOldTasks,
          1,
          taskCleanUpConfiguration.getIntervalInMinutes(),
          TimeUnit.MINUTES);
    }
    
    if (taskDriverConfiguration.isRandomWorkerIdEnabled() && taskCleanUpConfiguration.getOrphanIntervalInSeconds() > 0) {
      orphanTasksExecutorService.scheduleWithFixedDelay(this::handleOrphanTasks,
          0,
          taskCleanUpConfiguration.getOrphanIntervalInSeconds(),
          TimeUnit.SECONDS);
    }
  }

  private void purgeOldTasks() {
    // try catch is important to not throw exceptions while running in the scheduler.
    try {
      record(
          () -> {
            taskManager.purge(
                Duration.ofDays(config.getTaskCleanUpConfiguration().getRetentionInDays()),
                config.getTaskCleanUpConfiguration().getMaxEntriesToDelete());
          },
          purgeOldTasksTimerOfSuccess,
          purgeOldTasksTimerOfException);
      LOG.debug("Old task purge performed successfully.");
    } catch (Exception e) {
      // catching exceptions only. Errors will be escalated.
      LOG.error("Failed to purge old tasks.", e);
    }
  }

  private void handleOrphanTasks() {
    // try catch is important to not throw exceptions while running in the scheduler.
    try {
      record(
          () -> {
            final long activeBuffer = taskDriverConfiguration.getActiveThresholdMultiplier()
                * taskDriverConfiguration.getHeartbeatInterval().toMillis();
            final Timestamp activeThreshold = new Timestamp(System.currentTimeMillis() - activeBuffer);
            taskManager.cleanupOrphanTasks(activeThreshold);
          },
          handleOrphanTasksTimerOfSuccess,
          handleOrphanTasksTimerOfException
      );
      LOG.debug("Orphan tasks handling performed successfully.");
    } catch (Exception e) {
      // catching exceptions only. Errors will be escalated.
      LOG.error("Failed to handle orphan tasks.", e);
    }
  }

  @Override
  public void stop() throws Exception {
    oldTasksExecutorService.shutdown();
    optional(orphanTasksExecutorService).ifPresent(ExecutorService::shutdown);
    if (holidayEventsLoaderConfiguration.isEnabled()) {
      holidayEventsLoader.shutdown();
    }
    if (detectionScheduler != null) {
      detectionScheduler.shutdown();
    }
    if (config.isDetectionAlert()) {
      subscriptionScheduler.shutdown();
    }
  }
}
