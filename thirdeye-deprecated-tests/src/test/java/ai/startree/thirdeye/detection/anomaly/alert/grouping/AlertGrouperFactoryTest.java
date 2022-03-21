/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping;

import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlertGrouperFactoryTest {

  @Test
  public void testFromSpecNull() throws Exception {
    AlertGrouper dataFilter = AlertGrouperFactory.fromSpec(null);
    Assert.assertEquals(dataFilter.getClass(), DummyAlertGrouper.class);
  }

  @Test
  public void testAlertGrouperCreation() {
    Map<String, String> spec = new HashMap<>();
    spec.put(AlertGrouperFactory.GROUPER_TYPE_KEY, "diMenSionAL");
    AlertGrouper alertGrouper = AlertGrouperFactory.fromSpec(spec);
    Assert.assertEquals(alertGrouper.getClass(), DimensionalAlertGrouper.class);
  }
}
