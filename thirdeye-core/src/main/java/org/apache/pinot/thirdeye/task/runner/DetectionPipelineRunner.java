package org.apache.pinot.thirdeye.task.runner;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Date;
import java.util.Map;
import org.apache.pinot.thirdeye.alert.AlertTemplateRenderer;
import org.apache.pinot.thirdeye.alert.PlanExecutor;
import org.apache.pinot.thirdeye.detection.DetectionPipeline;
import org.apache.pinot.thirdeye.detection.DetectionPipelineContext;
import org.apache.pinot.thirdeye.detection.DetectionPipelineFactory;
import org.apache.pinot.thirdeye.detection.DetectionPipelineResultV1;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.task.DetectionPipelineResultWrapper;

@Singleton
public class DetectionPipelineRunner {

  private final DetectionPipelineFactory detectionPipelineFactory;
  private final PlanExecutor planExecutor;
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public DetectionPipelineRunner(
      final DetectionPipelineFactory detectionPipelineFactory,
      final PlanExecutor planExecutor,
      final AlertTemplateRenderer alertTemplateRenderer) {
    this.detectionPipelineFactory = detectionPipelineFactory;
    this.planExecutor = planExecutor;
    this.alertTemplateRenderer = alertTemplateRenderer;
  }

  public DetectionPipelineResult run(final AlertDTO alert,
      final long start,
      final long end) throws Exception {
    if (PlanExecutor.isV2Alert(alert)) {
      return executeV2Plan(alert, start, end);
    } else {
      return executeV1Plan(alert, start, end);
    }
  }

  private DetectionPipelineResult executeV2Plan(final AlertDTO alert,
      final long start,
      final long end) throws Exception {

    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(
        alert,
        new Date(start),
        new Date(end));

    final Map<String, DetectionPipelineResult> detectionPipelineResultMap = planExecutor.runPipeline(
        templateWithProperties.getNodes(),
        start,
        end);
    checkState(detectionPipelineResultMap.size() == 1,
        "Only a single output from the pipeline is supported at the moment.");
    final DetectionPipelineResult result = detectionPipelineResultMap.values().iterator().next();
    return new DetectionPipelineResultWrapper(alert, result);
  }

  @Deprecated
  private DetectionPipelineResultV1 executeV1Plan(final AlertDTO alert,
      final long start,
      final long end) throws Exception {
    final DetectionPipeline pipeline = detectionPipelineFactory.get(new DetectionPipelineContext()
        .setAlert(alert)
        .setStart(start)
        .setEnd(end));
    return pipeline.run();
  }
}
