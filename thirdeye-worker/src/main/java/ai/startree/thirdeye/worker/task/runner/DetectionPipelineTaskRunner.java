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
package ai.startree.thirdeye.worker.task.runner;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator;
import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.worker.task.TaskContext;
import ai.startree.thirdeye.worker.task.TaskResult;
import ai.startree.thirdeye.worker.task.TaskRunner;
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
  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager;
  private final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator;
  private final MergedAnomalyResultManager anomalyDao;
  private final PlanExecutor planExecutor;
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public DetectionPipelineTaskRunner(final AlertManager alertManager,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager,
      final MetricRegistry metricRegistry,
      final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator,
      final MergedAnomalyResultManager anomalyDao,
      final PlanExecutor planExecutor,
      final AlertTemplateRenderer alertTemplateRenderer) {
    this.alertManager = alertManager;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
    this.alertDetectionIntervalCalculator = alertDetectionIntervalCalculator;
    this.anomalyDao = anomalyDao;
    this.planExecutor = planExecutor;
    this.alertTemplateRenderer = alertTemplateRenderer;

    detectionTaskExceptionCounter = metricRegistry.counter("detectionTaskExceptionCounter");
    detectionTaskSuccessCounter = metricRegistry.counter("detectionTaskSuccessCounter");
    detectionTaskCounter = metricRegistry.counter("detectionTaskCounter");
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

      final Interval detectionInterval = alertDetectionIntervalCalculator.getCorrectedInterval(
          alert,
          info.getStart(), info.getEnd());

      final OperatorResult result = run(alert, detectionInterval);

      if (result.getLastTimestamp() < 0) {
        // notice lastTimestamp is not updated
        LOG.info("No data returned for detection run for id {} between {} and {}",
            alert.getId(),
            detectionInterval.getStart(),
            detectionInterval.getEnd());
        return Collections.emptyList();
      }

      alert.setLastTimestamp(detectionInterval.getEndMillis());
      alertManager.update(alert);
      postExecution(result);

      detectionTaskSuccessCounter.inc();
      LOG.info("Completed detection task for id {} between {} and {}. Detected {} anomalies.",
          alert.getId(),
          detectionInterval.getStart(),
          detectionInterval.getEnd(),
          result.getAnomalies().size());

      return Collections.emptyList();
    } catch (final Exception e) {
      detectionTaskExceptionCounter.inc();
      throw e;
    }
  }

  public OperatorResult run(final AlertDTO alert, final Interval detectionInterval)
      throws Exception {
    LOG.info(String.format("Running detection pipeline for alert: %d, start: %s, end: %s",
        alert.getId(), detectionInterval.getStart(), detectionInterval.getEnd()));

    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alert,
        detectionInterval);

    final DetectionPipelineContext context = new DetectionPipelineContext()
        .setAlertId(alert.getId())
        .setUsage(DetectionPipelineUsage.DETECTION)
        .setDetectionInterval(detectionInterval);
    final var detectionPipelineResultMap = planExecutor.runPipelineAndGetRootOutputs(
        templateWithProperties.getNodes(),
        context);
    checkState(detectionPipelineResultMap.size() == 1,
        "Only a single output from the pipeline is supported at the moment.");
    return detectionPipelineResultMap.values().iterator().next();
  }

  private void postExecution(final OperatorResult result) {
    final List<MergedAnomalyResultDTO> anomalies = result.getAnomalies();
    if (anomalies == null) {
      return;
    }
    for (final MergedAnomalyResultDTO mergedAnomalyResultDTO : anomalies) {
      final Long id = anomalyDao.save(mergedAnomalyResultDTO);
      if (id == null) {
        LOG.error("Failed to store anomaly: {}", mergedAnomalyResultDTO);
      }
    }

    // re-notify the anomalies if any
    // note cyril - dead code - renotify is always false
    for (final MergedAnomalyResultDTO anomaly : anomalies) {
      // if an anomaly should be re-notified, update the notification lookup table in the database
      if (anomaly.isRenotify()) {
        DetectionUtils.renotifyAnomaly(anomaly, anomalySubscriptionGroupNotificationManager);
      }
    }
  }
}
