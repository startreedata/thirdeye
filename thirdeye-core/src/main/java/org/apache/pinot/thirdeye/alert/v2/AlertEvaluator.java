package org.apache.pinot.thirdeye.alert.v2;

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
import org.apache.pinot.thirdeye.spi.api.DetectionEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.v2.AlertEvaluationPlanApi;
import org.apache.pinot.thirdeye.spi.api.v2.DetectionPlanApi;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertEvaluator {

  public static final String ROOT_OPERATOR_KEY = "root";
  protected static final Logger LOG = LoggerFactory.getLogger(AlertEvaluator.class);
  // 5 detection previews are running at the same time at most
  private static final int PARALLELISM = 5;
  // max time allowed for a preview task
  private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final ExecutorService executorService;
  private final DetectionPipelinePlanNodeFactory detectionPipelinePlanNodeFactory;

  @Inject
  public AlertEvaluator(
      final DetectionPipelinePlanNodeFactory detectionPipelinePlanNodeFactory) {
    this.detectionPipelinePlanNodeFactory = detectionPipelinePlanNodeFactory;
    this.executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  private void stop() {
    this.executorService.shutdownNow();
  }

  public Map<String, Map<String, DetectionEvaluationApi>> evaluate(
      final AlertEvaluationPlanApi request)
      throws ExecutionException {
    try {
      Map<String, DetectionPipelineResult> result = runPipeline(request);
      return toApi(result);
    } catch (Exception e) {
      handleAlertEvaluationException(e);
    }
    return null;
  }

  private Map<String, DetectionPipelineResult> runPipeline(final AlertEvaluationPlanApi request)
      throws Exception {
    Map<String, PlanNode> pipelinePlanNodes = new HashMap<>();
    for (DetectionPlanApi operator : request.getNodes()) {
      final String operatorName = operator.getPlanNodeName();
      pipelinePlanNodes.put(operatorName, detectionPipelinePlanNodeFactory
          .get(operatorName,
              pipelinePlanNodes,
              operator,
              request.getStart().getTime(),
              request.getEnd().getTime()));
    }
    return executorService.submit(() -> {
      PlanNode rootNode = pipelinePlanNodes.get(ROOT_OPERATOR_KEY);
      Map<String, DetectionPipelineResult> context = new HashMap<>();
      PlanExecutor.executePlanNode(pipelinePlanNodes, context, rootNode);
      final Map<String, DetectionPipelineResult> output = getOutput(context, rootNode);
      return output;
    }).get(TIMEOUT, TimeUnit.MILLISECONDS);
  }

  private Map<String, DetectionPipelineResult> getOutput(
      final Map<String, DetectionPipelineResult> context,
      final PlanNode rootNode) {
    Map<String, DetectionPipelineResult> results = new HashMap<>();
    for (String contextKey : context.keySet()) {
      if (PlanExecutor.getNodeFromContextKey(contextKey).equals(rootNode.getName())) {
        results.put(PlanExecutor.getOutputKeyFromContextKey(contextKey), context.get(contextKey));
      }
    }
    return results;
  }

  private Map<String, Map<String, DetectionEvaluationApi>> toApi(
      final Map<String, DetectionPipelineResult> outputMap) {

    final Map<String, Map<String, DetectionEvaluationApi>> resultMap = new HashMap<>();
    for (String key : outputMap.keySet()) {
      final DetectionPipelineResult result = outputMap.get(key);
      resultMap.put(key, detectionPipelineResultToApi(result));
    }
    return resultMap;
  }

  private Map<String, DetectionEvaluationApi> detectionPipelineResultToApi(
      final DetectionPipelineResult result) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    final List<DetectionResult> detectionResults = result.getDetectionResults();
    for (int i = 0; i < detectionResults.size(); i++) {
      DetectionEvaluationApi detectionEvaluationApi = detectionResults.get(i).toApi();
      map.put(String.valueOf(i), detectionEvaluationApi);
    }
    return map;
  }
}
