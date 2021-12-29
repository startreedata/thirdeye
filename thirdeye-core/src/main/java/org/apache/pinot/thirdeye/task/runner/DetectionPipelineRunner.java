package org.apache.pinot.thirdeye.task.runner;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Date;
import java.util.Map;
import org.apache.pinot.thirdeye.alert.AlertTemplateRenderer;
import org.apache.pinot.thirdeye.alert.PlanExecutor;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.task.DetectionPipelineResultWrapper;
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
      final long start,
      final long end) throws Exception {
    LOG.info(String.format("Running detection pipeline for alert: %d, start: %s, end: %s",
        alert.getId(), new Date(start), new Date(end)));
    if (PlanExecutor.isV2Alert(alert)) {
      return executeV2Plan(alert, start, end);
    } else {
      throw new RuntimeException("V1 detection pipeline is not supported anymore.");
    }
  }

  private DetectionPipelineResult executeV2Plan(final AlertDTO alert,
      final long start,
      final long end) throws Exception {

    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(
        alert,
        start,
        end);

    final Map<String, DetectionPipelineResult> detectionPipelineResultMap = planExecutor.runPipeline(
        templateWithProperties.getNodes(),
        start,
        end);
    checkState(detectionPipelineResultMap.size() == 1,
        "Only a single output from the pipeline is supported at the moment.");
    final DetectionPipelineResult result = detectionPipelineResultMap.values().iterator().next();
    return new DetectionPipelineResultWrapper(alert, result);
  }
}
