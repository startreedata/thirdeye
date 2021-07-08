package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.alert.AlertExceptionHandler.handleAlertEvaluationException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.v2.plan.DetectionPipelinePlanNodeFactory;
import org.apache.pinot.thirdeye.spi.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.DetectionEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.v2.AlertEvaluationPlanApi;
import org.apache.pinot.thirdeye.spi.api.v2.DetectionPlanApi;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertEvaluatorV2 {

  public static final String ROOT_OPERATOR_KEY = "root";
  protected static final Logger LOG = LoggerFactory.getLogger(AlertEvaluatorV2.class);

  // 5 detection previews are running at the same time at most
  private static final int PARALLELISM = 5;

  // max time allowed for a preview task
  private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final ExecutorService executorService;
  private final DetectionPipelinePlanNodeFactory detectionPipelinePlanNodeFactory;

  @Inject
  public AlertEvaluatorV2(
      final DetectionPipelinePlanNodeFactory detectionPipelinePlanNodeFactory) {
    this.detectionPipelinePlanNodeFactory = detectionPipelinePlanNodeFactory;
    executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  private void stop() {
    executorService.shutdownNow();
  }

  public AlertEvaluationApi evaluate(
      final AlertEvaluationPlanApi request)
      throws ExecutionException {
    try {
      final Map<String, DetectionPipelineResult> result = runPipeline(request);
      return toApi(result);
    } catch (final Exception e) {
      handleAlertEvaluationException(e);
    }
    return null;
  }

  private Map<String, DetectionPipelineResult> runPipeline(final AlertEvaluationPlanApi request)
      throws Exception {
    final Map<String, PlanNode> pipelinePlanNodes = new HashMap<>();
    for (final DetectionPlanApi operator : request.getNodes()) {
      final String operatorName = operator.getPlanNodeName();
      pipelinePlanNodes.put(operatorName, detectionPipelinePlanNodeFactory
          .get(operatorName,
              pipelinePlanNodes,
              operator,
              request.getStart().getTime(),
              request.getEnd().getTime()));
    }
    return executorService.submit(() -> {
      final PlanNode rootNode = pipelinePlanNodes.get(ROOT_OPERATOR_KEY);
      final Map<String, DetectionPipelineResult> context = new HashMap<>();
      PlanExecutor.executePlanNode(pipelinePlanNodes, context, rootNode);
      final Map<String, DetectionPipelineResult> output = getOutput(context, rootNode);
      return output;
    }).get(TIMEOUT, TimeUnit.MILLISECONDS);
  }

  private Map<String, DetectionPipelineResult> getOutput(
      final Map<String, DetectionPipelineResult> context,
      final PlanNode rootNode) {
    final Map<String, DetectionPipelineResult> results = new HashMap<>();
    for (final String contextKey : context.keySet()) {
      if (PlanExecutor.getNodeFromContextKey(contextKey).equals(rootNode.getName())) {
        results.put(PlanExecutor.getOutputKeyFromContextKey(contextKey), context.get(contextKey));
      }
    }
    return results;
  }

  private AlertEvaluationApi toApi(
      final Map<String, DetectionPipelineResult> outputMap) {

    final Map<String, Map<String, DetectionEvaluationApi>> resultMap = new HashMap<>();
    for (final String key : outputMap.keySet()) {
      final DetectionPipelineResult result = outputMap.get(key);
      resultMap.put(key, detectionPipelineResultToApi(result));
    }
    return new AlertEvaluationApi().setEvaluations(resultMap);
  }

  private Map<String, DetectionEvaluationApi> detectionPipelineResultToApi(
      final DetectionPipelineResult result) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    final List<DetectionResult> detectionResults = result.getDetectionResults();
    for (int i = 0; i < detectionResults.size(); i++) {
      final DetectionEvaluationApi detectionEvaluationApi = detectionResults.get(i).toApi();
      map.put(String.valueOf(i), detectionEvaluationApi);
    }
    return map;
  }
}
