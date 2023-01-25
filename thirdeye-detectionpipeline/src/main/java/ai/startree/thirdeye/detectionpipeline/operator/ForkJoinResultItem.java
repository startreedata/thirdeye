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

import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ForkJoinResultItem {

  private final EnumerationItemDTO enumerationItem;
  private final Map<String, OperatorResult> results;
  private final DetectionPipelineContext detectionPipelineContext;

  public ForkJoinResultItem(final EnumerationItemDTO enumerationItem,
      final Map<String, OperatorResult> results,
      DetectionPipelineContext detectionPipelineContext) {
    this.enumerationItem = enumerationItem;
    this.detectionPipelineContext = detectionPipelineContext;
    this.results = process(results);
  }

  public Map<String, OperatorResult> getResults() {
    return results;
  }


  private Map<String, OperatorResult> process(final Map<String, OperatorResult> outputs) {
    if (outputs == null) {
      return null;
    }

    return outputs.entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, e -> conditionalClone(e.getValue()), (a, b) -> b));
  }

  /**
   * The main intent of cloning the operator result is to skip the timeseries if not required.
   * This intentionally loses the src result and forgets references so that they can be GC'd later.
   *
   * @param src operator result
   * @return cloned operator result according to {@link DetectionPipelineContext} instance
   */
  private OperatorResult conditionalClone(final OperatorResult src) {
    final long lastTimestamp = src.getLastTimestamp();
    final List<AnomalyDTO> anomalies = src.getAnomalies();
    // below is written this way to ensure it breaks if a new DetectionPipelineUsage value is added
    final TimeSeries timeSeries;
    if (detectionPipelineContext.getUsage().equals(DetectionPipelineUsage.EVALUATION)) {
      timeSeries = src.getTimeseries();
    } else if (detectionPipelineContext.getUsage().equals(DetectionPipelineUsage.DETECTION)) {
      // allows garbage collection
      timeSeries = null;
    } else {
      throw new UnsupportedOperationException("Unsupported detection pipeline usage: " + detectionPipelineContext.getUsage());
    }
    final Map<String, List> rawData = src.getRawData();

    return new OperatorResult() {
      @Override
      public long getLastTimestamp() {
        return lastTimestamp;
      }

      @Override
      public @Nullable List<AnomalyDTO> getAnomalies() {
        return anomalies;
      }

      @Override
      public @Nullable EnumerationItemDTO getEnumerationItem() {
        return enumerationItem;
      }

      @Override
      public @Nullable Map<String, List> getRawData() {
        return rawData;
      }

      @Override
      public @Nullable TimeSeries getTimeseries() {
        return timeSeries;
      }
    };
  }

}
