package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.alert.AlertExceptionHandler.handleAlertEvaluationException;
import static org.apache.pinot.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.bool;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import org.apache.pinot.thirdeye.detection.v2.plan.DataFetcherPlanNode;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.AnomalyApi;
import org.apache.pinot.thirdeye.spi.api.DetectionDataApi;
import org.apache.pinot.thirdeye.spi.api.DetectionEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.EvaluationContextApi;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.RcaMetadataDTO;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.TimeseriesFilter;
import org.apache.pinot.thirdeye.spi.detection.v2.TimeseriesFilter.DimensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertEvaluatorV2 {

  public static final String EVALUATION_FILTERS_KEY = "evaluation.filters";

  protected static final Logger LOG = LoggerFactory.getLogger(AlertEvaluatorV2.class);
  private static final boolean USE_V1_FORMAT = true;

  // 5 detection previews are running at the same time at most
  private static final int PARALLELISM = 5;

  // max time allowed for a preview task
  private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final AlertTemplateRenderer alertTemplateRenderer;
  private final ExecutorService executorService;
  private final PlanExecutor planExecutor;

  @Inject
  public AlertEvaluatorV2(
      final AlertTemplateRenderer alertTemplateRenderer,
      final PlanExecutor planExecutor) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.planExecutor = planExecutor;

    executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  public static DetectionDataApi getData(final DetectionResult detectionResult) {
    final Map<String, List> rawData = detectionResult.getRawData();
    if (!rawData.isEmpty()) {
      return new DetectionDataApi().setRawData(rawData);
    }
    final TimeSeries timeSeries = detectionResult.getTimeseries();
    final DetectionDataApi api = new DetectionDataApi()
        .setCurrent(timeSeries.getCurrent().toList())
        .setExpected(timeSeries.getPredictedBaseline().toList())
        .setTimestamp(timeSeries.getTime().toList());

    if (timeSeries.hasLowerBound()) {
      api.setLowerBound(timeSeries.getPredictedLowerBound().toList());
    }

    if (timeSeries.hasUpperBound()) {
      api.setUpperBound(timeSeries.getPredictedUpperBound().toList());
    }
    return api;
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
    try {
      // apply template properties
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(
          request.getAlert(),
          request.getStart().getTime(),
          request.getEnd().getTime());

      // inject custom evaluation context
      injectEvaluationContext(templateWithProperties, request.getEvaluationContext());

      if (bool(request.isDryRun())) {
        return new AlertEvaluationApi()
            .setDryRun(true)
            .setAlert(new AlertApi()
                .setTemplate(toAlertTemplateApi(templateWithProperties)));
      }

      final Map<String, DetectionPipelineResult> result = executorService
          .submit(() -> planExecutor.runPipeline(
              templateWithProperties.getNodes(),
              request.getStart().getTime(),
              request.getEnd().getTime()
          ))
          .get(TIMEOUT, TimeUnit.MILLISECONDS);

      return toApi(result)
          .setAlert(new AlertApi().setTemplate(toAlertTemplateApi(templateWithProperties)));
    } catch (final WebApplicationException e) {
      throw e;
    } catch (final Exception e) {
      handleAlertEvaluationException(e);
    }
    return null;
  }

  private void injectEvaluationContext(final AlertTemplateDTO templateWithProperties,
      @Nullable final EvaluationContextApi evaluationContext) {
    if (evaluationContext == null) {
      return;
    }

    List<String> filters = evaluationContext.getFilters();
    if (filters != null) {
      injectFilters(templateWithProperties, filters);
    }

  }

  @VisibleForTesting
  protected void injectFilters(final AlertTemplateDTO templateWithProperties,
      List<String> filters) {
    if (filters.isEmpty()) {
      return;
    }
    final RcaMetadataDTO rcaMetadataDTO = Objects.requireNonNull(templateWithProperties.getRca(),
        "rca not found in alert config.");
    final String dataset = Objects.requireNonNull(rcaMetadataDTO.getDataset(),
        "rca$dataset not found in alert config.");

    final List<TimeseriesFilter> timeseriesFilters = filters
        .stream()
        .map(Predicate::parseFilterPredicate)
        .map(p -> TimeseriesFilter.of(p, getDimensionType(p.getLhs(), dataset), dataset))
        .collect(Collectors.toList());

    templateWithProperties.getNodes().forEach(n -> addFilters(n, timeseriesFilters));
  }

  // fixme datatype from metricDTO is always double + abstraction metric/dimension needs refactoring
  private DimensionType getDimensionType(final String metric, final String dataset) {
    // first version: assume dimension is always of type String
    // todo fetch info from database with a DAO
    return DimensionType.STRING;
  }

  private void addFilters(PlanNodeBean planNodeBean, List<TimeseriesFilter> filters) {
    if (planNodeBean.getType().equals(new DataFetcherPlanNode().getType())) {
      if (planNodeBean.getParams() == null) {
        planNodeBean.setParams(new HashMap<>());
      }
      planNodeBean.getParams().put(EVALUATION_FILTERS_KEY, filters);
    }
  }

  private AlertEvaluationApi toApi(final Map<String, DetectionPipelineResult> outputMap) {
    final Map<String, Map<String, DetectionEvaluationApi>> resultMap = new HashMap<>();
    for (final String key : outputMap.keySet()) {
      final DetectionPipelineResult result = outputMap.get(key);
      resultMap.put(key, detectionPipelineResultToApi(result));
    }
    if (USE_V1_FORMAT) {
      return toV1Format(resultMap);
    }
    return new AlertEvaluationApi().setEvaluations(resultMap);
  }

  private AlertEvaluationApi toV1Format(
      final Map<String, Map<String, DetectionEvaluationApi>> v2Result) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    for (final String key : v2Result.keySet()) {
      final Map<String, DetectionEvaluationApi> detectionEvaluationApiMap = v2Result.get(key);
      detectionEvaluationApiMap
          .keySet()
          .forEach(apiKey -> map.put(key + "_" + apiKey, detectionEvaluationApiMap.get(apiKey)));
    }
    return new AlertEvaluationApi().setDetectionEvaluations(map);
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
