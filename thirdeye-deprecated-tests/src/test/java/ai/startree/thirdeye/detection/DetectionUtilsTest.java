/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.detection.DetectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DetectionUtilsTest {

  @Test
  public void testGetComponentType() {
    Assert.assertEquals(DetectionUtils.getComponentType("myRule:ALGORITHM"), "ALGORITHM");

    try {
      Assert.assertEquals(DetectionUtils.getComponentType(null), "ALGORITHM");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(true);
    }

    try {
      Assert.assertEquals(DetectionUtils.getComponentType("ALGORITHM"), "ALGORITHM");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(true);
    }

    try {
      Assert.assertEquals(DetectionUtils.getComponentType(""), "ALGORITHM");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(true);
    }
  }
}
