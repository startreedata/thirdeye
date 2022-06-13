/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.model.AnomalySlice;
import ai.startree.thirdeye.spi.detection.model.EvaluationSlice;
import ai.startree.thirdeye.spi.detection.model.EventSlice;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Centralized data source for anomaly detection algorithms. All data used by any
 * algorithm <b>MUST</b> be obtained through this interface to maintain loose coupling.
 *
 * <br/><b>NOTE:</b> extend this interface in case necessary data cannot be obtained
 * through one of the existing methods.
 */
@Deprecated
public interface DataProvider {

  /**
   * Returns a map of granular timeseries (keyed by slice) for a given set of slices.
   * The format of the DataFrame follows the standard convention of DataFrameUtils.
   *
   * Note: The slices are treated left inclusive and right exclusive
   *
   * @param slices metric slices
   * @return map of timeseries (keyed by slice)
   * @see MetricSlice
   */
  Map<MetricSlice, DataFrame> fetchTimeseries(Collection<MetricSlice> slices);

  /**
   * Returns a map of aggregation values (keyed by slice) for a given set of slices,
   * grouped by the given dimensions.
   * The format of the DataFrame follows the standard convention of DataFrameUtils.
   *
   * @param slices metric slices
   * @param dimensions dimensions to group by
   * @param limit max number of records to return ordered by metric value
   *     no limitation if it is a non-positive number
   * @return map of aggregation values (keyed by slice)
   * @see MetricSlice
   */
  Map<MetricSlice, DataFrame> fetchAggregates(Collection<MetricSlice> slices,
      List<String> dimensions, int limit);

  /**
   * Returns a multimap of anomalies (keyed by slice) for a given set of slices.
   *
   * @param slices anomaly slice
   * @return multimap of anomalies (keyed by slice)
   * @see MergedAnomalyResultDTO
   * @see AnomalySlice
   */
  Multimap<AnomalySlice, MergedAnomalyResultDTO> fetchAnomalies(Collection<AnomalySlice> slices);

  /**
   * Returns a multimap of events (keyed by slice) for a given set of slices.
   *
   * @param slices event slice
   * @return multimap of events (keyed by slice)
   * @see EventDTO
   * @see EventSlice
   */
  Multimap<EventSlice, EventDTO> fetchEvents(Collection<EventSlice> slices);

  /**
   * Returns a map of metric configs (keyed by id) for a given set of ids.
   *
   * @param ids metric config ids
   * @return map of metric configs (keyed by id)
   * @see MetricConfigDTO
   */
  Map<Long, MetricConfigDTO> fetchMetrics(Collection<Long> ids);

  /**
   * Returns a map of dataset configs (keyed by id) for a given set of dataset names.
   *
   * @param datasetNames dataset config names
   * @return map of dataset configs (keyed by dataset name)
   * @see DatasetConfigDTO
   */
  Map<String, DatasetConfigDTO> fetchDatasets(Collection<String> datasetNames);

  /**
   * Returns a metricConfigDTO for a given metric name.
   *
   * @param metricName metric name
   * @param datasetName dataset name
   * @return map of dataset configs (keyed by dataset name)
   * @see MetricConfigDTO
   */
  MetricConfigDTO fetchMetric(String metricName, String datasetName);

  /**
   * Returns a multimap of evaluations (keyed by the evaluations slice) for a given set of
   * evaluations slices.
   *
   * @param evaluationSlices the evaluation slices
   * @param configId configId
   * @return a multimap of evaluations (keyed by the evaluations slice)
   * @see Evaluation
   */
  Multimap<EvaluationSlice, EvaluationDTO> fetchEvaluations(
      Collection<EvaluationSlice> evaluationSlices, long configId);

  default List<DatasetConfigDTO> fetchDatasetByDisplayName(String datasetDisplayName) {
    throw new UnsupportedOperationException();
  }
}
