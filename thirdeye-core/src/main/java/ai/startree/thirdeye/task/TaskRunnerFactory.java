/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
