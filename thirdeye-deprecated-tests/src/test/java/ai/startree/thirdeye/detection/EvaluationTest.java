/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.detection.Evaluation;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EvaluationTest {

  @Test
  public void testCalculateMape() {
    double mape = Evaluation
        .calculateMape(DoubleSeries.buildFrom(10, 20), DoubleSeries.buildFrom(20, 30));
    Assert.assertEquals(mape, 0.75);
  }

  @Test
  public void testCalculateZero() {
    double mape = Evaluation
        .calculateMape(DoubleSeries.buildFrom(0, 20), DoubleSeries.buildFrom(1, 30));
    Assert.assertEquals(mape, Double.POSITIVE_INFINITY);
  }
}
