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
package ai.startree.thirdeye;

import ai.startree.thirdeye.anomaly.AnomaliesCacheBuilder;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EvaluationManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.DetectionUtils;
import ai.startree.thirdeye.spi.detection.model.AnomalySlice;
import ai.startree.thirdeye.spi.detection.model.EvaluationSlice;
import ai.startree.thirdeye.spi.detection.model.EventSlice;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultDataProvider implements DataProvider {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultDataProvider.class);

  private final MetricConfigManager metricDAO;
  private final DatasetConfigManager datasetDAO;
  private final EventManager eventDAO;
  private final EvaluationManager evaluationDAO;

  private final AnomaliesCacheBuilder anomaliesCache;

  @Inject
  public DefaultDataProvider(MetricConfigManager metricDAO,
      DatasetConfigManager datasetDAO,
      EventManager eventDAO,
      EvaluationManager evaluationDAO,
      AnomaliesCacheBuilder anomaliesCache) {
    this.metricDAO = metricDAO;
    this.datasetDAO = datasetDAO;
    this.eventDAO = eventDAO;
    this.evaluationDAO = evaluationDAO;
    this.anomaliesCache = anomaliesCache;
  }

  private static Predicate AND(Collection<Predicate> predicates) {
    return Predicate.AND(predicates.toArray(new Predicate[predicates.size()]));
  }

  @Override
  public Map<MetricSlice, DataFrame> fetchTimeseries(Collection<MetricSlice> slices) {
    throw new UnsupportedOperationException("fetchTimeseries not supported anymore");
  }

  @Override
  public Map<MetricSlice, DataFrame> fetchAggregates(Collection<MetricSlice> slices,
      final List<String> dimensions, int limit) {
    throw new UnsupportedOperationException("fetchAggregates not supported anymore");
  }

  /**
   * Fetch all anomalies based on the request Anomaly Slices (overlap with slice window)
   */
  @Override
  public Multimap<AnomalySlice, MergedAnomalyResultDTO> fetchAnomalies(
      Collection<AnomalySlice> slices) {
    Multimap<AnomalySlice, MergedAnomalyResultDTO> output = ArrayListMultimap.create();
    try {
      for (AnomalySlice slice : slices) {
        Collection<MergedAnomalyResultDTO> cacheResult = anomaliesCache.fetchSlice(slice);

        // make a copy of the result so that cache won't be contaminated by client code
        List<MergedAnomalyResultDTO> clonedAnomalies = new ArrayList<>();
        for (MergedAnomalyResultDTO anomaly : cacheResult) {
          clonedAnomalies.add(SerializationUtils.clone(anomaly));
        }

        LOG.info("Fetched {} anomalies for slice {}", clonedAnomalies.size(), slice);
        output.putAll(slice, clonedAnomalies);
      }

      return output;
    } catch (Exception e) {
      throw new DataProviderException("Failed to fetch anomalies from database.", e);
    }
  }

  @Override
  public Multimap<EventSlice, EventDTO> fetchEvents(Collection<EventSlice> slices) {
    Multimap<EventSlice, EventDTO> output = ArrayListMultimap.create();
    for (EventSlice slice : slices) {
      List<Predicate> predicates = DetectionUtils
          .buildPredicatesOnTime(slice.getStart(), slice.getEnd());

      if (predicates.isEmpty()) {
        throw new IllegalArgumentException("Must provide at least one of start, or end");
      }
      List<EventDTO> events = this.eventDAO.findByPredicate(AND(predicates));
      events.removeIf(eventDTO -> !slice.match(eventDTO));
      output.putAll(slice, events);
    }
    return output;
  }

  @Override
  public Map<Long, MetricConfigDTO> fetchMetrics(Collection<Long> ids) {
    List<MetricConfigDTO> metrics = this.metricDAO
        .findByPredicate(Predicate.IN("baseId", ids.toArray()));

    Map<Long, MetricConfigDTO> output = new HashMap<>();
    for (MetricConfigDTO metric : metrics) {
      if (metric != null) {
        output.put(metric.getId(), metric);
      }
    }
    return output;
  }

  @Override
  public Map<String, DatasetConfigDTO> fetchDatasets(Collection<String> datasetNames) {
    List<DatasetConfigDTO> datasets = this.datasetDAO
        .findByPredicate(Predicate.IN("dataset", datasetNames.toArray()));

    Map<String, DatasetConfigDTO> output = new HashMap<>();
    for (DatasetConfigDTO dataset : datasets) {
      if (dataset != null) {
        output.put(dataset.getDataset(), dataset);
      }
    }
    return output;
  }

  @Override
  public MetricConfigDTO fetchMetric(String metricName, String datasetName) {
    return this.metricDAO.findByMetricAndDataset(metricName, datasetName);
  }

  @Override
  public Multimap<EvaluationSlice, EvaluationDTO> fetchEvaluations(
      Collection<EvaluationSlice> slices, long configId) {
    Multimap<EvaluationSlice, EvaluationDTO> output = ArrayListMultimap.create();
    for (EvaluationSlice slice : slices) {
      List<Predicate> predicates = DetectionUtils
          .buildPredicatesOnTime(slice.getStart(), slice.getEnd());
      if (predicates.isEmpty()) {
        throw new IllegalArgumentException("Must provide at least one of start, or end");
      }

      if (configId >= 0) {
        predicates.add(Predicate.EQ("detectionConfigId", configId));
      }
      List<EvaluationDTO> evaluations = this.evaluationDAO.findByPredicate(AND(predicates));
      output.putAll(slice, evaluations.stream().filter(slice::match).collect(Collectors.toList()));
    }
    return output;
  }

  @Override
  public List<DatasetConfigDTO> fetchDatasetByDisplayName(String datasetDisplayName) {
    List<DatasetConfigDTO> dataset = this.datasetDAO
        .findByPredicate(Predicate.EQ("displayName", datasetDisplayName));
    return dataset;
  }
}
