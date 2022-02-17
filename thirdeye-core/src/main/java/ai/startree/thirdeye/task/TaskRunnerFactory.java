/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.runner.DetectionPipelineTaskRunner;
import ai.startree.thirdeye.task.runner.MonitorTaskRunner;
import ai.startree.thirdeye.task.runner.NotificationTaskRunner;
import ai.startree.thirdeye.task.runner.OnboardingTaskRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TaskRunnerFactory {

  private final DetectionPipelineTaskRunner detectionPipelineTaskRunner;
  private final NotificationTaskRunner notificationTaskRunner;
  private final OnboardingTaskRunner onboardingTaskRunner;
  private final MonitorTaskRunner monitorTaskRunner;

  @Inject
  public TaskRunnerFactory(
      final DetectionPipelineTaskRunner detectionPipelineTaskRunner,
      final NotificationTaskRunner notificationTaskRunner,
      final OnboardingTaskRunner onboardingTaskRunner,
      final MonitorTaskRunner monitorTaskRunner) {
    this.detectionPipelineTaskRunner = detectionPipelineTaskRunner;
    this.notificationTaskRunner = notificationTaskRunner;
    this.onboardingTaskRunner = onboardingTaskRunner;
    this.monitorTaskRunner = monitorTaskRunner;
  }

  public TaskRunner get(TaskType taskType) {
    switch (taskType) {
      case DETECTION:
        return detectionPipelineTaskRunner;
      case NOTIFICATION:
        return notificationTaskRunner;
      case ONBOARDING:
        return onboardingTaskRunner;
      case MONITOR:
        return monitorTaskRunner;
      default:
        throw new RuntimeException("Invalid TaskType: " + taskType);
    }
  }
}
