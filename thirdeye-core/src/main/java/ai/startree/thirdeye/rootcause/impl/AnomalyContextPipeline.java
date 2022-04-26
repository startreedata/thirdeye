/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.rootcause.entity.AnomalyEventEntity;
import ai.startree.thirdeye.rootcause.entity.DimensionEntity;
import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.rootcause.entity.TimeRangeEntity;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AnomalyContextPipeline resolves an anomaly entity to a rootcause search context that can
 * serve as input to a typical RCA framework.
 * Populates time ranges, metric urn, and dimension filters.
 */
public class AnomalyContextPipeline extends Pipeline {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyContextPipeline.class);

  private static final String PROP_BASELINE_OFFSET = "baselineOffset";
  private static final long PROP_BASELINE_OFFSET_DEFAULT = TimeUnit.DAYS.toMillis(7);

  private static final String PROP_ANALYSIS_WINDOW = "analysisWindow";
  private static final long PROP_ANALYSIS_WINDOW_DEFAULT = TimeUnit.DAYS.toMillis(14);

  private final MergedAnomalyResultManager anomalyDAO;
  private final MetricConfigManager metricDAO;

  private final long baselineOffset;
  private final long analysisWindow;

  /**
   * Constructor for dependency injection
   *
   * @param outputName pipeline output name
   * @param inputNames input pipeline names
   * @param anomalyDAO anomaly config DAO
   * @param metricDAO metric config DAO
   * @param baselineOffset baseline range offset
   * @param analysisWindow analysis range window up to end of anomaly
   */
  public AnomalyContextPipeline(String outputName, Set<String> inputNames,
      MergedAnomalyResultManager anomalyDAO, MetricConfigManager metricDAO, long baselineOffset,
      long analysisWindow) {
    super();
    this.anomalyDAO = anomalyDAO;
    this.metricDAO = metricDAO;
    this.baselineOffset = baselineOffset;
    this.analysisWindow = analysisWindow;
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    Set<AnomalyEventEntity> anomalies = context.filter(AnomalyEventEntity.class);

    if (anomalies.size() > 1) {
      LOG.warn("Got multiple anomalies to resolve. This could lead to unexpected results.");
    }

    Set<Entity> output = new HashSet<>();
    for (AnomalyEventEntity e : anomalies) {
      MergedAnomalyResultDTO anomalyDTO = this.anomalyDAO.findById(e.getId());
      if (anomalyDTO == null) {
        LOG.warn("Could not resolve anomaly id {}. Skipping.", e.getId());
        continue;
      }

      long start = anomalyDTO.getStartTime();
      long end = anomalyDTO.getEndTime();

      // TODO replace with metric id when available
      String metric = anomalyDTO.getMetric();
      String dataset = anomalyDTO.getCollection();

      MetricConfigDTO metricDTO = this.metricDAO.findByMetricAndDataset(metric, dataset);
      if (metricDTO == null) {
        LOG.warn("Could not resolve metric '{}' from '{}'", metric, dataset);
        continue;
      }

      long metricId = metricDTO.getId();

      // time ranges
      output.add(TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANOMALY, start, end));
      output.add(TimeRangeEntity
          .fromRange(0.8, TimeRangeEntity.TYPE_BASELINE, start - this.baselineOffset,
              end - this.baselineOffset));
      output.add(TimeRangeEntity
          .fromRange(1.0, TimeRangeEntity.TYPE_ANALYSIS, end - this.analysisWindow, end));

      // filters
      Multimap<String, String> filters = TreeMultimap.create();
      for (Map.Entry<String, String> entry : anomalyDTO.getDimensions().entrySet()) {
        filters.put(entry.getKey(), entry.getValue());

        // TODO deprecate dimension entity?
        output.add(DimensionEntity
            .fromDimension(1.0, entry.getKey(), entry.getValue(), DimensionEntity.TYPE_PROVIDED));
      }

      // metric
      output.add(MetricEntity.fromMetric(1.0, metricId, filters));
    }

    return new PipelineResult(context, output);
  }
}
