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
  private final TaskManager anomalyTaskDAO;
  private final MonitorConfiguration monitorConfiguration;
  private final JobManager jobManager;
  private MonitorJobRunner monitorJobRunner;
  private MonitorJobContext monitorJobContext;

  @Inject
  public MonitorJobScheduler(MonitorConfiguration monitorConfiguration,
      final TaskManager taskManager,
      final JobManager jobManager) {
    this.anomalyTaskDAO = taskManager;
    this.monitorConfiguration = monitorConfiguration;
    this.jobManager = jobManager;

    scheduledExecutorService = Executors.newScheduledThreadPool(10);
  }

  public void start() {
    LOG.info("Starting monitor service");

    monitorJobContext = new MonitorJobContext();
    monitorJobContext.setTaskDAO(anomalyTaskDAO);
    monitorJobContext.setMonitorConfiguration(monitorConfiguration);
    monitorJobContext.setJobDAO(jobManager);

    monitorJobRunner = new MonitorJobRunner(monitorJobContext);
    scheduledExecutorService
        .scheduleWithFixedDelay(monitorJobRunner, 0,
            monitorConfiguration.getMonitorFrequency().getSize(),
            monitorConfiguration.getMonitorFrequency().getUnit());
  }

  public void shutdown() {
    LOG.info("Stopping monitor service");
    AnomalyUtils.safelyShutdownExecutionService(scheduledExecutorService, this.getClass());
  }
}
