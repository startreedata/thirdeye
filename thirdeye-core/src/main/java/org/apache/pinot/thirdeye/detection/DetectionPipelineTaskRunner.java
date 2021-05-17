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

package org.apache.pinot.thirdeye.detection;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.apache.pinot.thirdeye.anomaly.task.TaskContext;
import org.apache.pinot.thirdeye.anomaly.task.TaskInfo;
import org.apache.pinot.thirdeye.anomaly.task.TaskResult;
import org.apache.pinot.thirdeye.anomaly.task.TaskRunner;
import org.apache.pinot.thirdeye.anomaly.utils.ThirdeyeMetricsUtil;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.EvaluationDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionPipelineTaskRunner implements TaskRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineTaskRunner.class);

  private final AlertManager alertManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final EvaluationManager evaluationManager;
  private final DetectionPipelineFactory detectionPipelineFactory;
  private final ModelMaintenanceFlow modelMaintenanceFlow;
  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager;

  /**
   * @param anomalySubscriptionGroupNotificationManager
   * @param alertManager detection config DAO
   * @param mergedAnomalyResultManager merged anomaly DAO
   * @param evaluationManager the evaluation DAO
   * @param detectionPipelineFactory pipeline loader
   */
  @Inject
  public DetectionPipelineTaskRunner(AlertManager alertManager,
      MergedAnomalyResultManager mergedAnomalyResultManager,
      EvaluationManager evaluationManager,
      DetectionPipelineFactory detectionPipelineFactory,
      ModelRetuneFlow modelMaintenanceFlow,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager) {
    this.alertManager = alertManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.evaluationManager = evaluationManager;
    this.detectionPipelineFactory = detectionPipelineFactory;
    this.modelMaintenanceFlow = modelMaintenanceFlow;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
  }

  @Override
  public List<TaskResult> execute(TaskInfo taskInfo, TaskContext taskContext) throws Exception {
    ThirdeyeMetricsUtil.detectionTaskCounter.inc();

    try {
      final DetectionPipelineTaskInfo info = (DetectionPipelineTaskInfo) taskInfo;
      final AlertDTO config = requireNonNull(alertManager.findById(info.configId),
          String.format("Could not resolve config id %d", info.configId));

      LOG.info("Start detection for config {} between {} and {}",
          config.getId(),
          info.start,
          info.end);

      final DetectionPipeline pipeline = detectionPipelineFactory.get(new DetectionPipelineContext()
          .setAlert(config)
          .setStart(info.getStart())
          .setEnd(info.getEnd())
      );
      final DetectionPipelineResultV1 result = pipeline.run();

      if (result.getLastTimestamp() < 0) {
        LOG.info("No detection ran for config {} between {} and {}",
            config.getId(),
            info.start,
            info.end);
        return Collections.emptyList();
      }

      postExecution(config, result);

      ThirdeyeMetricsUtil.detectionTaskSuccessCounter.inc();
      LOG.info("End detection for config {} between {} and {}. Detected {} anomalies.",
          config.getId(), info.start,
          info.end, result.getAnomalies());

      return Collections.emptyList();
    } catch (Exception e) {
      ThirdeyeMetricsUtil.detectionTaskExceptionCounter.inc();
      throw e;
    }
  }

  private void postExecution(final AlertDTO config,
      final DetectionPipelineResultV1 result) {
    config.setLastTimestamp(result.getLastTimestamp());

    for (MergedAnomalyResultDTO mergedAnomalyResultDTO : result.getAnomalies()) {
      this.mergedAnomalyResultManager.save(mergedAnomalyResultDTO);
      if (mergedAnomalyResultDTO.getId() == null) {
        LOG.error("Failed to store anomaly: {}", mergedAnomalyResultDTO);
      }
    }

    for (EvaluationDTO evaluationDTO : result.getEvaluations()) {
      this.evaluationManager.save(evaluationDTO);
    }

    try {
      // run maintenance flow to update model
      final AlertDTO updatedConfig = modelMaintenanceFlow.maintain(config, Instant.now());
      this.alertManager.update(updatedConfig);
    } catch (Exception e) {
      LOG.warn("Re-tune pipeline {} failed", config.getId(), e);
    }

    // re-notify the anomalies if any
    for (MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      // if an anomaly should be re-notified, update the notification lookup table in the database
      if (anomaly.isRenotify()) {
        DetectionUtils.renotifyAnomaly(anomaly,
            anomalySubscriptionGroupNotificationManager);
      }
    }
  }
}
