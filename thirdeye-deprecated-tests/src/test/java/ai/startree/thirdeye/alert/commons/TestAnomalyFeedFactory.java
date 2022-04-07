/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.alert.commons;

import ai.startree.thirdeye.detection.alert.AnomalyFeedFactory;
import ai.startree.thirdeye.detection.alert.feed.AnomalyFeed;
import ai.startree.thirdeye.detection.alert.feed.UnionAnomalyFeed;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAnomalyFeedFactory {

  @Test(enabled = false)
  public void testCreateAlertFeed() throws Exception {
    AnomalyFeed anomalyFeed = AnomalyFeedFactory.fromClassName("UnionAnomalyFeed");
    Assert.assertNotNull(anomalyFeed);
    Assert.assertTrue(anomalyFeed instanceof UnionAnomalyFeed);
  }
}
