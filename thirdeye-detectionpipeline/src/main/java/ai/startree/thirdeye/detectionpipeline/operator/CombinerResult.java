/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.detectionpipeline.operator;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CombinerResult implements OperatorResult {

  private final Map<String, OperatorResult> results;

  public CombinerResult(final Map<String, OperatorResult> results) {
    this.results = results;
  }

  @Override
  public long getLastTimestamp() {
    // returns max of all OperatorResult
    return results.values()
        .stream()
        .filter(Objects::nonNull)
        .map(OperatorResult::getLastTimestamp)
        .max(Long::compareTo)
        .orElse(-1L);
  }

  @Override
  public List<AnomalyDTO> getAnomalies() {
    // flatten anomalies of all OperatorResult
    return results.values()
        .stream()
        .flatMap(r -> r.getAnomalies().stream())
        .collect(Collectors.toList());
  }

  @Override
  public @Nullable Map<String, List> getRawData() {
    throw new UnsupportedOperationException(
        "Flattening not implemented yet. Cast and use CombinerResult getDetectionResults to loop.");
  }

  @Override
  public @Nullable TimeSeries getTimeseries() {
    throw new UnsupportedOperationException(
        "Flattening not implemented yet. Cast and use CombinerResult getDetectionResults to loop.");
  }

  public List<OperatorResult> getDetectionResults() {
    return new ArrayList<>(results.values());
  }

  public Map<String, OperatorResult> getResults() {
    return results;
  }
}
