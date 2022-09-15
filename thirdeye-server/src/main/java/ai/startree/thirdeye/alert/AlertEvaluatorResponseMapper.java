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

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.operator.CombinerResult;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.DetectionDataApi;
import ai.startree.thirdeye.spi.api.DetectionEvaluationApi;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AlertEvaluatorResponseMapper {

  public static AlertEvaluationApi toAlertEvaluationApi(
      final Map<String, OperatorResult> outputMap) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    for (final String key : outputMap.keySet()) {
      final OperatorResult result = outputMap.get(key);
      final Map<String, DetectionEvaluationApi> detectionEvaluationApiMap = operatorResultToApi(
          result);
      detectionEvaluationApiMap.keySet()
          .forEach(apiKey -> map.put(key + "_" + apiKey, detectionEvaluationApiMap.get(apiKey)));
    }
    return new AlertEvaluationApi().setDetectionEvaluations(map);
  }

  private static DetectionDataApi getData(final OperatorResult operatorResult) {
    final var rawData = requireNonNull(operatorResult.getRawData(), "rawData is null");
    if (!rawData.isEmpty()) {
      return new DetectionDataApi().setRawData(rawData);
    }
    final TimeSeries timeSeries = requireNonNull(operatorResult.getTimeseries(),
        "timeseries is null");

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

  private static DetectionEvaluationApi toDetectionEvaluationApi(
      final OperatorResult operatorResult) {
    final DetectionEvaluationApi api = new DetectionEvaluationApi();
    final List<AnomalyApi> anomalyApis = new ArrayList<>();
    for (final MergedAnomalyResultDTO anomalyDto : operatorResult.getAnomalies()) {
      anomalyApis.add(ApiBeanMapper.toApi(anomalyDto));
    }
    api.setAnomalies(anomalyApis);
    api.setData(getData(operatorResult));
    api.setEnumerationItem(ApiBeanMapper.toApi(operatorResult.getEnumerationItem()));
    return api;
  }

  private static Map<String, DetectionEvaluationApi> operatorResultToApi(
      final OperatorResult result) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    if (result instanceof CombinerResult) {
      final List<OperatorResult> operatorResults = ((CombinerResult) result).getDetectionResults();
      for (int i = 0; i < operatorResults.size(); i++) {
        final DetectionEvaluationApi api = toDetectionEvaluationApi(operatorResults.get(i));
        map.put(String.valueOf(i), api);
      }
    } else {
      map.put(String.valueOf(0), toDetectionEvaluationApi(result));
    }

    return map;
  }
}
