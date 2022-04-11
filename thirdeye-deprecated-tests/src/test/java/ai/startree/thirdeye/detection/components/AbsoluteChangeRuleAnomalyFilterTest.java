/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.DefaultInputDataFetcher;
import ai.startree.thirdeye.detection.DetectionTestUtils;
import ai.startree.thirdeye.detection.MockDataProvider;
import ai.startree.thirdeye.detection.components.filters.AbsoluteChangeRuleAnomalyFilter;
import ai.startree.thirdeye.detection.components.filters.AbsoluteChangeRuleAnomalyFilterSpec;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.detection.AnomalyFilter;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.rootcause.timeseries.Baseline;
import ai.startree.thirdeye.spi.rootcause.timeseries.BaselineAggregate;
import ai.startree.thirdeye.spi.rootcause.timeseries.BaselineAggregateType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbsoluteChangeRuleAnomalyFilterTest {

  private static final String METRIC_URN = "thirdeye:metric:123";
  private static final long CONFIG_ID = 125L;

  private DataProvider testDataProvider;
  private Baseline baseline;

  @BeforeMethod
  public void beforeMethod() {
    this.baseline = BaselineAggregate
        .fromWeekOverWeek(BaselineAggregateType.MEDIAN, 1, 1, DateTimeZone.forID("UTC"));

    MetricSlice slice1 = MetricSlice.from(123L, 0, 2);
    MetricSlice baselineSlice1 = this.baseline.scatter(slice1).get(0);
    MetricSlice slice2 = MetricSlice.from(123L, 4, 6);
    MetricSlice baselineSlice2 = this.baseline.scatter(slice2).get(0);

    Map<MetricSlice, DataFrame> aggregates = new HashMap<>();
    aggregates.put(slice1, new DataFrame().addSeries(DataFrame.COL_VALUE, 150).addSeries(
        DataFrame.COL_TIME, slice1.getStartMillis()).setIndex(DataFrame.COL_TIME));
    aggregates.put(baselineSlice1, new DataFrame().addSeries(DataFrame.COL_VALUE, 200).addSeries(
        DataFrame.COL_TIME, baselineSlice1.getStartMillis()).setIndex(DataFrame.COL_TIME));
    aggregates.put(slice2, new DataFrame().addSeries(DataFrame.COL_VALUE, 500).addSeries(
        DataFrame.COL_TIME, slice2.getStartMillis()).setIndex(DataFrame.COL_TIME));
    aggregates.put(baselineSlice2, new DataFrame().addSeries(DataFrame.COL_VALUE, 1000).addSeries(
        DataFrame.COL_TIME, baselineSlice2.getStartMillis()).setIndex(DataFrame.COL_TIME));

    this.testDataProvider = new MockDataProvider().setAggregates(aggregates);
  }

  @Test
  public void testAbsoluteChangeFilter() {
    AbsoluteChangeRuleAnomalyFilterSpec spec = new AbsoluteChangeRuleAnomalyFilterSpec();
    spec.setOffset("median1w");
    spec.setThreshold(100);
    spec.setPattern("up_or_down");
    AnomalyFilter filter = new AbsoluteChangeRuleAnomalyFilter();
    filter.init(spec, new DefaultInputDataFetcher(this.testDataProvider, CONFIG_ID));
    List<Boolean> results =
        Arrays.asList(DetectionTestUtils.makeAnomaly(0, 2, CONFIG_ID, METRIC_URN, 150),
            DetectionTestUtils.makeAnomaly(4, 6, CONFIG_ID, METRIC_URN, 500)).stream()
            .map(anomaly -> filter.isQualified(anomaly)).collect(
            Collectors.toList());
    Assert.assertEquals(results, Arrays.asList(false, true));
  }
}
