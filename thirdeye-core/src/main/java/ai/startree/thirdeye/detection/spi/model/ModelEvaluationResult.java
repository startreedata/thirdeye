/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.spi.model;

/**
 * The model valuation result
 */
public class ModelEvaluationResult {

  // the overall model status
  private final ModelStatus status;

  public ModelStatus getStatus() {
    return status;
  }

  public ModelEvaluationResult(ModelStatus status) {
    this.status = status;
  }
}
