/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.spec;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

/**
 * The spec class for MAPE change evaluator
 */
public class MapeAveragePercentageChangeModelEvaluatorSpec extends AbstractSpec {

  private double threshold = 0.1; // default threshold to 10%

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }
}
