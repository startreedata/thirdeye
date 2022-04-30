/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.util;

import ai.startree.thirdeye.rootcause.util.ScoreUtils.HyperbolaStrategy;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.LinearStartTimeStrategy;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.QuadraticTriangularStartTimeStrategy;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.TimeRangeStrategy;
import ai.startree.thirdeye.rootcause.util.ScoreUtils.TriangularStartTimeStrategy;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ScoreUtilsTest {

  private final static double EPSILON = 0.0001;

  @Test
  public void testLinearTimeRangeStrategy() {
    TimeRangeStrategy scorer = new LinearStartTimeStrategy(10, 20);
    Assert.assertEquals(scorer.score(9, -1), 0.0d, EPSILON);
    Assert.assertEquals(scorer.score(10, -1), 1.0d, EPSILON);
    Assert.assertEquals(scorer.score(19, -1), 0.1d, EPSILON);
    Assert.assertEquals(scorer.score(20, -1), 0.0d, EPSILON);
    Assert.assertEquals(scorer.score(99, -1), 0.0d, EPSILON);
  }

  @Test
  public void testTriangularTimeRangeStrategy() {
    TimeRangeStrategy scorer = new TriangularStartTimeStrategy(10, 20, 40);
    Assert.assertEquals(scorer.score(9, -1), 0.0d, EPSILON);
    Assert.assertEquals(scorer.score(10, -1), 0.0d, EPSILON);
    Assert.assertEquals(scorer.score(19, -1), 0.9d, EPSILON);
    Assert.assertEquals(scorer.score(20, -1), 1.0d, EPSILON);
    Assert.assertEquals(scorer.score(30, -1), 0.5d, EPSILON);
    Assert.assertEquals(scorer.score(39, -1), 0.05d, EPSILON);
    Assert.assertEquals(scorer.score(40, -1), 0.0d, EPSILON);
    Assert.assertEquals(scorer.score(99, -1), 0.0d, EPSILON);
  }

  @Test
  public void testQuadraticTriangularTimeRangeStrategy() {
    TimeRangeStrategy scorer = new QuadraticTriangularStartTimeStrategy(10,
        20, 40);
    Assert.assertEquals(scorer.score(9, -1), 0.0d, EPSILON);
    Assert.assertEquals(scorer.score(10, -1), 0.0d, EPSILON);
    Assert.assertEquals(scorer.score(19, -1), 0.81d, EPSILON);
    Assert.assertEquals(scorer.score(20, -1), 1.0d, EPSILON);
    Assert.assertEquals(scorer.score(30, -1), 0.25d, EPSILON);
    Assert.assertEquals(scorer.score(39, -1), 0.0025d, EPSILON);
    Assert.assertEquals(scorer.score(40, -1), 0.0d, EPSILON);
    Assert.assertEquals(scorer.score(99, -1), 0.0d, EPSILON);
  }

  @Test
  public void testHyperbolaStrategy() {
    TimeRangeStrategy scorer = new HyperbolaStrategy(3600000, 7200000);
    Assert.assertEquals(scorer.score(0, -1), 0.5d, EPSILON);
    Assert.assertEquals(scorer.score(1800000, -1), 0.6666d, EPSILON);
    Assert.assertEquals(scorer.score(3600000, -1), 1.0d, EPSILON);
    Assert.assertEquals(scorer.score(5300000, -1), 0.6792d, EPSILON);
    Assert.assertEquals(scorer.score(5400000, -1), 0.0d, EPSILON);
  }
}
