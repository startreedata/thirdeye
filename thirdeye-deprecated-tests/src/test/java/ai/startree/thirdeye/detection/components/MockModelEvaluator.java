/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.spec.MockModelEvaluatorSpec;
import ai.startree.thirdeye.detection.spi.components.ModelEvaluator;
import ai.startree.thirdeye.detection.spi.model.ModelEvaluationResult;
import ai.startree.thirdeye.detection.spi.model.ModelStatus;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import org.joda.time.Instant;

public class MockModelEvaluator implements ModelEvaluator<MockModelEvaluatorSpec> {

  private ModelStatus status;

  @Override
  public void init(MockModelEvaluatorSpec spec) {
    this.status = spec.getMockModelStatus();
  }

  @Override
  public void init(MockModelEvaluatorSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
  }

  @Override
  public ModelEvaluationResult evaluateModel(Instant evaluationTimeStamp) {
    return new ModelEvaluationResult(status);
  }
}
