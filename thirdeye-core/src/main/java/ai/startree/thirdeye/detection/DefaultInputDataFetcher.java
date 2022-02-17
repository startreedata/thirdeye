/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.model.AnomalySlice;
import ai.startree.thirdeye.spi.detection.model.EvaluationSlice;
import ai.startree.thirdeye.spi.detection.model.EventSlice;
import ai.startree.thirdeye.spi.detection.model.InputData;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Input data fetcher.
 * For components to fetch the input data it need.
 */
public class DefaultInputDataFetcher implements InputDataFetcher {

  private final DataProvider provider;
  private final long configId;

  public DefaultInputDataFetcher(DataProvider provider, long configId) {
    this.provider = provider;
    this.configId = configId;
  }

  /**
   * Fetch data for input data spec
   */
  public InputData fetchData(InputDataSpec inputDataSpec) {
    Map<MetricSlice, DataFrame> timeseries = provider
        .fetchTimeseries(inputDataSpec.getTimeseriesSlices());
    Map<MetricSlice, DataFrame> aggregates = provider
        .fetchAggregates(inputDataSpec.getAggregateSlices(), Collections.emptyList(), -1);

    Collection<AnomalySlice> slicesWithConfigId = new HashSet<>();
    for (AnomalySlice slice : inputDataSpec.getAnomalySlices()) {
      slicesWithConfigId.add(slice.withDetectionId(configId));
    }
    Multimap<AnomalySlice, MergedAnomalyResultDTO> existingAnomalies = provider
        .fetchAnomalies(slicesWithConfigId);

    Multimap<EventSlice, EventDTO> events = provider.fetchEvents(inputDataSpec.getEventSlices());
    Map<Long, MetricConfigDTO> metrics = provider.fetchMetrics(inputDataSpec.getMetricIds());
    Map<String, DatasetConfigDTO> datasets = provider
        .fetchDatasets(inputDataSpec.getDatasetNames());
    Multimap<EvaluationSlice, EvaluationDTO> evaluations = provider
        .fetchEvaluations(inputDataSpec.getEvaluationSlices(), configId);
    Map<Long, DatasetConfigDTO> datasetForMetricId = fetchDatasetForMetricId(
        inputDataSpec.getMetricIdsForDatasets());
    Map<InputDataSpec.MetricAndDatasetName, MetricConfigDTO> metricForMetricAndDatasetName = fetchMetricForDatasetAndMetricNames(
        inputDataSpec.getMetricAndDatasetNames());
    return new InputData(inputDataSpec, timeseries, aggregates, existingAnomalies, events, metrics,
        datasets, evaluations, datasetForMetricId, metricForMetricAndDatasetName);
  }

  private Map<InputDataSpec.MetricAndDatasetName, MetricConfigDTO> fetchMetricForDatasetAndMetricNames(
      Collection<InputDataSpec.MetricAndDatasetName> metricNameAndDatasetNames) {
    Map<InputDataSpec.MetricAndDatasetName, MetricConfigDTO> result = new HashMap<>();
    for (InputDataSpec.MetricAndDatasetName pair : metricNameAndDatasetNames) {
      result.put(pair, this.provider.fetchMetric(pair.getMetricName(), pair.getDatasetName()));
    }
    return result;
  }

  private Map<Long, DatasetConfigDTO> fetchDatasetForMetricId(
      Collection<Long> metricIdsForDatasets) {
    Map<Long, MetricConfigDTO> metrics = provider.fetchMetrics(metricIdsForDatasets);
    Map<Long, String> metricIdToDataSet = new HashMap<>();
    for (Map.Entry<Long, MetricConfigDTO> entry : metrics.entrySet()) {
      metricIdToDataSet.put(entry.getKey(), entry.getValue().getDataset());
    }
    Map<String, DatasetConfigDTO> datasets = provider.fetchDatasets(metricIdToDataSet.values());
    Map<Long, DatasetConfigDTO> result = new HashMap<>();
    for (Map.Entry<Long, MetricConfigDTO> entry : metrics.entrySet()) {
      result.put(entry.getKey(), datasets.get(entry.getValue().getDataset()));
    }
    return result;
  }
}
