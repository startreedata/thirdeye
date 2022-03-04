/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task.runner;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.task.OnboardingTaskInfo;
import ai.startree.thirdeye.task.TaskContext;
import ai.startree.thirdeye.task.TaskResult;
import ai.startree.thirdeye.task.TaskRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
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

  @Inject
  public OnboardingTaskRunner(final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager alertManager,
      final DetectionPipelineRunner detectionPipelineRunner) {
    this.detectionPipelineRunner = detectionPipelineRunner;
    this.alertManager = alertManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
  }

  @Override
  public List<TaskResult> execute(final TaskInfo taskInfo, final TaskContext taskContext)
      throws Exception {
    final OnboardingTaskInfo info = (OnboardingTaskInfo) taskInfo;
    final long alertId = info.getConfigId();
    LOG.info("Running detection onboarding task for id {}", alertId);

    // replay the detection pipeline
    final AlertDTO alert = requireNonNull(alertManager.findById(alertId),
        String.format("Could not resolve config id %d", alertId));

    final DetectionPipelineResult result = detectionPipelineRunner.run(alert,
        info.getStart(),
        info.getEnd());

    if (result.getLastTimestamp() < 0) {
      return Collections.emptyList();
    }

    alert.setLastTimestamp(info.getEnd());
    alertManager.update(alert);

    for (final MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      anomaly.setAnomalyResultSource(AnomalyResultSource.ANOMALY_REPLAY);
      mergedAnomalyResultManager.save(anomaly);
      if (anomaly.getId() == null) {
        LOG.warn("Could not store anomaly:\n{}", anomaly);
      }
    }

    LOG.info("Detection onboarding task for id {} completed", alertId);
    return Collections.emptyList();
  }
}
