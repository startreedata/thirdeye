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

package org.apache.pinot.thirdeye.anomaly.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.anomaly.monitor.MonitorTaskRunner;
import org.apache.pinot.thirdeye.anomaly.task.TaskConstants.TaskType;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.detection.DetectionPipelineLoader;
import org.apache.pinot.thirdeye.detection.DetectionPipelineTaskRunner;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertTaskRunner;
import org.apache.pinot.thirdeye.detection.dataquality.DataQualityPipelineTaskRunner;
import org.apache.pinot.thirdeye.detection.onboard.YamlOnboardingTaskRunner;

@Singleton
public class TaskRunnerFactory {

  private final AlertManager detectionConfigManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final EvaluationManager evaluationManager;
  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final EventManager eventManager;
  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;

  @Inject
  public TaskRunnerFactory(
      final AlertManager detectionConfigManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final EvaluationManager evaluationManager,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final EventManager eventManager,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    this.detectionConfigManager = detectionConfigManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.evaluationManager = evaluationManager;
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;

    this.eventManager = eventManager;
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
  }

  public TaskRunner get(TaskType taskType) {
    switch (taskType) {
      case DATA_QUALITY:
        return new DataQualityPipelineTaskRunner();
      case DETECTION:
        return new DetectionPipelineTaskRunner(new DetectionPipelineLoader(),
            detectionConfigManager,
            mergedAnomalyResultManager,
            evaluationManager,
            metricConfigManager,
            datasetConfigManager,
            eventManager,
            thirdEyeCacheRegistry);
      case DETECTION_ALERT:
        return new DetectionAlertTaskRunner();
      case YAML_DETECTION_ONBOARD:
        return new YamlOnboardingTaskRunner();
      case MONITOR:
        return new MonitorTaskRunner();
      default:
        throw new RuntimeException("Invalid TaskType: " + taskType);
    }
  }
}
