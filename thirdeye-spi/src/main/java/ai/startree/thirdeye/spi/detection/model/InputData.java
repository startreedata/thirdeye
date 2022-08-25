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
package ai.startree.thirdeye.spi.detection.model;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;

/**
 * Input data for each detection stage
 */
public class InputData {

  final InputDataSpec dataSpec;
  final Map<MetricSlice, DataFrame> timeseries;
  final Map<MetricSlice, DataFrame> aggregates;
  final Multimap<AnomalySlice, MergedAnomalyResultDTO> anomalies;
  final Multimap<EventSlice, EventDTO> events;
  final Map<Long, MetricConfigDTO> metrics;
  final Map<String, DatasetConfigDTO> datasets;

  /**
   * The data set config dtos for metric ids
   *
   * @see InputDataSpec#withMetricIdsForDataset(Collection)
   */
  final Map<Long, DatasetConfigDTO> datasetForMetricId;

  public InputData(final InputDataSpec spec,
      final Map<MetricSlice, DataFrame> timeseries,
      final Map<MetricSlice, DataFrame> aggregates,
      final Multimap<AnomalySlice, MergedAnomalyResultDTO> anomalies,
      final Multimap<EventSlice, EventDTO> events,
      final Map<Long, MetricConfigDTO> metrics,
      final Map<String, DatasetConfigDTO> datasets,
      final Map<Long, DatasetConfigDTO> datasetForMetricId) {
    dataSpec = spec;
    this.timeseries = timeseries;
    this.aggregates = aggregates;
    this.anomalies = anomalies;
    this.events = events;
    this.metrics = metrics;
    this.datasets = datasets;
    this.datasetForMetricId = datasetForMetricId;
  }

  public Map<MetricSlice, DataFrame> getTimeseries() {
    return timeseries;
  }

  public Map<MetricSlice, DataFrame> getAggregates() {
    return aggregates;
  }

  public Multimap<AnomalySlice, MergedAnomalyResultDTO> getAnomalies() {
    return anomalies;
  }

  public Multimap<EventSlice, EventDTO> getEvents() {
    return events;
  }

  public Map<Long, MetricConfigDTO> getMetrics() {
    return metrics;
  }

  public Map<String, DatasetConfigDTO> getDatasets() {
    return datasets;
  }
}
