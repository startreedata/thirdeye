/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task.runner;

import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.alert.PlanExecutor;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.task.DetectionPipelineResultWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionPipelineRunner {

  private final Logger LOG = LoggerFactory.getLogger(DetectionPipelineRunner.class);

  private final PlanExecutor planExecutor;
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public DetectionPipelineRunner(
      final PlanExecutor planExecutor,
      final AlertTemplateRenderer alertTemplateRenderer) {
    this.planExecutor = planExecutor;
    this.alertTemplateRenderer = alertTemplateRenderer;
  }

  public DetectionPipelineResult run(final AlertDTO alert,
      final Interval detectionInterval) throws Exception {
    LOG.info(String.format("Running detection pipeline for alert: %d, start: %s, end: %s",
        alert.getId(), detectionInterval.getStart(), detectionInterval.getEnd()));

    return executePlan(alert, detectionInterval);
  }

  private DetectionPipelineResult executePlan(final AlertDTO alert,
      final Interval detectionInterval) throws Exception {

    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alert,
        detectionInterval);

    final Map<String, DetectionPipelineResult> detectionPipelineResultMap = planExecutor.runPipeline(
        templateWithProperties.getNodes(),
        detectionInterval);
    checkState(detectionPipelineResultMap.size() == 1,
        "Only a single output from the pipeline is supported at the moment.");
    final DetectionPipelineResult result = detectionPipelineResultMap.values().iterator().next();
    return new DetectionPipelineResultWrapper(alert, result);
  }
}
