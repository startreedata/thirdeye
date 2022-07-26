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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.worker.task.OnboardingTaskInfo;
import ai.startree.thirdeye.worker.task.TaskContext;
import ai.startree.thirdeye.worker.task.TaskResult;
import ai.startree.thirdeye.worker.task.TaskRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The task runner to run onboarding task after a new detection is set up
 * It will replay the detection pipeline and the re-tune the pipeline.
 * Because for some pipeline component, tuning is depend on replay result
 */
@Singleton
public class OnboardingTaskRunner implements TaskRunner {

  private static final Logger LOG = LoggerFactory.getLogger(OnboardingTaskRunner.class);

  private final AlertManager alertManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final DetectionPipelineRunner detectionPipelineRunner;
  private final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator;

  @Inject
  public OnboardingTaskRunner(final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager,
      final DetectionPipelineRunner detectionPipelineRunner,
      final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator) {
    this.detectionPipelineRunner = detectionPipelineRunner;
    this.alertManager = alertManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.alertDetectionIntervalCalculator = alertDetectionIntervalCalculator;
  }

  @Override
  public List<TaskResult> execute(final TaskInfo taskInfo, final TaskContext taskContext)
      throws Exception {
    final OnboardingTaskInfo info = (OnboardingTaskInfo) taskInfo;
    LOG.info("Start onboarding task for id {} between {} and {}",
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
      LOG.warn("No data returned for detection run for id {} between {} and {}",
          alert.getId(),
          detectionInterval.getStart(),
          detectionInterval.getEnd());
      return Collections.emptyList();
    }

    alert.setLastTimestamp(detectionInterval.getEndMillis());
    alertManager.update(alert);

    for (final MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      anomaly.setAnomalyResultSource(AnomalyResultSource.ANOMALY_REPLAY);
      mergedAnomalyResultManager.save(anomaly);
      if (anomaly.getId() == null) {
        LOG.warn("Could not store anomaly:\n{}", anomaly);
      }
    }

    LOG.info("Completed detection task for id {} between {} and {}. Detected {} anomalies.",
        alert.getId(),
        detectionInterval.getStart(),
        detectionInterval.getEnd(),
        optional(result.getAnomalies()).map(List::size).orElse(0));
    return Collections.emptyList();
  }
}
