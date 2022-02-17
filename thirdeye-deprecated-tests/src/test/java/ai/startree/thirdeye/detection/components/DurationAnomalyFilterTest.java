/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import static ai.startree.thirdeye.detection.DetectionTestUtils.makeAnomaly;

import ai.startree.thirdeye.detection.DefaultInputDataFetcher;
import ai.startree.thirdeye.detection.MockDataProvider;
import ai.startree.thirdeye.detection.components.filters.DurationAnomalyFilter;
import ai.startree.thirdeye.detection.components.filters.DurationAnomalyFilterSpec;
import ai.startree.thirdeye.spi.detection.AnomalyFilter;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DurationAnomalyFilterTest {

  @Test
  public void testIsQualified() {
    AnomalyFilter anomalyFilter = new DurationAnomalyFilter();
    DurationAnomalyFilterSpec spec = new DurationAnomalyFilterSpec();
    spec.setMaxDuration("PT3H");
    spec.setMinDuration("PT2H");
    anomalyFilter.init(spec, new DefaultInputDataFetcher(new MockDataProvider(), -1));
    Assert.assertEquals(anomalyFilter.isQualified(makeAnomaly(1547164800000L, 1547168400000L)),
        false);
    Assert
        .assertEquals(anomalyFilter.isQualified(makeAnomaly(1547164800000L, 1547172000000L)), true);
    Assert
        .assertEquals(anomalyFilter.isQualified(makeAnomaly(1547164800000L, 1547175600000L)), true);
    Assert.assertEquals(anomalyFilter.isQualified(makeAnomaly(1547164800000L, 1547179200000L)),
        false);
  }

  @Test
  public void testDefaultQualified() {
    AnomalyFilter anomalyFilter = new DurationAnomalyFilter();
    DurationAnomalyFilterSpec spec = new DurationAnomalyFilterSpec();
    anomalyFilter.init(spec, new DefaultInputDataFetcher(new MockDataProvider(), -1));
    Assert
        .assertEquals(anomalyFilter.isQualified(makeAnomaly(1547164800000L, 1547168400000L)), true);
    Assert
        .assertEquals(anomalyFilter.isQualified(makeAnomaly(1547164800000L, 1547172000000L)), true);
    Assert
        .assertEquals(anomalyFilter.isQualified(makeAnomaly(1547164800000L, 1547175600000L)), true);
    Assert
        .assertEquals(anomalyFilter.isQualified(makeAnomaly(1547164800000L, 1547179200000L)), true);
  }
}
