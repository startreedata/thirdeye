/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task.runner;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detection.ModelMaintenanceFlow;
import ai.startree.thirdeye.detection.ModelRetuneFlow;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.EvaluationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.task.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.task.TaskContext;
import ai.startree.thirdeye.task.TaskResult;
import ai.startree.thirdeye.task.TaskRunner;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionPipelineTaskRunner implements TaskRunner {

  private final Logger LOG = LoggerFactory.getLogger(DetectionPipelineTaskRunner.class);

  private final Counter detectionTaskExceptionCounter;
  private final Counter detectionTaskSuccessCounter;
  private final Counter detectionTaskCounter;

  private final AlertManager alertManager;
  private final EvaluationManager evaluationManager;
  private final ModelMaintenanceFlow modelMaintenanceFlow;
  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager;
  private final DetectionPipelineRunner detectionPipelineRunner;
  private final AnomalyMerger anomalyMerger;

  @Inject
  public DetectionPipelineTaskRunner(final AlertManager alertManager,
      final EvaluationManager evaluationManager,
      final ModelRetuneFlow modelMaintenanceFlow,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager,
      final MetricRegistry metricRegistry,
      final DetectionPipelineRunner detectionPipelineRunner,
      final AnomalyMerger anomalyMerger) {
    this.alertManager = alertManager;
    this.evaluationManager = evaluationManager;
    this.modelMaintenanceFlow = modelMaintenanceFlow;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
    this.detectionPipelineRunner = detectionPipelineRunner;

    detectionTaskExceptionCounter = metricRegistry.counter("detectionTaskExceptionCounter");
    detectionTaskSuccessCounter = metricRegistry.counter("detectionTaskSuccessCounter");
    detectionTaskCounter = metricRegistry.counter("detectionTaskCounter");
    this.anomalyMerger = anomalyMerger;
  }

  @Override
  public List<TaskResult> execute(final TaskInfo taskInfo, final TaskContext taskContext)
      throws Exception {
    detectionTaskCounter.inc();

    try {
      final DetectionPipelineTaskInfo info = (DetectionPipelineTaskInfo) taskInfo;
      final AlertDTO alert = requireNonNull(alertManager.findById(info.getConfigId()),
          String.format("Could not resolve config id %d", info.getConfigId()));

      LOG.info("Start detection for config {} between {} and {}",
          alert.getId(),
          info.getStart(),
          info.getEnd());

      final DetectionPipelineResult result = detectionPipelineRunner.run(
          alert,
          info.getStart(),
          info.getEnd());

      if (result.getLastTimestamp() < 0) {
        LOG.info("No detection ran for config {} between {} and {}",
            alert.getId(),
            info.getStart(),
            info.getEnd());
        return Collections.emptyList();
      }

      postExecution(info, alert, result);

      detectionTaskSuccessCounter.inc();
      LOG.info("End detection for alert {} between {} and {}. Detected {} anomalies.",
          alert.getId(),
          new Date(info.getStart()),
          new Date(info.getEnd()),
          optional(result.getAnomalies()).map(List::size).orElse(0));

      return Collections.emptyList();
    } catch (final Exception e) {
      detectionTaskExceptionCounter.inc();
      throw e;
    }
  }

  private void postExecution(final DetectionPipelineTaskInfo taskInfo,
      final AlertDTO alert, final DetectionPipelineResult result) {
    alert.setLastTimestamp(result.getLastTimestamp());

    anomalyMerger.mergeAndSave(taskInfo, alert, result.getAnomalies());

    for (final EvaluationDTO evaluationDTO : result.getEvaluations()) {
      evaluationManager.save(evaluationDTO);
    }

    try {
      // run maintenance flow to update model
      final AlertDTO updatedConfig = modelMaintenanceFlow.maintain(alert, Instant.now());
      alertManager.update(updatedConfig);
    } catch (final Exception e) {
      LOG.warn("Re-tune pipeline {} failed", alert.getId(), e);
    }

    // re-notify the anomalies if any
    for (final MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      // if an anomaly should be re-notified, update the notification lookup table in the database
      if (anomaly.isRenotify()) {
        DetectionUtils.renotifyAnomaly(anomaly,
            anomalySubscriptionGroupNotificationManager);
      }
    }
  }
}
