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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.stream.Collectors.toList;

import ai.startree.thirdeye.detectionpipeline.operator.CombinerResult;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.DetectionDataApi;
import ai.startree.thirdeye.spi.api.DetectionEvaluationApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AlertEvaluatorResponseMapper {

  public static AlertEvaluationApi toAlertEvaluationApi(
      final Map<String, OperatorResult> outputMap) {
    final Map<String, DetectionEvaluationApi> map = new HashMap<>();
    for (final Entry<String, OperatorResult> entry : outputMap.entrySet()) {
      final Map<String, DetectionEvaluationApi> detectionEvaluationApiMap = operatorResultToApi(
          entry.getValue());
      detectionEvaluationApiMap.keySet()
          .forEach(apiKey -> map.put(entry.getKey() + "_" + apiKey,
              detectionEvaluationApiMap.get(apiKey)));
    }
    return new AlertEvaluationApi().setDetectionEvaluations(map);
  }

  private static DetectionDataApi getData(final OperatorResult operatorResult) {
    final var rawData = operatorResult.getRawData();
    if (rawData != null && !rawData.isEmpty()) {
      return new DetectionDataApi().setRawData(rawData);
    }

    final TimeSeries timeSeries = operatorResult.getTimeseries();
    if (timeSeries != null) {
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
    return null;
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
    } else if (result.getEnumerationItems() != null) {
      final List<EnumerationItemDTO> enumerationItems = result.getEnumerationItems();
      for (int i = 0; i < enumerationItems.size(); i++) {
        final EnumerationItemApi api = ApiBeanMapper.toApi(enumerationItems.get(i));
        map.put(String.valueOf(i), new DetectionEvaluationApi()
            .setIdx(i)
            .setEnumerationItem(api));
      }
    } else {
      map.put(String.valueOf(0), toDetectionEvaluationApi(result));
    }

    return map;
  }

  private static DetectionEvaluationApi toDetectionEvaluationApi(
      final OperatorResult operatorResult) {
    final DetectionEvaluationApi api = new DetectionEvaluationApi();

    optional(operatorResult.getAnomalies())
        .map(anomalies -> anomalies.stream()
            .map(ApiBeanMapper::toApi)
            .collect(toList()))
        .ifPresent(api::setAnomalies);

    api.setData(getData(operatorResult));
    api.setEnumerationItem(ApiBeanMapper.toApi(operatorResult.getEnumerationItem()));
    return api;
  }
}
