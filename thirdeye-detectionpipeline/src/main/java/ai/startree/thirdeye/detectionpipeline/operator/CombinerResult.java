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

package ai.startree.thirdeye.detectionpipeline.operator;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.DetectionResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CombinerResult implements DetectionResult {

  private final Map<String, DetectionResult> results;

  public CombinerResult(final Map<String, DetectionResult> results) {
    this.results = results;
  }

  @Override
  public long getLastTimestamp() {
    // returns max of all DetectionResult
    return results.values()
        .stream()
        .filter(Objects::nonNull)
        .map(DetectionResult::getLastTimestamp)
        .max(Long::compareTo)
        .orElse(-1L);
  }

  @Override
  public List<MergedAnomalyResultDTO> getAnomalies() {
    // flatten anomalies of all DetectionResult
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

  public List<DetectionResult> getDetectionResults() {
    return results.values().stream()
        // fixme cyril suvodeep not sure to understand why only WrappedAnomalyDetectionResult are returned
        .filter(r -> r instanceof WrappedAnomalyDetectionResult)
        .collect(Collectors.toList());
  }

  public Map<String, DetectionResult> getResults() {
    return results;
  }
}
