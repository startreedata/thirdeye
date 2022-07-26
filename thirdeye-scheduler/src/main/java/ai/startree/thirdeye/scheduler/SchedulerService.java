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

import ai.startree.thirdeye.detection.anomaly.detection.trigger.DataAvailabilityEventListenerDriver;
import ai.startree.thirdeye.scheduler.autoonboard.AutoOnboardConfiguration;
import ai.startree.thirdeye.scheduler.autoonboard.AutoOnboardService;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoader;
import ai.startree.thirdeye.scheduler.events.HolidayEventsLoaderConfiguration;
import ai.startree.thirdeye.scheduler.modeldownload.ModelDownloaderManager;
import ai.startree.thirdeye.scheduler.monitor.MonitorJobScheduler;
import ai.startree.thirdeye.scheduler.monitor.TaskCleanUpConfiguration;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.task.TaskDriverConfiguration;
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
  private final AutoOnboardConfiguration autoOnboardConfiguration;
  private final TaskDriverConfiguration taskDriverConfiguration;
  private final MonitorJobScheduler monitorJobScheduler;
  private final AutoOnboardService autoOnboardService;
  private final HolidayEventsLoader holidayEventsLoader;
  private final DetectionCronScheduler detectionScheduler;
  private final DataAvailabilityEventListenerDriver dataAvailabilityEventListenerDriver;
  private final ModelDownloaderManager modelDownloaderManager;
  private final DataAvailabilityTaskScheduler dataAvailabilityTaskScheduler;
  private final SubscriptionCronScheduler subscriptionScheduler;
  private final TaskManager taskManager;

  private final ScheduledExecutorService executorService;

  @Inject
  public SchedulerService(final ThirdEyeSchedulerConfiguration config,
      final HolidayEventsLoaderConfiguration holidayEventsLoaderConfiguration,
      final AutoOnboardConfiguration autoOnboardConfiguration,
      final TaskDriverConfiguration taskDriverConfiguration,
      final MonitorJobScheduler monitorJobScheduler,
      final AutoOnboardService autoOnboardService,
      final HolidayEventsLoader holidayEventsLoader,
      final DetectionCronScheduler detectionScheduler,
      final DataAvailabilityEventListenerDriver dataAvailabilityEventListenerDriver,
      final ModelDownloaderManager modelDownloaderManager,
      final DataAvailabilityTaskScheduler dataAvailabilityTaskScheduler,
      final SubscriptionCronScheduler subscriptionScheduler,
      final TaskManager taskManager) {
    this.config = config;
    this.holidayEventsLoaderConfiguration = holidayEventsLoaderConfiguration;
    this.autoOnboardConfiguration = autoOnboardConfiguration;
    this.taskDriverConfiguration = taskDriverConfiguration;
    this.monitorJobScheduler = monitorJobScheduler;
    this.autoOnboardService = autoOnboardService;
    this.holidayEventsLoader = holidayEventsLoader;
    this.detectionScheduler = detectionScheduler;
    this.dataAvailabilityEventListenerDriver = dataAvailabilityEventListenerDriver;
    this.modelDownloaderManager = modelDownloaderManager;
    this.dataAvailabilityTaskScheduler = dataAvailabilityTaskScheduler;
    this.subscriptionScheduler = subscriptionScheduler;
    this.taskManager = taskManager;

    executorService = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
  }

  @Override
  public void start() throws Exception {
    if (config.isMonitor()) {
      monitorJobScheduler.start();
    }

    if (autoOnboardConfiguration.isEnabled()) {
      autoOnboardService.start();
    }

    if (holidayEventsLoaderConfiguration.isEnabled()) {
      holidayEventsLoader.start();
    }
    if (config.isDetectionPipeline()) {
      detectionScheduler.start();
    }
    if (config.isDetectionAlert()) {
      subscriptionScheduler.start();
    }
    if (config.isDataAvailabilityEventListener()) {
      dataAvailabilityEventListenerDriver.start();
    }
    if (config.isDataAvailabilityTaskScheduler()) {
      dataAvailabilityTaskScheduler.start();
    }
    if (config.getModelDownloaderConfigs() != null) {
      modelDownloaderManager.start();
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

    if (monitorJobScheduler != null) {
      monitorJobScheduler.shutdown();
    }
    if (holidayEventsLoaderConfiguration.isEnabled()) {
      holidayEventsLoader.shutdown();
    }
    if (autoOnboardConfiguration.isEnabled()) {
      autoOnboardService.shutdown();
    }
    if (detectionScheduler != null) {
      detectionScheduler.shutdown();
    }
    if (config.isDetectionAlert()) {
      subscriptionScheduler.shutdown();
    }
    if (dataAvailabilityEventListenerDriver != null) {
      dataAvailabilityEventListenerDriver.shutdown();
    }
    if (config.isDataAvailabilityTaskScheduler()) {
      dataAvailabilityTaskScheduler.shutdown();
    }
    if (modelDownloaderManager != null) {
      modelDownloaderManager.shutdown();
    }
  }
}
