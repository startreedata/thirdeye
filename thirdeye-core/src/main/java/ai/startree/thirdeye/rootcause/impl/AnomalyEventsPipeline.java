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
import ai.startree.thirdeye.rootcause.entity.AnomalyEventEntity;
import ai.startree.thirdeye.rootcause.entity.DimensionEntity;
import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.rootcause.entity.TimeRangeEntity;
import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.HyperbolaStrategy;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.LinearStartTimeStrategy;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.QuadraticTriangularStartTimeStrategy;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.TimeRangeStrategy;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.TriangularStartTimeStrategy;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline for identifying anomaly events based on their associated metric
 * names. The pipeline identifies metric entities in the search context and then invokes the
 * event provider manager to fetch any matching events. It then scores events based on their
 * time distance from the end of the search time window (closer is better).
 */
public class AnomalyEventsPipeline extends Pipeline {

  private static final Logger LOG = LoggerFactory.getLogger(AnomalyEventsPipeline.class);

  enum StrategyType {
    LINEAR,
    TRIANGULAR,
    QUADRATIC,
    HYPERBOLA,
    DIMENSION,
    COMPOUND
  }

  private static final String PROP_K = "k";
  private static final int PROP_K_DEFAULT = -1;

  private static final String PROP_STRATEGY = "strategy";
  private static final String PROP_STRATEGY_DEFAULT = StrategyType.COMPOUND.toString();

  private StrategyType strategy;
  private MergedAnomalyResultManager anomalyDAO;
  private int k;

  @Override
  public void init(final PipelineInitContext context) {
    super.init(context);
    Map<String, Object> properties = context.getProperties();
    this.anomalyDAO = context.getMergedAnomalyResultManager();
    this.strategy = StrategyType.valueOf(
        MapUtils.getString(properties, PROP_STRATEGY, PROP_STRATEGY_DEFAULT));
    this.k = MapUtils.getInteger(properties, PROP_K, PROP_K_DEFAULT);
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    Set<MetricEntity> metrics = context.filter(MetricEntity.class);

    TimeRangeEntity anomaly = TimeRangeEntity.getTimeRangeAnomaly(context);
    TimeRangeEntity baseline = TimeRangeEntity.getTimeRangeBaseline(context);
    TimeRangeEntity analysis = TimeRangeEntity.getTimeRangeAnalysis(context);

    // use both provided and generated
    Set<DimensionEntity> dimensionEntities = context.filter(DimensionEntity.class);
    Map<String, DimensionEntity> urn2entity = EntityUtils.mapEntityURNs(dimensionEntities);

    ScoringStrategy strategyAnomaly = makeStrategy(analysis.getStart(), anomaly.getStart(),
        anomaly.getEnd());
    ScoringStrategy strategyBaseline = makeStrategy(baseline.getStart(), baseline.getStart(),
        baseline.getEnd());

    Set<AnomalyEventEntity> entities = new MaxScoreSet<>();
    for (MetricEntity me : metrics) {
      entities.addAll(EntityUtils.addRelated(score(strategyAnomaly,
          filter(this.anomalyDAO
              .findAnomaliesByMetricIdAndTimeRange(me.getId(), analysis.getStart(),
                  anomaly.getEnd())),
          urn2entity, anomaly.getScore() * me.getScore()), Arrays.asList(anomaly, me)));
      entities.addAll(EntityUtils.addRelated(score(strategyBaseline,
          filter(this.anomalyDAO
              .findAnomaliesByMetricIdAndTimeRange(me.getId(), baseline.getStart(),
                  baseline.getEnd())),
          urn2entity, baseline.getScore() * me.getScore()), Arrays.asList(baseline, me)));
    }

    return new PipelineResult(context, EntityUtils.topk(entities, this.k));
  }

  private Collection<AnomalyEventEntity> score(final ScoringStrategy strategy,
      final Collection<MergedAnomalyResultDTO> anomalies,
      final Map<String, DimensionEntity> urn2entity, final double coefficient) {
    return Collections2
        .transform(anomalies, new Function<MergedAnomalyResultDTO, AnomalyEventEntity>() {
          @Override
          public AnomalyEventEntity apply(MergedAnomalyResultDTO dto) {
            double score = strategy.score(dto, urn2entity) * coefficient;
            return AnomalyEventEntity.fromDTO(score, dto);
          }
        });
  }

  private Collection<MergedAnomalyResultDTO> filter(Collection<MergedAnomalyResultDTO> anomalies) {
    return Collections2.filter(anomalies, new Predicate<MergedAnomalyResultDTO>() {
      @Override
      public boolean apply(MergedAnomalyResultDTO mergedAnomalyResultDTO) {
        return !mergedAnomalyResultDTO.isChild();
      }
    });
  }

  private ScoringStrategy makeStrategy(long lookback, long start, long end) {
    switch (this.strategy) {
      case LINEAR:
        return new ScoreWrapper(new LinearStartTimeStrategy(start, end));
      case TRIANGULAR:
        return new ScoreWrapper(new TriangularStartTimeStrategy(lookback, start, end));
      case QUADRATIC:
        return new ScoreWrapper(
            new QuadraticTriangularStartTimeStrategy(lookback, start, end));
      case HYPERBOLA:
        return new ScoreWrapper(new HyperbolaStrategy(start, end));
      case DIMENSION:
        return new DimensionStrategy();
      case COMPOUND:
        return new CompoundStrategy(new HyperbolaStrategy(start, end));
      default:
        throw new IllegalArgumentException(
            String.format("Invalid strategy type '%s'", this.strategy));
    }
  }

  private interface ScoringStrategy {

    double score(MergedAnomalyResultDTO dto, Map<String, DimensionEntity> urn2entity);
  }

  /**
   * Wrapper for ScoreUtils time-based strategies
   */
  private static class ScoreWrapper implements ScoringStrategy {

    private final TimeRangeStrategy delegate;

    ScoreWrapper(TimeRangeStrategy delegate) {
      this.delegate = delegate;
    }

    @Override
    public double score(MergedAnomalyResultDTO dto, Map<String, DimensionEntity> urn2entity) {
      return this.delegate.score(dto.getStartTime(), dto.getEndTime());
    }
  }

  /**
   * Uses the highest score of dimension entities as they relate to an event
   */
  private static class DimensionStrategy implements ScoringStrategy {

    @Override
    public double score(MergedAnomalyResultDTO dto, Map<String, DimensionEntity> urn2entity) {
      return makeDimensionScore(urn2entity, dto.getDimensions());
    }

    private static double makeDimensionScore(Map<String, DimensionEntity> urn2entity,
        Map<String, String> dimensions) {
      double max = 0.0;
      for (DimensionEntity e : filter2entities(dimensions)) {
        if (urn2entity.containsKey(e.getUrn())) {
          max = Math.max(urn2entity.get(e.getUrn()).getScore(), max);
        }
      }
      return max;
    }

    private static Set<DimensionEntity> filter2entities(Map<String, String> dimensions) {
      Set<DimensionEntity> entities = new HashSet<>();
      for (Map.Entry<String, String> e : dimensions.entrySet()) {
        String name = e.getKey();
        String val = e.getValue();
        entities.add(DimensionEntity
            .fromDimension(1.0, name, val.toLowerCase(), DimensionEntity.TYPE_GENERATED));
      }
      return entities;
    }
  }

  /**
   * Compound strategy that considers both event time as well as the presence of related dimension
   * entities
   */
  private static class CompoundStrategy implements ScoringStrategy {

    private final TimeRangeStrategy delegateTime;
    private final ScoringStrategy delegateDimension = new DimensionStrategy();

    CompoundStrategy(TimeRangeStrategy delegateTime) {
      this.delegateTime = delegateTime;
    }

    @Override
    public double score(MergedAnomalyResultDTO dto, Map<String, DimensionEntity> urn2entity) {
      double scoreTime = this.delegateTime.score(dto.getStartTime(), dto.getEndTime());
      double scoreDimension = this.delegateDimension.score(dto, urn2entity);
      double scoreHasDimension = scoreDimension > 0 ? 1 : 0;

      // ignore truncated results
      if (scoreTime <= 0) {
        return 0;
      }

      return scoreTime + scoreHasDimension + Math.min(scoreDimension, 1);
    }
  }
}
