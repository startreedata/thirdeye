/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import static ai.startree.thirdeye.util.ThirdeyeMetricsUtil.detectionRetuneCounter;

import ai.startree.thirdeye.detection.annotation.registry.DetectionRegistry;
import ai.startree.thirdeye.detection.components.MapeAveragePercentageChangeModelEvaluator;
import ai.startree.thirdeye.detection.spec.MapeAveragePercentageChangeModelEvaluatorSpec;
import ai.startree.thirdeye.detection.spi.components.ModelEvaluator;
import ai.startree.thirdeye.detection.spi.model.ModelStatus;
import ai.startree.thirdeye.detection.yaml.DetectionConfigTuner;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.BaseComponent;
import ai.startree.thirdeye.spi.detection.DataProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default model maintenance flow. If the model is tunable, this flow will run the configured
 * model evaluators for
 * the detection config and automatically re-tunes the model.
 */
@Singleton
public class ModelRetuneFlow implements ModelMaintenanceFlow {

  private static final int DEFAULT_TUNING_WINDOW_DAYS = 28;
  private static final Logger LOG = LoggerFactory.getLogger(ModelRetuneFlow.class);

  private final DataProvider provider;
  private final DetectionRegistry detectionRegistry;

  @Inject
  public ModelRetuneFlow(DataProvider provider, DetectionRegistry detectionRegistry) {
    this.provider = provider;
    this.detectionRegistry = detectionRegistry;
  }

  public AlertDTO maintain(AlertDTO config, Instant timestamp) {
    final Map<String, BaseComponent> components = config.getComponents();
    if (components == null || components.isEmpty()) {
      return config;
    }

    if (isTunable(config)) {
      // todo cyril no model is tunable in v2 - remove ?
      // if the pipeline is tunable, get the model evaluators
      Collection<? extends ModelEvaluator<? extends AbstractSpec>> modelEvaluators = getModelEvaluators(
          config);
      // check the status for model evaluators
      for (ModelEvaluator<? extends AbstractSpec> modelEvaluator : modelEvaluators) {
        // if returns bad model status, trigger model tuning
        if (modelEvaluator.evaluateModel(timestamp).getStatus().equals(ModelStatus.BAD)) {
          LOG.info("Status for detection pipeline {} is {}, re-tuning", config.getId(),
              ModelStatus.BAD.toString());
          detectionRetuneCounter.inc();
          DetectionConfigTuner detectionConfigTuner = new DetectionConfigTuner(config, provider);
          config = detectionConfigTuner
              .tune(timestamp.toDateTime().minusDays(DEFAULT_TUNING_WINDOW_DAYS).getMillis(),
                  timestamp.getMillis());
          config.setLastTuningTimestamp(timestamp.getMillis());
          break;
        }
      }
    }
    return config;
  }

  private Collection<? extends ModelEvaluator<? extends AbstractSpec>> getModelEvaluators(
      AlertDTO config) {
    // get the model evaluator in the detection config
    Collection<? extends ModelEvaluator<? extends AbstractSpec>> modelEvaluators = config
        .getComponents()
        .values()
        .stream()
        .filter(component -> component instanceof ModelEvaluator)
        .map(component -> (ModelEvaluator<? extends AbstractSpec>) component)
        .collect(Collectors.toList());

    if (modelEvaluators.isEmpty()) {
      // if evaluators are not configured, use the default ones
      modelEvaluators = instantiateDefaultEvaluators(config);
    }

    return modelEvaluators;
  }

  private Collection<ModelEvaluator<MapeAveragePercentageChangeModelEvaluatorSpec>> instantiateDefaultEvaluators(
      AlertDTO config) {
    ModelEvaluator<MapeAveragePercentageChangeModelEvaluatorSpec> evaluator =
        new MapeAveragePercentageChangeModelEvaluator();
    evaluator.init(new MapeAveragePercentageChangeModelEvaluatorSpec(),
        new DefaultInputDataFetcher(this.provider, config.getId()));
    return Collections.singleton(evaluator);
  }

  /**
   * If the detection config contains a tunable component
   *
   * @param configDTO the detection config
   * @return True if the detection config is contains tunable component
   */
  private boolean isTunable(AlertDTO configDTO) {
    return configDTO.getComponents()
        .values()
        .stream()
        .anyMatch(component -> this.detectionRegistry.isTunable(component.getClass().getName()));
  }
}
