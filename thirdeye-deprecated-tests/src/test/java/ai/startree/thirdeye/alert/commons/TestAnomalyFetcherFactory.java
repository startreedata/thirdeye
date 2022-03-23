/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.alert.commons;

import ai.startree.thirdeye.detection.alert.AnomalyFetcherFactory;
import ai.startree.thirdeye.detection.alert.fetcher.AnomalyFetcher;
import ai.startree.thirdeye.detection.alert.fetcher.ContinuumAnomalyFetcher;
import ai.startree.thirdeye.detection.alert.fetcher.UnnotifiedAnomalyFetcher;
import org.junit.Assert;
import org.testng.annotations.Test;

public class TestAnomalyFetcherFactory {

  @Test(enabled = false)
  public void testCreateAlertFetcher() throws Exception {
    AnomalyFetcher anomalyFetcher = AnomalyFetcherFactory.fromClassName("ContinuumAnomalyFetcher");
    Assert.assertNotNull(anomalyFetcher);
    Assert.assertTrue(anomalyFetcher instanceof ContinuumAnomalyFetcher);

    anomalyFetcher = AnomalyFetcherFactory.fromClassName("UnnotifiedAnomalyFetcher");
    Assert.assertNotNull(anomalyFetcher);
    Assert.assertTrue(anomalyFetcher instanceof UnnotifiedAnomalyFetcher);
  }
}
