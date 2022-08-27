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
package ai.startree.thirdeye.core;

import static ai.startree.thirdeye.core.ExceptionHandler.handleAlertEvaluationException;
import static ai.startree.thirdeye.mapper.ApiBeanMapper.toAlertTemplateApi;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_CONFIGURATION_FIELD;
import static ai.startree.thirdeye.spi.datalayer.Predicate.parseAndCombinePredicates;
import static ai.startree.thirdeye.spi.util.SpiUtils.bool;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator;
import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.datasource.calcite.QueryPredicate;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.detectionpipeline.operator.CombinerOperator.CombinerResult;
import ai.startree.thirdeye.detectionpipeline.plan.DataFetcherPlanNode;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.DetectionDataApi;
import ai.startree.thirdeye.spi.api.DetectionEvaluationApi;
import ai.startree.thirdeye.spi.api.EvaluationContextApi;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.DetectionResult;
import ai.startree.thirdeye.spi.metric.DimensionType;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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

  @Inject
  public AlertEvaluator(
      final AlertTemplateRenderer alertTemplateRenderer,
      final PlanExecutor planExecutor,
      final AlertDetectionIntervalCalculator alertDetectionIntervalCalculator) {
    this.alertTemplateRenderer = alertTemplateRenderer;
    this.planExecutor = planExecutor;
    this.alertDetectionIntervalCalculator = alertDetectionIntervalCalculator;

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
    api.setEnumerationItem(ApiBeanMapper.toApi(detectionResult.getEnumerationItem()));
    return api;
  }

  private void stop() {
    executorService.shutdownNow();
  }

  public AlertEvaluationApi evaluate(final AlertEvaluationApi request)
      throws ExecutionException {
    try {
      Interval detectionInterval = computeDetectionInterval(request);

      // apply template properties
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(request.getAlert(),
          detectionInterval);

      // inject custom evaluation context
      injectEvaluationContext(templateWithProperties, request.getEvaluationContext());

      if (bool(request.isDryRun())) {
        return new AlertEvaluationApi()
            .setDryRun(true)
            .setAlert(new AlertApi()
                .setTemplate(toAlertTemplateApi(templateWithProperties)));
      }

      final Map<String, DetectionResult> result = executorService
          .submit(() -> planExecutor.runPipeline(templateWithProperties.getNodes(),
              detectionInterval))
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

  private Interval computeDetectionInterval(final AlertEvaluationApi request)
      throws IOException, ClassNotFoundException {
    // this method only exists to catch exception and translate into a TE exception
    Interval detectionInterval;
    detectionInterval = alertDetectionIntervalCalculator.getCorrectedInterval(request.getAlert(),
        request.getStart().getTime(),
        request.getEnd().getTime());
    return detectionInterval;
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
    final AlertMetadataDTO alertMetadataDTO = ensureExists(templateWithProperties.getMetadata(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata");
    final DatasetConfigDTO datasetConfigDTO = ensureExists(alertMetadataDTO.getDataset(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata$dataset");
    final String dataset = ensureExists(datasetConfigDTO.getDataset(),
        ERR_MISSING_CONFIGURATION_FIELD,
        "metadata$dataset$name");

    final List<QueryPredicate> timeseriesFilters = parseAndCombinePredicates(filters).stream()
        .map(p -> QueryPredicate.of(p, getDimensionType(p.getLhs(), dataset), dataset))
        .collect(Collectors.toList());

    templateWithProperties.getNodes().forEach(n -> addFilters(n, timeseriesFilters));
  }

  // fixme datatype from metricDTO is always double + abstraction metric/dimension needs refactoring
  private DimensionType getDimensionType(final String metric, final String dataset) {
    // first version: assume dimension is always of type String
    // todo fetch info from database with a DAO
    return DimensionType.STRING;
  }

  private void addFilters(PlanNodeBean planNodeBean, List<QueryPredicate> filters) {
    if (planNodeBean.getType().equals(new DataFetcherPlanNode().getType())) {
      if (planNodeBean.getParams() == null) {
        planNodeBean.setParams(new TemplatableMap<>());
      }
      planNodeBean.getParams().putValue(Constants.EVALUATION_FILTERS_KEY, filters);
    }
  }

  private AlertEvaluationApi toApi(final Map<String, DetectionResult> outputMap) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    for (final String key : outputMap.keySet()) {
      final DetectionResult result = outputMap.get(key);
      final Map<String, DetectionEvaluationApi> detectionEvaluationApiMap = detectionPipelineResultToApi(
          result);
      detectionEvaluationApiMap.keySet()
          .forEach(apiKey -> map.put(key + "_" + apiKey, detectionEvaluationApiMap.get(apiKey)));
    }
    return new AlertEvaluationApi().setDetectionEvaluations(map);
  }

  private Map<String, DetectionEvaluationApi> detectionPipelineResultToApi(
      final DetectionResult result) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    if (result instanceof CombinerResult) {
      final List<DetectionResult> detectionResults = ((CombinerResult) result).getDetectionResults();
      for (int i = 0; i < detectionResults.size(); i++) {
        final DetectionEvaluationApi detectionEvaluationApi = toApi(detectionResults.get(i));
        map.put(String.valueOf(i), detectionEvaluationApi);
      }
    } else {
      map.put(String.valueOf(0), toApi(result));
    }

    return map;
  }
}
