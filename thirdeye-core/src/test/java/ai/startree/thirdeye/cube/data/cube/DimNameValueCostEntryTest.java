/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.cube;

import ai.startree.thirdeye.cube.data.DimNameValueCostEntry;
import org.testng.annotations.Test;

public class DimNameValueCostEntryTest {

  @Test
  public void testCreation() {
    // test that constructor is working
    new DimNameValueCostEntry("", "", 0, 0, 0d, 0d, 0, 0, 0, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDimensionNameCreation() {
    new DimNameValueCostEntry(null, "", 0, 0, 0d, 0d, 0, 0, 0, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullDimensionValueCreation() {
    new DimNameValueCostEntry("", null, 0, 0, 0d, 0d, 0, 0, 0, 0);
  }
}
