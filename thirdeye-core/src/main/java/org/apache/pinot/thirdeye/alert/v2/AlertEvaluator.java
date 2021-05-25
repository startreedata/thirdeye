package org.apache.pinot.thirdeye.alert.v2;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.badRequest;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.serverError;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.statusListApi;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_DATA_UNAVAILABLE;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_TIMEOUT;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.pinot.thirdeye.api.v2.AlertEvaluationPlanApi;
import org.apache.pinot.thirdeye.api.v2.DetectionPlanApi;
import org.apache.pinot.thirdeye.detection.DataProviderException;
import org.apache.pinot.thirdeye.detection.DetectionPipelineException;
import org.apache.pinot.thirdeye.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.detection.v2.PlanNode;
import org.apache.pinot.thirdeye.detection.v2.plan.DetectionPipelinePlanNodeFactory;
import org.apache.pinot.thirdeye.spi.ThirdEyeException;
import org.apache.pinot.thirdeye.spi.api.DetectionEvaluationApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertEvaluator {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertEvaluator.class);

  public static final String ROOT_OPERATOR_KEY = "root";
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
    } catch (ThirdEyeException e) {
      throw badRequest(statusListApi(e.getStatus(), e.getMessage()));
    } catch (InterruptedException e) {
      LOG.error("Error occurred during evaluate", e);
      throw serverError(ERR_UNKNOWN, e.getMessage());
    } catch (TimeoutException e) {
      LOG.error("Error occurred during evaluate", e);
      throw serverError(ERR_TIMEOUT);
    } catch (ExecutionException e) {
      LOG.error("Error occurred during evaluate", e);
      handleExecutionException(e);
      throw e;
    } catch (Exception e) {
      LOG.error("Error occurred during evaluate", e);
      throw serverError(ERR_UNKNOWN);
    }
  }

  private void handleExecutionException(final ExecutionException e) {
    final Throwable cause = e.getCause();
    if (cause instanceof DetectionPipelineException) {
      final Throwable innerCause = cause.getCause();
      if (innerCause instanceof DataProviderException) {
        throw serverError(ERR_DATA_UNAVAILABLE, innerCause.getMessage());
      }
      throw serverError(ERR_UNKNOWN, cause.getMessage());
    }
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
      return getOutput(context, rootNode);
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
    // TODO: implement this
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
