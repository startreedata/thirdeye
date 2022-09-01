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
package ai.startree.thirdeye.worker.task.runner;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.v2.DetectionResult;
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
  private final DetectionPipelineRunner detectionPipelineRunner;
  private final AnomalyMerger anomalyMerger;
  private final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator;

  @Inject
  public DetectionPipelineTaskRunner(final AlertManager alertManager,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager,
      final MetricRegistry metricRegistry, final DetectionPipelineRunner detectionPipelineRunner,
      final AnomalyMerger anomalyMerger,
      final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator) {
    this.alertManager = alertManager;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
    this.detectionPipelineRunner = detectionPipelineRunner;
    this.anomalyMerger = anomalyMerger;
    this.alertDetectionIntervalCalculator = alertDetectionIntervalCalculator;

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

      Interval detectionInterval = alertDetectionIntervalCalculator.getCorrectedInterval(alert,
          info.getStart(),
          info.getEnd());

      final DetectionResult result = detectionPipelineRunner.run(alert, detectionInterval);

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
      postExecution(alert, result);

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

  private void postExecution(final AlertDTO alert, final DetectionResult result) {
    anomalyMerger.mergeAndSave(alert, result.getAnomalies());

    // re-notify the anomalies if any
    for (final MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      // if an anomaly should be re-notified, update the notification lookup table in the database
      if (anomaly.isRenotify()) {
        DetectionUtils.renotifyAnomaly(anomaly, anomalySubscriptionGroupNotificationManager);
      }
    }
  }
}
