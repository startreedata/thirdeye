/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.monitor;

import ai.startree.thirdeye.detection.anomaly.utils.AnomalyUtils;
import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MonitorJobScheduler {

  private static final Logger LOG = LoggerFactory.getLogger(MonitorJobScheduler.class);
  private final ScheduledExecutorService scheduledExecutorService;
  private final MonitorConfiguration monitorConfiguration;
  private final TaskManager taskManager;
  private final JobManager jobManager;

  @Inject
  public MonitorJobScheduler(final MonitorConfiguration monitorConfiguration,
      final TaskManager taskManager,
      final JobManager jobManager) {
    this.taskManager = taskManager;
    this.monitorConfiguration = monitorConfiguration;
    this.jobManager = jobManager;

    scheduledExecutorService = Executors.newScheduledThreadPool(10);
  }

  public void start() {
    LOG.info("Starting monitor service");

    final MonitorJobContext monitorJobContext = new MonitorJobContext();
    monitorJobContext.setTaskDAO(taskManager);
    monitorJobContext.setMonitorConfiguration(monitorConfiguration);
    monitorJobContext.setJobDAO(jobManager);

    final MonitorJobRunnable monitorJobRunnable = new MonitorJobRunnable(monitorJobContext);
    scheduledExecutorService.scheduleWithFixedDelay(monitorJobRunnable,
        0,
        monitorConfiguration.getMonitorFrequency().getSize(),
        monitorConfiguration.getMonitorFrequency().getUnit());
  }

  public void shutdown() {
    LOG.info("Stopping monitor service");
    AnomalyUtils.safelyShutdownExecutionService(scheduledExecutorService, getClass());
  }
}
