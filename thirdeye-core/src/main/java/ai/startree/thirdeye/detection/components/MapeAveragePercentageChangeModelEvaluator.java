/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.spec.MapeAveragePercentageChangeModelEvaluatorSpec;
import ai.startree.thirdeye.detection.spi.components.ModelEvaluator;
import ai.startree.thirdeye.detection.spi.model.ModelEvaluationResult;
import ai.startree.thirdeye.detection.spi.model.ModelStatus;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.model.EvaluationSlice;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.joda.time.Instant;

/**
 * Monitor the recent mean MAPE in last 7 days, and compare that with the mean MAPE for the last 30
 * days.
 * If the percentage change dropped to a certain threshold for a metric urn, return a bad model
 * status to trigger
 * auto configuration.
 */
public class MapeAveragePercentageChangeModelEvaluator implements
    ModelEvaluator<MapeAveragePercentageChangeModelEvaluatorSpec> {

  private static final int MAPE_LOOK_BACK_DAYS_RECENT = 7;
  private static final int MAPE_LOOK_BACK_DAYS_BASELINE = 30;

  private InputDataFetcher dataFetcher;
  private double threshold;

  @Override
  public void init(MapeAveragePercentageChangeModelEvaluatorSpec spec) {
    this.threshold = spec.getThreshold();
  }

  @Override
  public void init(MapeAveragePercentageChangeModelEvaluatorSpec spec,
      InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public ModelEvaluationResult evaluateModel(Instant evaluationTimeStamp) {
    EvaluationSlice evaluationSlice =
        new EvaluationSlice().withStartTime(
            evaluationTimeStamp.toDateTime().minusDays(MAPE_LOOK_BACK_DAYS_BASELINE).getMillis())
            .withEndTime(evaluationTimeStamp.getMillis());
    // fetch evaluations
    Collection<EvaluationDTO> evaluations =
        this.dataFetcher.fetchData(
            new InputDataSpec().withEvaluationSlices(Collections.singleton(evaluationSlice)))
            .getEvaluations()
            .get(evaluationSlice);

    Collection<EvaluationDTO> recentEvaluations = getEvaluationsWithinDays(evaluations,
        evaluationTimeStamp,
        MAPE_LOOK_BACK_DAYS_RECENT);
    Collection<EvaluationDTO> baselineEvaluations = getEvaluationsWithinDays(evaluations,
        evaluationTimeStamp,
        MAPE_LOOK_BACK_DAYS_BASELINE);

    if (recentEvaluations.isEmpty() || recentEvaluations.containsAll(baselineEvaluations)) {
      // data is insufficient for performing evaluations
      return new ModelEvaluationResult(ModelStatus.UNKNOWN);
    }

    // calculate past 7 day mean MAPE for each metric urn and rules
    Map<String, Double> recentMeanMapeForMetricUrnsAndRules = getMeanMapeForEachMetricUrnAndRule(
        recentEvaluations);

    // calculate past 30 day mean MAPE for each metric urn and rules
    Map<String, Double> baselineMeanMapeForMetricUrnsAndRules = getMeanMapeForEachMetricUrnAndRule(
        baselineEvaluations);

    // evaluate for each metric urn
    Map<String, Boolean> evaluationResultForMetricUrnsAndRules = recentMeanMapeForMetricUrnsAndRules
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey,
            // compare the MAPE percentage change to threshold
            recentMeanMape -> recentMeanMape.getValue() / baselineMeanMapeForMetricUrnsAndRules
                .get(recentMeanMape.getKey()) - 1 <= threshold));

    if (evaluationResultForMetricUrnsAndRules.values().stream().allMatch(result -> result)) {
      // if all metric urn's status is good, return overall good status
      return new ModelEvaluationResult(ModelStatus.GOOD);
    }
    return new ModelEvaluationResult(ModelStatus.BAD);
  }

  /**
   * Filter the evaluations to return only the past number days.
   *
   * @param evaluations evaluations
   * @param evaluationTimeStamp the time stamp for evaluations
   * @param days look back number of days
   * @return the filtered collection of evaluationDTOs
   */
  private Collection<EvaluationDTO> getEvaluationsWithinDays(Collection<EvaluationDTO> evaluations,
      Instant evaluationTimeStamp, int days) {
    return evaluations.stream()
        .filter(eval -> Objects.nonNull(eval.getMape()))
        .filter(eval -> evaluationTimeStamp.toDateTime().minusDays(days).getMillis() < eval
            .getStartTime())
        .collect(Collectors.toSet());
  }

  /**
   * calculate the mean MAPE for each metric urn based on the available evaluations over the past
   * number of days
   *
   * @param evaluations the available evaluations
   * @return the mean MAPE keyed by metric urns
   */
  private Map<String, Double> getMeanMapeForEachMetricUrnAndRule(
      Collection<EvaluationDTO> evaluations) {
    return
        evaluations.stream().collect(
            Collectors
                .groupingBy(e -> String.format("%s:%s", e.getMetricUrn(), e.getDetectorName()),
                    Collectors.averagingDouble(EvaluationDTO::getMape)));
  }
}
