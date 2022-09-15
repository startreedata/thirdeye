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
package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.alert.AlertEvaluatorResponseMapper.toAlertEvaluationApi;
import static ai.startree.thirdeye.core.ExceptionHandler.handleAlertEvaluationException;
import static ai.startree.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static ai.startree.thirdeye.spi.util.SpiUtils.bool;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
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

  // 5 detection previews are running at the same time at most
  private static final int PARALLELISM = 5;

  // max time allowed for a preview task
  private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final ExecutorService executorService;
  private final PlanExecutor planExecutor;
  private final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator;
  private final EvaluationContextProcessor evaluationContextProcessor;

  @Inject
  public AlertEvaluator(
      final AlertTemplateRenderer alertTemplateRenderer,
      final PlanExecutor planExecutor,
      final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator,
      final EvaluationContextProcessor evaluationContextProcessor) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.planExecutor = planExecutor;
    this.alertDetectionIntervalCalculator = alertDetectionIntervalCalculator;
    this.evaluationContextProcessor = evaluationContextProcessor;

    executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  private void stop() {
    executorService.shutdownNow();
  }

  public AlertEvaluationApi evaluate(final AlertEvaluationApi request)
      throws ExecutionException {
    try {
      final Interval detectionInterval = computeDetectionInterval(request);

      // apply template properties
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(request.getAlert(),
          detectionInterval);

      // inject custom evaluation context
      optional(request.getEvaluationContext())
          .ifPresent(ctx -> evaluationContextProcessor.process(templateWithProperties, ctx));

      if (bool(request.isDryRun())) {
        return new AlertEvaluationApi()
            .setDryRun(true)
            .setAlert(new AlertApi()
                .setTemplate(toAlertTemplateApi(templateWithProperties)));
      }

      final Map<String, OperatorResult> result = executorService
          .submit(() -> planExecutor.runPipelineAndGetRootOutputs(templateWithProperties.getNodes(),
              detectionInterval))
          .get(TIMEOUT, TimeUnit.MILLISECONDS);

      return toAlertEvaluationApi(result)
          .setAlert(new AlertApi()
              .setTemplate(toAlertTemplateApi(templateWithProperties)));
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      handleAlertEvaluationException(e);
    }
    return null;
  }

  private Interval computeDetectionInterval(final AlertEvaluationApi request)
      throws IOException, ClassNotFoundException {
    // this method only exists to catch exception and translate into a TE exception
    final Interval detectionInterval;
    detectionInterval = alertDetectionIntervalCalculator.getCorrectedInterval(request.getAlert(),
        request.getStart().getTime(),
        request.getEnd().getTime());
    return detectionInterval;
  }
}
