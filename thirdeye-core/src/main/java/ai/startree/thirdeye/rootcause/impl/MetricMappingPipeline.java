/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.MaxScoreSet;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.rootcause.entity.DatasetEntity;
import ai.startree.thirdeye.rootcause.entity.DimensionEntity;
import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MetricMappingPipeline maps metrics to related metrics via dataset and entity mappings.
 * Also translates dimension filters between related metric entities if possible.<br />
 *
 * <br/><b>NOTE:</b> traverses a maximum of one hop for related metrics and related datasets.
 * Performs 2nd degree search for metric > dataset > related dataset > related metric
 */
@Deprecated
public class MetricMappingPipeline extends Pipeline {

  private static final Logger LOG = LoggerFactory.getLogger(MetricMappingPipeline.class);

  private static final String MAPPING_DIMENSIONS = "DIMENSION_TO_DIMENSION";

  private static final String PROP_INCLUDE_FILTERS = "includeFilters";
  private static final boolean PROP_INCLUDE_FILTERS_DEFAULT = true;

  private static final String PROP_EXCLUDE_METRICS = "excludeMetrics";
  private static final Set<String> PROP_EXCLUDE_METRICS_DEFAULT = Collections.singleton("__COUNT");

  private MetricConfigManager metricDAO;
  private DatasetConfigManager datasetDAO;
  private EntityToEntityMappingManager mappingDAO;
  private boolean includeFilters;
  private Set<String> excludeMetrics;

  /**
   * Used by {@link RCAFrameworkLoader}
   */
  @SuppressWarnings("unused")
  public MetricMappingPipeline() {
  }

  /**
   * Constructor for dependency injection
   */
  public MetricMappingPipeline(boolean includeFilters,
      Set<String> excludeMetrics,
      MetricConfigManager metricDAO,
      DatasetConfigManager datasetDAO,
      EntityToEntityMappingManager mappingDAO) {
    super();
    this.metricDAO = metricDAO;
    this.datasetDAO = datasetDAO;
    this.mappingDAO = mappingDAO;
    this.includeFilters = includeFilters;
    this.excludeMetrics = excludeMetrics;
  }

  @Override
  public void init(final PipelineInitContext context) {
    super.init(context);
    Map<String, Object> properties = context.getProperties();
    this.metricDAO = context.getMetricConfigManager();
    this.datasetDAO = context.getDatasetConfigManager();
    this.mappingDAO = context.getEntityToEntityMappingManager();
    this.includeFilters = MapUtils
        .getBooleanValue(properties, PROP_INCLUDE_FILTERS, PROP_INCLUDE_FILTERS_DEFAULT);

    if (properties.containsKey(PROP_EXCLUDE_METRICS)) {
      this.excludeMetrics = new HashSet<>((Collection<String>) properties.get(PROP_EXCLUDE_METRICS));
    } else {
      this.excludeMetrics = PROP_EXCLUDE_METRICS_DEFAULT;
    }
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    Set<MetricEntity> input = context.filter(MetricEntity.class);
    Set<MetricEntity> output = new MaxScoreSet<>();

    for (MetricEntity me : input) {
      MetricConfigDTO metric = this.metricDAO.findById(me.getId());
      if (metric == null) {
        LOG.warn("Could not resolve metric id {}. Skipping.", me.getId());
        continue;
      }

      DatasetConfigDTO dataset = this.datasetDAO.findByDataset(metric.getDataset());
      if (dataset == null) {
        LOG.warn("Could not resolve metric id {} dataset '{}'. Skipping.", me.getId(),
            metric.getDataset());
        continue;
      }

      Multimap<String, String> filters = ArrayListMultimap.create();
      if (this.includeFilters) {
        filters = fetchTransitiveHull(me.getFilters());
      }

      DatasetEntity de = DatasetEntity.fromName(me.getScore(), metric.getDataset());

      Set<MetricEntity> metrics = new MaxScoreSet<>();
      Set<DatasetEntity> datasets = new MaxScoreSet<>();

      // metric
      // NOTE: native metric added via native dataset

      // dataset
      datasets.add(de);

      // from metric
      List<EntityToEntityMappingDTO> fromMetric = this.mappingDAO
          .findByFromURN(me.withoutFilters().getUrn());
      for (EntityToEntityMappingDTO mapping : fromMetric) {
        String urn = mapping.getToURN();

        // metric-related metrics
        if (MetricEntity.TYPE.isType(urn)) {
          MetricEntity m = MetricEntity.fromURN(urn, me.getScore() * mapping.getScore());
          metrics.add(m.withFilters(pruneFilters(filters, m.getId())));
        }

        // metric-related datasets
        if (DatasetEntity.TYPE.isType(urn)) {
          datasets.add(DatasetEntity.fromURN(urn, me.getScore() * mapping.getScore()));
        }
      }

      // from dataset
      List<EntityToEntityMappingDTO> fromDataset = this.mappingDAO.findByFromURN(de.getUrn());
      for (EntityToEntityMappingDTO mapping : fromDataset) {
        String urn = mapping.getToURN();

        // NOTE: dataset-native metrics explored with datasets below

        // dataset-related datasets
        if (DatasetEntity.TYPE.isType(urn)) {
          DatasetEntity relatedDataset = DatasetEntity
              .fromURN(urn, de.getScore() * mapping.getScore());
          datasets.add(relatedDataset);
        }
      }

      // from related datasets (and dataset)
      for (DatasetEntity relatedDataset : datasets) {
        List<MetricConfigDTO> nativeMetrics = pruneMetrics(
            this.metricDAO.findByDataset(relatedDataset.getName()));

        // related-dataset-native metrics
        for (MetricConfigDTO nativeMetric : nativeMetrics) {
          MetricEntity m = MetricEntity.fromMetric(relatedDataset.getScore(), nativeMetric.getId());
          metrics.add(m.withFilters(pruneFilters(filters, m.getId())));
        }

        // related-dataset-related metrics
        // NOTE: potentially expensive 2nd degree search
        List<EntityToEntityMappingDTO> relatedMetrics = this.mappingDAO
            .findByFromURN(relatedDataset.getUrn());
        for (EntityToEntityMappingDTO relatedMetric : relatedMetrics) {
          if (MetricEntity.TYPE.isType(relatedMetric.getToURN())) {
            MetricEntity m = MetricEntity.fromURN(relatedMetric.getToURN(),
                relatedDataset.getScore() * relatedMetric.getScore());
            metrics.add(m.withFilters(pruneFilters(filters, m.getId())));
          }
        }
      }

      output.addAll(metrics);
    }

    return new PipelineResult(context, output);
  }

  /**
   * Prunes filter set to only allow dimensions that are available in a metrics own dataset
   *
   * @param metricId metric id
   * @return pruned filter multimap
   */
  private Multimap<String, String> pruneFilters(Multimap<String, String> filters, long metricId) {
    MetricConfigDTO metric = this.metricDAO.findById(metricId);
    if (metric == null) {
      LOG.warn("Could not resolve metric id {} while pruning filters", metricId);
      return ArrayListMultimap.create();
    }

    DatasetConfigDTO dataset = this.datasetDAO.findByDataset(metric.getDataset());
    if (dataset == null) {
      LOG.warn("Could not resolve dataset '{}' for metric id {} while pruning filters",
          metric.getDataset(), metricId);
      return ArrayListMultimap.create();
    }

    Multimap<String, String> output = TreeMultimap.create(); // sorted, unique keys
    Set<String> validKeys = new HashSet<>(dataset.getDimensions());
    for (Map.Entry<String, String> entry : filters.entries()) {
      if (validKeys.contains(entry.getKey())) {
        output.put(entry.getKey(), entry.getValue());
      }
    }
    return output;
  }

  /**
   * Prunes metrics extracted from dataset based on active-state and excluded metrics list
   *
   * @param metrics dataste metrics
   * @return pruned list of metrics
   */
  private List<MetricConfigDTO> pruneMetrics(List<MetricConfigDTO> metrics) {
    List<MetricConfigDTO> output = new ArrayList<>();
    for (MetricConfigDTO metric : metrics) {
      if (!Boolean.TRUE.equals(metric.getActive())) {
        continue;
      }

      if (this.excludeMetrics.contains(metric.getName())) {
        continue;
      }

      output.add(metric);
    }

    return output;
  }

  /**
   * Fetches the transitive hull of dimension names from the database and augments the filter map.
   * Transparently translates between filter names/values and DimensionEntity.
   *
   * @param filters filters
   * @return transitive hull of filter dimensions
   */
  private Multimap<String, String> fetchTransitiveHull(Multimap<String, String> filters) {

    List<EntityToEntityMappingDTO> mappings = this.mappingDAO.findByMappingType(MAPPING_DIMENSIONS);

    Multimap<String, String> output = HashMultimap.create(); // unique keys
    output.putAll(filters);

    for (EntityToEntityMappingDTO mapping : mappings) {
      for (Map.Entry<String, String> entry : filters.entries()) {
        DimensionEntity dimension = DimensionEntity
            .fromDimension(1.0, entry.getKey(), entry.getValue(), DimensionEntity.TYPE_GENERATED);

        // apply mappings both ways
        if (dimension.getUrn().startsWith(mapping.getFromURN())) {
          String newUrn =
              mapping.getToURN() + dimension.getUrn().substring(mapping.getFromURN().length());
          DimensionEntity newDimension = DimensionEntity.fromURN(newUrn, 1.0);

          output.put(newDimension.getName(), newDimension.getValue());
        }

        if (dimension.getUrn().startsWith(mapping.getToURN())) {
          String newUrn =
              mapping.getFromURN() + dimension.getUrn().substring(mapping.getToURN().length());
          DimensionEntity newDimension = DimensionEntity.fromURN(newUrn, 1.0);

          output.put(newDimension.getName(), newDimension.getValue());
        }
      }
    }

    if (output.size() == filters.size()) {
      return output;
    }

    return fetchTransitiveHull(output);
  }
}
