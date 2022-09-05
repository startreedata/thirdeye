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
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.detectionpipeline.operator.CombinerResult;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionPipelineRunner {

  private final Logger LOG = LoggerFactory.getLogger(DetectionPipelineRunner.class);

  private final PlanExecutor planExecutor;
  private final AlertTemplateRenderer alertTemplateRenderer;
  private final EnumerationItemManager enumerationItemManager;

  @Inject
  public DetectionPipelineRunner(
      final PlanExecutor planExecutor,
      final AlertTemplateRenderer alertTemplateRenderer,
      final EnumerationItemManager enumerationItemManager) {
    this.planExecutor = planExecutor;
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.enumerationItemManager = enumerationItemManager;
  }

  private static EnumerationItemDTO enumerationItemRef(final EnumerationItemDTO evaluationItem) {
    final EnumerationItemDTO ei = new EnumerationItemDTO();
    ei.setId(evaluationItem.getId());
    return ei;
  }

  private static boolean matches(final EnumerationItemDTO o1, final EnumerationItemDTO o2) {
    return Objects.equals(o1.getName(), o2.getName())
        && Objects.equals(o1.getParams(), o2.getParams());
  }

  public OperatorResult run(final AlertDTO alert,
      final Interval detectionInterval) throws Exception {
    LOG.info(String.format("Running detection pipeline for alert: %d, start: %s, end: %s",
        alert.getId(), detectionInterval.getStart(), detectionInterval.getEnd()));

    final OperatorResult result = executePlan(alert, detectionInterval);
    enrichAnomalies(result, alert);
    return result;
  }

  private void enrichAnomalies(final OperatorResult result, final AlertDTO alert) {
    // generic enrichment
    result.getAnomalies().forEach(anomaly -> anomaly.setDetectionConfigId(alert.getId()));

    // dimension exploration enrichment
    // TODO spyne can this casting thing be eliminated?
    if (result instanceof CombinerResult) {
      enrichAnomaliesFromCombinerResult((CombinerResult) result);
    }
  }

  private void enrichAnomaliesFromCombinerResult(final CombinerResult result) {
    result
        .getDetectionResults()
        .stream()
        .filter(r -> r.getEnumerationItem() != null)
        .forEach(this::enrichAnomaliesWithEnumerationItem);
  }

  private void enrichAnomaliesWithEnumerationItem(final OperatorResult result) {
    final EnumerationItemDTO enumerationItemDTO = findExistingOrCreate(
        requireNonNull(result.getEnumerationItem(), "enumerationItem is null"));
    result
        .getAnomalies()
        .forEach(anomaly -> anomaly.setEnumerationItem(enumerationItemRef(enumerationItemDTO)));
  }

  private EnumerationItemDTO findExistingOrCreate(final EnumerationItemDTO source) {
    requireNonNull(source.getName(), "enumeration item name does not exist!");

    final List<EnumerationItemDTO> byName = enumerationItemManager.findByName(source.getName());

    final Optional<EnumerationItemDTO> filtered = optional(byName).orElse(emptyList()).stream()
        .filter(e -> matches(source, e))
        .findFirst();

    if (filtered.isEmpty()) {
      /* Create new */
      enumerationItemManager.save(source);
      requireNonNull(source.getId(), "expecting a generated ID");
      return source;
    }

    return filtered.get();
  }

  private OperatorResult executePlan(final AlertDTO alert,
      final Interval detectionInterval) throws Exception {

    final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alert,
        detectionInterval);

    final Map<String, OperatorResult> detectionPipelineResultMap = planExecutor.runPipeline(
        templateWithProperties.getNodes(),
        detectionInterval);
    checkState(detectionPipelineResultMap.size() == 1,
        "Only a single output from the pipeline is supported at the moment.");
    return detectionPipelineResultMap.values().iterator().next();
  }
}
