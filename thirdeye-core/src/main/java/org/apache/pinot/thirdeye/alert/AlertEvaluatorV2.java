package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.alert.AlertExceptionHandler.handleAlertEvaluationException;
import static org.apache.pinot.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static org.apache.pinot.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.WebApplicationException;
import org.apache.pinot.thirdeye.detection.v2.plan.DetectionPipelinePlanNodeFactory;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.AlertTemplateApi;
import org.apache.pinot.thirdeye.spi.api.AnomalyApi;
import org.apache.pinot.thirdeye.spi.api.DetectionDataApi;
import org.apache.pinot.thirdeye.spi.api.DetectionEvaluationApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.apache.pinot.thirdeye.util.GroovyTemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertEvaluatorV2 {

  public static final String ROOT_OPERATOR_KEY = "root";
  protected static final Logger LOG = LoggerFactory.getLogger(AlertEvaluatorV2.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  // 5 detection previews are running at the same time at most
  private static final int PARALLELISM = 5;

  // max time allowed for a preview task
  private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final AlertTemplateManager alertTemplateManager;
  private final ExecutorService executorService;
  private final DetectionPipelinePlanNodeFactory detectionPipelinePlanNodeFactory;

  @Inject
  public AlertEvaluatorV2(
      final AlertTemplateManager alertTemplateManager,
      final DetectionPipelinePlanNodeFactory detectionPipelinePlanNodeFactory) {
    this.alertTemplateManager = alertTemplateManager;
    this.detectionPipelinePlanNodeFactory = detectionPipelinePlanNodeFactory;
    executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  public static DetectionDataApi getData(final DetectionResult detectionResult) {
    final Map<String, List> rawData = detectionResult.getRawData();
    if (!rawData.isEmpty()) {
      return new DetectionDataApi().setRawData(rawData);
    }
    final TimeSeries timeSeries = detectionResult.getTimeseries();
    final DetectionDataApi detectionDataApi = new DetectionDataApi();
    detectionDataApi.setCurrent(timeSeries.getCurrent().toList());
    detectionDataApi.setExpected(timeSeries.getPredictedBaseline().toList());
    detectionDataApi.setTimestamp(timeSeries.getTime().toList());
    if (timeSeries.hasLowerBound()) {
      detectionDataApi.setLowerBound(timeSeries.getPredictedLowerBound().toList());
    }
    if (timeSeries.hasUpperBound()) {
      detectionDataApi.setUpperBound(timeSeries.getPredictedUpperBound().toList());
    }
    return detectionDataApi;
  }

  public static DetectionEvaluationApi toApi(final DetectionResult detectionResult) {
    final DetectionEvaluationApi api = new DetectionEvaluationApi();
    final List<AnomalyApi> anomalyApis = new ArrayList<>();
    for (final MergedAnomalyResultDTO anomalyDto : detectionResult.getAnomalies()) {
      anomalyApis.add(ApiBeanMapper.toApi(anomalyDto));
    }
    api.setAnomalies(anomalyApis);
    api.setData(getData(detectionResult));
    return api;
  }

  private void stop() {
    executorService.shutdownNow();
  }

  public AlertEvaluationApi evaluate(final AlertEvaluationApi request)
      throws ExecutionException {
    final AlertApi alert = request.getAlert();
    ensureExists(alert, ERR_OBJECT_DOES_NOT_EXIST, "alert body is null");

    final AlertTemplateApi templateApi = alert.getTemplate();
    ensureExists(templateApi, ERR_OBJECT_DOES_NOT_EXIST, "alert template body is null");

    try {
      final AlertTemplateDTO template = getTemplate(templateApi);
      final Map<String, Object> templateProperties = alert.getTemplateProperties();
      final AlertTemplateDTO templateWithProperties = applyContext(template, templateProperties);

      if (request.isDryRun()) {
        return new AlertEvaluationApi()
            .setDryRun(true)
            .setAlert(new AlertApi()
                .setTemplate(toAlertTemplateApi(templateWithProperties)));
      }

      final Map<String, DetectionPipelineResult> result = runPipeline(
          templateWithProperties.getNodes(),
          request.getStart(),
          request.getEnd()
      );
      return toApi(result);
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      handleAlertEvaluationException(e);
    }
    return null;
  }

  private Map<String, DetectionPipelineResult> runPipeline(final List<PlanNodeBean> nodes,
      final Date start,
      final Date end)
      throws InterruptedException, ExecutionException, TimeoutException {
    final Map<String, PlanNode> pipelinePlanNodes = new HashMap<>();
    for (final PlanNodeBean operator : nodes) {
      final String operatorName = operator.getName();
      final PlanNode planNode = detectionPipelinePlanNodeFactory.get(
          operatorName,
          pipelinePlanNodes,
          operator,
          start.getTime(),
          end.getTime());

      pipelinePlanNodes.put(operatorName, planNode);
    }
    return executorService
        .submit(() -> runPipelineInternal(pipelinePlanNodes))
        .get(TIMEOUT, TimeUnit.MILLISECONDS);
  }

  private AlertTemplateDTO applyContext(final AlertTemplateDTO template,
      final Map<String, Object> templateProperties) throws IOException, ClassNotFoundException {
    if (templateProperties == null || templateProperties.size() == 0) {
      /* Nothing to replace. Skip running the engine */
      return template;
    }

    final String jsonString = OBJECT_MAPPER.writeValueAsString(template);
    return GroovyTemplateUtils.applyContextToTemplate(jsonString,
        templateProperties,
        AlertTemplateDTO.class);
  }

  private Map<String, DetectionPipelineResult> runPipelineInternal(
      final Map<String, PlanNode> pipelinePlanNodes) throws Exception {
    final Map<String, DetectionPipelineResult> context = new HashMap<>();

    /* Execute the DAG */
    final PlanNode rootNode = pipelinePlanNodes.get(ROOT_OPERATOR_KEY);
    PlanExecutor.executePlanNode(pipelinePlanNodes, context, rootNode);

    /* Return the output */
    return getOutput(context, rootNode);
  }

  private AlertTemplateDTO getTemplate(final AlertTemplateApi templateApi) {
    final Long id = templateApi.getId();
    if (id != null) {
      return alertTemplateManager.findById(id);
    }

    final String name = templateApi.getName();
    if (name != null) {
      final List<AlertTemplateDTO> byName = alertTemplateManager.findByName(name);
      ensure(byName.size() == 1, ERR_OBJECT_DOES_NOT_EXIST, "template not found: " + name);
      return byName.get(0);
    }

    return ApiBeanMapper.toAlertTemplateDto(templateApi);
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

  private AlertEvaluationApi toApi(final Map<String, DetectionPipelineResult> outputMap) {
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
      final DetectionEvaluationApi detectionEvaluationApi = toApi(detectionResults.get(i));
      map.put(String.valueOf(i), detectionEvaluationApi);
    }
    return map;
  }
}
