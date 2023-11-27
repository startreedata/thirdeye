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
package ai.startree.thirdeye.scheduler.monitor;

import static ai.startree.thirdeye.spi.util.ExecutorUtils.shutdownExecutionService;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    scheduledExecutorService = Executors.newScheduledThreadPool(10,
        new ThreadFactoryBuilder().setNameFormat("monitor-job-scheduler-%d").build());
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
        isoPeriod(monitorConfiguration.getMonitorFrequency()).toStandardDuration().getMillis(),
        TimeUnit.MILLISECONDS);
  }

  public void shutdown() {
    LOG.info("Stopping monitor service");
    shutdownExecutionService(scheduledExecutorService);
  }
}
