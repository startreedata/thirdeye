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

import ai.startree.thirdeye.scheduler.events.HolidayEventsLoader;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoaderConfiguration;
import ai.startree.thirdeye.scheduler.taskcleanup.TaskCleanUpConfiguration;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.worker.task.TaskDriverConfiguration;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SchedulerService implements Managed {

  public static final int CORE_POOL_SIZE = 8;
  private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

  private final ThirdEyeSchedulerConfiguration config;
  private final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration;
  private final TaskDriverConfiguration taskDriverConfiguration;
  private final HolidayEventsLoader holidayEventsLoader;
  private final DetectionCronScheduler detectionScheduler;
  private final SubscriptionCronScheduler subscriptionScheduler;
  private final TaskManager taskManager;

  private final ScheduledExecutorService executorService;

  @Inject
  public SchedulerService(final ThirdEyeSchedulerConfiguration config,
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration,
      final TaskDriverConfiguration taskDriverConfiguration,
      final HolidayEventsLoader holidayEventsLoader,
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

    executorService = Executors.newScheduledThreadPool(CORE_POOL_SIZE,
        new ThreadFactoryBuilder().setNameFormat("scheduler-service-%d").build());
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

    // TODO spyne improve scheduler arch and localize
    // TODO spyne explore: consolidate all orphan maintenance tasks in a single pool
    scheduleTaskCleanUp(config.getTaskCleanUpConfiguration());
    if (taskDriverConfiguration.isRandomWorkerIdEnabled()) {
      scheduleOrphanTaskCleanUp(config.getTaskCleanUpConfiguration());
    }
  }

  private void scheduleTaskCleanUp(final TaskCleanUpConfiguration config) {
    executorService.scheduleWithFixedDelay(() -> cleanTasks(config),
        1,
        config.getIntervalInMinutes(),
        TimeUnit.MINUTES);
  }

  private void cleanTasks(final TaskCleanUpConfiguration config) {
    // try catch is important to not throw exceptions while running in the scheduler.
    try {
      taskManager.purge(
          Duration.ofDays(config.getRetentionInDays()),
          config.getMaxEntriesToDelete());
    } catch (Exception e) {
      // catching exceptions only. errors will be escalated.
      LOG.error("Error occurred during task purge", e);
    }
  }

  private void scheduleOrphanTaskCleanUp(final TaskCleanUpConfiguration config) {
    executorService.scheduleWithFixedDelay(this::handleOrphanTasks,
        0,
        config.getOrphanIntervalInSeconds(),
        TimeUnit.SECONDS);
  }

  private void handleOrphanTasks() {
    final Timestamp activeThreshold = new Timestamp(System.currentTimeMillis() - getActiveBuffer());
    taskManager.orphanTaskCleanUp(activeThreshold);
  }

  private long getActiveBuffer() {
    return taskDriverConfiguration.getActiveThresholdMultiplier()
        * taskDriverConfiguration.getHeartbeatInterval().toMillis();
  }

  @Override
  public void stop() throws Exception {
    executorService.shutdown();
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
