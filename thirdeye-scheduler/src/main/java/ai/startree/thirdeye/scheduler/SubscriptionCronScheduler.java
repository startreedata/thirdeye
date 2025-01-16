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

import static ai.startree.thirdeye.spi.util.ExecutorUtils.shutdownExecutionService;

import ai.startree.thirdeye.scheduler.job.NotificationPipelineJob;
import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.task.TaskType;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.quartz.SchedulerException;

/**
 * The Subscription group scheduler. Schedule, update, delete subscription group quartz jobs.
 * The scheduled jobs create tasks.
 */
@Singleton
public class SubscriptionCronScheduler {

  private static final int SUBSCRIPTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE = 10;

  private final ScheduledExecutorService executorService;
  private final ThirdEyeSchedulerConfiguration configuration;
  private final GuiceJobFactory guiceJobFactory;

  private final SubscriptionGroupManager subscriptionGroupManager;
  private TaskCronSchedulerRunnable<SubscriptionGroupDTO> runnable;

  private final TaskManager taskManager;
  private final NamespaceConfigurationManager namespaceConfigurationManager;

  @Inject
  public SubscriptionCronScheduler(
      final SubscriptionGroupManager subscriptionGroupManager,
      final ThirdEyeSchedulerConfiguration configuration,
      final GuiceJobFactory guiceJobFactory,
      final TaskManager taskManager,
      final NamespaceConfigurationManager namespaceConfigurationManager) {
    this.configuration = configuration;
    this.guiceJobFactory = guiceJobFactory;
    executorService = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder().setNameFormat("subscription-scheduler-%d").build());
    
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.taskManager = taskManager;
    this.namespaceConfigurationManager = namespaceConfigurationManager;
  }

  public void start() throws SchedulerException {
    runnable = new TaskCronSchedulerRunnable<>(
        subscriptionGroupManager,
        SubscriptionGroupDTO::getCronExpression,
        SubscriptionGroupDTO::isActive,
        SubscriptionGroupDTO.class,
        TaskType.NOTIFICATION,
        NotificationPipelineJob.class,
        guiceJobFactory,
        SUBSCRIPTION_SCHEDULER_CRON_MAX_TRIGGERS_PER_MINUTE,
        this.getClass(),
        taskManager,
        namespaceConfigurationManager
    );
    executorService.scheduleWithFixedDelay(runnable,
        0,
        configuration.getSubscriptionGroupUpdateDelay(),
        TimeUnit.SECONDS);
  }

  public void shutdown() throws SchedulerException {
    shutdownExecutionService(executorService);
    if (runnable != null) {
      runnable.shutdown();
    }
  }
}
