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

import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.alert.PlanExecutor;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.worker.task.DetectionPipelineResultWrapper;
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
