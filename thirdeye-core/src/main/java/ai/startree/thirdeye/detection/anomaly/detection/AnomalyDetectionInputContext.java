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
package ai.startree.thirdeye.detection.anomaly.detection;

import ai.startree.thirdeye.metric.MetricTimeSeries;
import ai.startree.thirdeye.metric.ScalingFactor;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnomalyDetectionInputContext {

  private Map<DimensionMap, MetricTimeSeries> dimensionMapMetricTimeSeriesMap = Collections
      .emptyMap();
  private MetricTimeSeries globalMetric;
  private ListMultimap<DimensionMap, MergedAnomalyResultDTO> knownMergedAnomalies = ArrayListMultimap
      .create();
  private List<ScalingFactor> scalingFactors = Collections.emptyList();

  public Map<DimensionMap, MetricTimeSeries> getDimensionMapMetricTimeSeriesMap() {
    return dimensionMapMetricTimeSeriesMap;
  }

  public void setDimensionMapMetricTimeSeriesMap(
      Map<DimensionMap, MetricTimeSeries> dimensionMapMetricTimeSeriesMap) {
    this.dimensionMapMetricTimeSeriesMap = dimensionMapMetricTimeSeriesMap;
  }

  public ListMultimap<DimensionMap, MergedAnomalyResultDTO> getKnownMergedAnomalies() {
    return knownMergedAnomalies;
  }

  public void setKnownMergedAnomalies(
      ListMultimap<DimensionMap, MergedAnomalyResultDTO> knownMergedAnomalies) {
    this.knownMergedAnomalies = knownMergedAnomalies;
  }

  public List<ScalingFactor> getScalingFactors() {
    return scalingFactors;
  }

  public void setScalingFactors(List<ScalingFactor> scalingFactors) {
    this.scalingFactors = scalingFactors;
  }

  public MetricTimeSeries getGlobalMetric() {
    return globalMetric;
  }

  public void setGlobalMetric(MetricTimeSeries globalMetric) {
    this.globalMetric = globalMetric;
  }
}
