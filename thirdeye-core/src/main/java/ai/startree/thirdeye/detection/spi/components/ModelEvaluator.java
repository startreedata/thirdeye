/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.spi.components;

import ai.startree.thirdeye.detection.spi.model.ModelEvaluationResult;
import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.BaseComponent;
import org.joda.time.Instant;

/**
 * The interface for model evaluator.
 *
 * @param <T> the spec class for this model evaluator
 */
public interface ModelEvaluator<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Evaluate the current detection model.
   *
   * @param evaluationTimeStamp the time stamp when the evaluation is run.
   * @return a model evaluation result
   */
  ModelEvaluationResult evaluateModel(Instant evaluationTimeStamp);
}
