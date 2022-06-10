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
package ai.startree.thirdeye.task;

import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DetectionPipelineResultWrapper implements DetectionPipelineResult {

  private final AlertDTO alert;
  private final DetectionPipelineResult delegate;

  public DetectionPipelineResultWrapper(
      final AlertDTO alert,
      final DetectionPipelineResult delegate) {
    this.alert = alert;
    this.delegate = delegate;
  }

  @Override
  public List<DetectionResult> getDetectionResults() {
    return delegate.getDetectionResults();
  }

  /**
   * Find the max out of all observed timestamps.
   *
   * @return the last timestamp observed in the data.
   */
  @Override
  public long getLastTimestamp() {
    return delegate
        .getDetectionResults()
        .stream()
        .filter(Objects::nonNull)
        .map(DetectionResult::getTimeseries)
        .filter(Objects::nonNull)
        .map(TimeSeries::getTime)
        .filter(Objects::nonNull)
        .filter(longSeries -> longSeries.size() > 0)
        .map(longSeries -> longSeries.get(longSeries.size() - 1))
        .max(Long::compareTo)
        .orElse(-1L);
  }

  /**
   * Collect anomalies from the results.
   *
   * @return list of anomalies found.
   */
  @Override
  public List<MergedAnomalyResultDTO> getAnomalies() {
    return delegate
        .getDetectionResults()
        .stream()
        .map(DetectionResult::getAnomalies)
        .flatMap(Collection::stream)
        .peek(anomaly -> anomaly.setDetectionConfigId(alert.getId()))
        .collect(Collectors.toList());
  }

  /**
   * TODO spyne to be implemented later.
   *
   * @return empty list.
   */
  @Override
  public List<EvaluationDTO> getEvaluations() {
    return Collections.emptyList();
  }
}
