/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task.runner;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator;
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
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
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
  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager;
  private final DetectionPipelineRunner detectionPipelineRunner;
  private final AnomalyMerger anomalyMerger;
  private final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator;

  @Inject
  public DetectionPipelineTaskRunner(final AlertManager alertManager,
      final EvaluationManager evaluationManager,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager,
      final MetricRegistry metricRegistry,
      final DetectionPipelineRunner detectionPipelineRunner,
      final AnomalyMerger anomalyMerger,
      final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator) {
    this.alertManager = alertManager;
    this.evaluationManager = evaluationManager;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
    this.detectionPipelineRunner = detectionPipelineRunner;

    detectionTaskExceptionCounter = metricRegistry.counter("detectionTaskExceptionCounter");
    detectionTaskSuccessCounter = metricRegistry.counter("detectionTaskSuccessCounter");
    detectionTaskCounter = metricRegistry.counter("detectionTaskCounter");
    this.anomalyMerger = anomalyMerger;
    this.alertDetectionIntervalCalculator = alertDetectionIntervalCalculator;
  }

  @Override
  public List<TaskResult> execute(final TaskInfo taskInfo, final TaskContext taskContext)
      throws Exception {
    detectionTaskCounter.inc();
    try {
      final DetectionPipelineTaskInfo info = (DetectionPipelineTaskInfo) taskInfo;
      LOG.info("Start detection task for id {} between {} and {}",
          info.getConfigId(),
          new DateTime(info.getStart(), DateTimeZone.UTC),
          new DateTime(info.getEnd(), DateTimeZone.UTC));
      final AlertDTO alert = requireNonNull(alertManager.findById(info.getConfigId()),
          String.format("Could not resolve config id %d", info.getConfigId()));

      Interval detectionInterval = alertDetectionIntervalCalculator
          .getCorrectedInterval(alert, info.getStart(), info.getEnd());

      final DetectionPipelineResult result = detectionPipelineRunner.run(alert, detectionInterval);

      if (result.getLastTimestamp() < 0) {
        // notice lastTimestamp is not updated
        LOG.info("No data returned for detection run for id {} between {} and {}",
            alert.getId(),
            detectionInterval.getStart(),
            detectionInterval.getEnd());
        return Collections.emptyList();
      }

      postExecution(alert, result, detectionInterval);

      detectionTaskSuccessCounter.inc();
      LOG.info("Completed detection task for id {} between {} and {}. Detected {} anomalies.",
          alert.getId(),
          detectionInterval.getStart(),
          detectionInterval.getEnd(),
          optional(result.getAnomalies()).map(List::size).orElse(0));

      return Collections.emptyList();
    } catch (final Exception e) {
      detectionTaskExceptionCounter.inc();
      throw e;
    }
  }

  private void postExecution(final AlertDTO alert, final DetectionPipelineResult result,
      final Interval detectionInterval) {
    alert.setLastTimestamp(detectionInterval.getEndMillis());
    alertManager.update(alert);
    anomalyMerger.mergeAndSave(alert, result.getAnomalies(), detectionInterval);

    for (final EvaluationDTO evaluationDTO : result.getEvaluations()) {
      evaluationManager.save(evaluationDTO);
    }

    // re-notify the anomalies if any
    for (final MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      // if an anomaly should be re-notified, update the notification lookup table in the database
      if (anomaly.isRenotify()) {
        DetectionUtils.renotifyAnomaly(anomaly, anomalySubscriptionGroupNotificationManager);
      }
    }
  }
}
