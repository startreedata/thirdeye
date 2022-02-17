/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.DefaultInputDataFetcher;
import ai.startree.thirdeye.detection.DetectionTestUtils;
import ai.startree.thirdeye.detection.MockDataProvider;
import ai.startree.thirdeye.detection.components.filters.PercentageChangeRuleAnomalyFilter;
import ai.startree.thirdeye.detection.components.filters.PercentageChangeRuleAnomalyFilterSpec;
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
import java.util.stream.Stream;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PercentageChangeRuleAnomalyFilterTest {

  private static final String METRIC_URN = "thirdeye:metric:123";
  private static final long CONFIG_ID = 125L;

  private DataProvider testDataProvider;
  private Baseline baseline;

  @BeforeMethod
  public void beforeMethod() {
    this.baseline = BaselineAggregate
        .fromWeekOverWeek(BaselineAggregateType.MEAN, 1, 1, DateTimeZone.forID("UTC"));

    MetricSlice slice1 = MetricSlice.from(123L, 1555570800000L, 1555693200000L);
    MetricSlice baselineSlice1 = this.baseline.scatter(slice1).get(0);
    MetricSlice slice2 = MetricSlice.from(123L, 1554163200000L, 1554249600000L);
    MetricSlice baselineSlice2 = this.baseline.scatter(slice2).get(0);
    MetricSlice slice3 = MetricSlice.from(123L, 1554076800000L, 1554163200000L);
    MetricSlice baselineSlice3 = this.baseline.scatter(slice3).get(0);

    Map<MetricSlice, DataFrame> aggregates = new HashMap<>();
    aggregates.put(slice1,
        new DataFrame().addSeries(DataFrame.COL_TIME, slice1.getStart()).addSeries(
            DataFrame.COL_VALUE, 150).setIndex(DataFrame.COL_TIME));
    aggregates.put(baselineSlice1,
        new DataFrame().addSeries(DataFrame.COL_TIME, baselineSlice1.getStart()).addSeries(
            DataFrame.COL_VALUE, 200).setIndex(DataFrame.COL_TIME));
    aggregates.put(slice2,
        new DataFrame().addSeries(DataFrame.COL_VALUE, 500)
            .addSeries(DataFrame.COL_TIME, slice2.getStart()).setIndex(
            DataFrame.COL_TIME));
    aggregates.put(baselineSlice2,
        new DataFrame().addSeries(DataFrame.COL_VALUE, 1000)
            .addSeries(DataFrame.COL_TIME, baselineSlice2.getStart()).setIndex(
            DataFrame.COL_TIME));
    aggregates.put(slice3,
        new DataFrame().addSeries(DataFrame.COL_VALUE, 200)
            .addSeries(DataFrame.COL_TIME, slice3.getStart()).setIndex(
            DataFrame.COL_TIME));
    aggregates.put(baselineSlice3,
        new DataFrame().addSeries(DataFrame.COL_VALUE, 150)
            .addSeries(DataFrame.COL_TIME, baselineSlice3.getStart()).setIndex(
            DataFrame.COL_TIME));

    this.testDataProvider = new MockDataProvider().setAggregates(aggregates);
  }

  @Test
  public void testPercentageChangeFilter() {
    PercentageChangeRuleAnomalyFilterSpec spec = new PercentageChangeRuleAnomalyFilterSpec();
    spec.setOffset("mean1w");
    spec.setThreshold(0.5);
    spec.setPattern("up_or_down");
    AnomalyFilter filter = new PercentageChangeRuleAnomalyFilter();
    filter.init(spec, new DefaultInputDataFetcher(this.testDataProvider, CONFIG_ID));
    List<Boolean> results =
        Stream.of(DetectionTestUtils
                .makeAnomaly(1555570800000L, 1555693200000L, CONFIG_ID, METRIC_URN, 150),
            DetectionTestUtils
                .makeAnomaly(1554163200000L, 1554249600000L, CONFIG_ID, METRIC_URN, 500))
            .map(anomaly -> filter.isQualified(anomaly))
            .collect(Collectors.toList());
    Assert.assertEquals(results, Arrays.asList(false, true));
  }

  @Test
  public void testPercentageChangeFilterTwoSide() {
    PercentageChangeRuleAnomalyFilterSpec spec = new PercentageChangeRuleAnomalyFilterSpec();
    spec.setOffset("mean1w");
    spec.setUpThreshold(0.25);
    spec.setDownThreshold(0.5);
    spec.setPattern("up_or_down");
    AnomalyFilter filter = new PercentageChangeRuleAnomalyFilter();
    filter.init(spec, new DefaultInputDataFetcher(this.testDataProvider, CONFIG_ID));
    List<Boolean> results =
        Stream.of(DetectionTestUtils
                .makeAnomaly(1555570800000L, 1555693200000L, CONFIG_ID, METRIC_URN, 150),
            DetectionTestUtils
                .makeAnomaly(1554163200000L, 1554249600000L, CONFIG_ID, METRIC_URN, 500),
            DetectionTestUtils
                .makeAnomaly(1554076800000L, 1554163200000L, CONFIG_ID, METRIC_URN, 200))
            .map(anomaly -> filter.isQualified(anomaly))
            .collect(Collectors.toList());
    Assert.assertEquals(results, Arrays.asList(false, true, true));
  }
}
