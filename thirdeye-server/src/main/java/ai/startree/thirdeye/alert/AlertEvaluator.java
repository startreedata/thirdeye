/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.alert.AlertEvaluatorResponseMapper.toAlertEvaluationApi;
import static ai.startree.thirdeye.core.ExceptionHandler.handleAlertEvaluationException;
import static ai.startree.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static ai.startree.thirdeye.spi.util.SpiUtils.bool;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.DetectionIntervalUtils.computeCorrectedInterval;
import static ai.startree.thirdeye.ResourceUtils.ensure;

import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.EvaluationContextApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.WebApplicationException;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertEvaluator {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertEvaluator.class);

  private static final String ENUMERATOR_NODE_TYPE = "Enumerator";
  // 5 detection previews are running at the same time at most
  private static final int PARALLELISM = 5;

  // max time allowed for a preview task
  private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final ExecutorService executorService;
  private final PlanExecutor planExecutor;
  private final EvaluationContextProcessor evaluationContextProcessor;

  @Inject
  public AlertEvaluator(
      final AlertTemplateRenderer alertTemplateRenderer,
      final PlanExecutor planExecutor,
      final EvaluationContextProcessor evaluationContextProcessor) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.planExecutor = planExecutor;
    this.evaluationContextProcessor = evaluationContextProcessor;

    executorService = Executors.newFixedThreadPool(PARALLELISM,
        new ThreadFactoryBuilder().setNameFormat("alert-evaluator-%d").build());
  }

  private void stop() {
    executorService.shutdownNow();
  }

  // does not resolve namespace - assumes namespace is set in the request by the consumer 
  @VisibleForTesting
  public AlertEvaluationApi evaluate(final AlertEvaluationApi request)
      throws ExecutionException {
    try {
      return evaluate0(request);
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      handleAlertEvaluationException(e);
    }
    return null;
  }

  private AlertEvaluationApi evaluate0(final AlertEvaluationApi request)
      throws Exception {
    final long startTime = request.getStart().getTime();
    final long endTime = request.getEnd().getTime();
    final String namespace = optional(request.getAlert().getAuth()).map(
        AuthorizationConfigurationApi::getNamespace).orElse(null);
    final AlertTemplateDTO renderedTemplate = alertTemplateRenderer.renderAlert(request.getAlert(),
        namespace);
    final Interval detectionInterval = computeCorrectedInterval(
        request.getAlert().getId(), startTime, endTime, renderedTemplate);
    final DetectionPipelineContext context = new DetectionPipelineContext()
        .setAlertId(request.getAlert().getId())
        .setNamespace(namespace)
        .setUsage(DetectionPipelineUsage.EVALUATION)
        .setDetectionInterval(detectionInterval);

    // inject custom evaluation context
    final EvaluationContextApi evaluationContext = request.getEvaluationContext();
    evaluationContextProcessor.process(context, evaluationContext);

    if (bool(request.isDryRun())) {
      return new AlertEvaluationApi()
          .setDryRun(true)
          .setAlert(new AlertApi()
              .setTemplate(toAlertTemplateApi(renderedTemplate)));
    }

    final String rootNodeName = optional(evaluationContext)
        .map(EvaluationContextApi::getListEnumerationItemsOnly)
        .filter(b -> b)
        .map(b -> findEnumeratorNodeName(renderedTemplate.getNodes()))
        .orElse(PlanExecutor.ROOT_NODE_NAME);

    final Map<String, OperatorResult> result = executorService
        .submit(() -> planExecutor.runAndGetOutputs(renderedTemplate.getNodes(),
            context,
            rootNodeName))
        .get(TIMEOUT, TimeUnit.MILLISECONDS);

    final Map<String, OperatorResult> processed = new DetectionPipelineOutputPostProcessor()
        .process(result, request);

    return toAlertEvaluationApi(processed)
        .setAlert(new AlertApi().setTemplate(toAlertTemplateApi(renderedTemplate)));
  }

  private String findEnumeratorNodeName(final List<PlanNodeBean> nodes) {
    final List<String> enumeratorNodeNames = nodes.stream()
        .filter(n -> ENUMERATOR_NODE_TYPE.equals(n.getType()))
        .map(PlanNodeBean::getName)
        .toList();
    ensure(enumeratorNodeNames.size() == 1,
        String.format("Expecting exactly 1 enumeration item in the template. Found: %d",
            enumeratorNodeNames.size()));

    return enumeratorNodeNames.iterator().next();
  }
}
