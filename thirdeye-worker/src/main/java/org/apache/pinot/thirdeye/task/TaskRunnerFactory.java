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

package org.apache.pinot.thirdeye.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.anomaly.monitor.MonitorTaskRunner;
import org.apache.pinot.thirdeye.anomaly.task.TaskConstants.TaskType;
import org.apache.pinot.thirdeye.anomaly.task.TaskRunner;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.detection.DetectionPipelineTaskRunner;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertTaskRunner;
import org.apache.pinot.thirdeye.detection.dataquality.DataQualityPipelineTaskRunner;
import org.apache.pinot.thirdeye.detection.onboard.YamlOnboardingTaskRunner;

@Singleton
public class TaskRunnerFactory {

  private final DAORegistry daoRegistry;
  private final DetectionPipelineTaskRunner detectionPipelineTaskRunner;
  private final DataQualityPipelineTaskRunner dataQualityPipelineTaskRunner;
  private final DetectionAlertTaskRunner detectionAlertTaskRunner;
  private final YamlOnboardingTaskRunner yamlOnboardingTaskRunner;

  @Inject
  public TaskRunnerFactory(
      final DAORegistry daoRegistry,
      final DetectionPipelineTaskRunner detectionPipelineTaskRunner,
      final DataQualityPipelineTaskRunner dataQualityPipelineTaskRunner,
      final DetectionAlertTaskRunner detectionAlertTaskRunner,
      final YamlOnboardingTaskRunner yamlOnboardingTaskRunner) {
    this.daoRegistry = daoRegistry;
    this.detectionPipelineTaskRunner = detectionPipelineTaskRunner;
    this.dataQualityPipelineTaskRunner = dataQualityPipelineTaskRunner;
    this.detectionAlertTaskRunner = detectionAlertTaskRunner;
    this.yamlOnboardingTaskRunner = yamlOnboardingTaskRunner;
  }

  public TaskRunner get(TaskType taskType) {
    switch (taskType) {
      case DATA_QUALITY:
        return dataQualityPipelineTaskRunner;
      case DETECTION:
        return detectionPipelineTaskRunner;
      case DETECTION_ALERT:
        return detectionAlertTaskRunner;
      case YAML_DETECTION_ONBOARD:
        return yamlOnboardingTaskRunner;
      case MONITOR:
        return new MonitorTaskRunner(daoRegistry);
      default:
        throw new RuntimeException("Invalid TaskType: " + taskType);
    }
  }
}
