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
package ai.startree.thirdeye.worker.task;

import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.worker.task.runner.DetectionPipelineTaskRunner;
import ai.startree.thirdeye.worker.task.runner.MonitorTaskRunner;
import ai.startree.thirdeye.worker.task.runner.NotificationTaskRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TaskRunnerFactory {

  private final DetectionPipelineTaskRunner detectionPipelineTaskRunner;
  private final NotificationTaskRunner notificationTaskRunner;
  private final MonitorTaskRunner monitorTaskRunner;

  @Inject
  public TaskRunnerFactory(
      final DetectionPipelineTaskRunner detectionPipelineTaskRunner,
      final NotificationTaskRunner notificationTaskRunner,
      final MonitorTaskRunner monitorTaskRunner) {
    this.detectionPipelineTaskRunner = detectionPipelineTaskRunner;
    this.notificationTaskRunner = notificationTaskRunner;
    this.monitorTaskRunner = monitorTaskRunner;
  }

  public TaskRunner get(TaskType taskType) {
    switch (taskType) {
      case ONBOARDING:
      case DETECTION:
        return detectionPipelineTaskRunner;
      case NOTIFICATION:
        return notificationTaskRunner;
      case MONITOR:
        return monitorTaskRunner;
      default:
        throw new RuntimeException("Invalid TaskType: " + taskType);
    }
  }
}
