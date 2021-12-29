/*
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.detection.wrapper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.MockDataProvider;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnomalyDetectorWrapperTest {

  @BeforeMethod
  public void setUp() {
    MockDataProvider provider = new MockDataProvider();
    MetricConfigDTO metric = new MetricConfigDTO();
    metric.setId(1L);
    metric.setDataset("test");
    provider.setMetrics(Collections.singletonList(metric));
    DatasetConfigDTO dataset = new DatasetConfigDTO();
    dataset.setDataset("test");
    dataset.setTimeUnit(TimeUnit.DAYS);
    dataset.setTimeDuration(1);
    provider.setDatasets(Collections.singletonList(dataset));
    provider.setTimeseries(ImmutableMap.of(
        MetricSlice.from(1L, 1546646400000L, 1546732800000L),
        new DataFrame().addSeries(DataFrame.COL_VALUE, 500, 1000)
            .addSeries(DataFrame.COL_TIME, 1546646400000L, 1546732800000L),
        MetricSlice.from(1L, 1546819200000L, 1546905600000L),
        DataFrame.builder(DataFrame.COL_TIME, DataFrame.COL_VALUE).build(),
        MetricSlice.from(1L, 1546300800000L, 1546560000000L),
        new DataFrame().addSeries(DataFrame.COL_VALUE, 500, 1000)
            .addSeries(DataFrame.COL_TIME, 1546300800000L, 1546387200000L),
        MetricSlice.from(1L, 1540147725000L - TimeUnit.DAYS.toMillis(90), 1540493325000L,
            HashMultimap.create(),
            new TimeGranularity(1, TimeUnit.DAYS)),
        new DataFrame().addSeries(DataFrame.COL_VALUE, 500, 1000)
            .addSeries(DataFrame.COL_TIME, 1546646400000L, 1546732800000L),
        MetricSlice.from(1L, 1540080000000L - TimeUnit.DAYS.toMillis(90), 1540425600000L,
            HashMultimap.create(),
            new TimeGranularity(1, TimeUnit.DAYS)),
        new DataFrame().addSeries(DataFrame.COL_VALUE, 500, 1000)
            .addSeries(DataFrame.COL_TIME, 1546646400000L, 1546732800000L)));
  }

  @Test
  public void testConsolidateTimeSeries() {
    TimeSeries ts1 =
        new TimeSeries(LongSeries.buildFrom(1L, 2L, 3L, 4L, 5L),
            DoubleSeries.buildFrom(1.0, 20.0, 30.0, 40.0, 50.0),
            DoubleSeries.buildFrom(1.0, 2.0, 3.0, 4.0, 5.0),
            DoubleSeries.buildFrom(1.0, 2.0, 3.0, 4.0, 5.0),
            DoubleSeries.buildFrom(1.0, 2.0, 3.0, 4.0, 5.0));
    TimeSeries ts2 =
        new TimeSeries(LongSeries.buildFrom(2L, 3L, 4L, 5L, 6L),
            DoubleSeries.buildFrom(1.0, 2.0, 3.0, 4.0, 5.0),
            DoubleSeries.buildFrom(2.0, 3.0, 4.0, 5.0, 6.0),
            DoubleSeries.buildFrom(1.0, 2.0, 3.0, 4.0, 5.0),
            DoubleSeries.buildFrom(1.0, 2.0, 3.0, 4.0, 5.0));
    TimeSeries result = AnomalyDetectorWrapper.consolidateTimeSeries(ts1, ts2);
    Assert.assertEquals(result.getTime(), LongSeries.buildFrom(1L, 2L, 3L, 4L, 5L, 6L));
    Assert.assertEquals(result.getCurrent(), DoubleSeries.buildFrom(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
    Assert.assertEquals(result.getPredictedBaseline(),
        DoubleSeries.buildFrom(1.0, 1.0, 2.0, 3.0, 4.0, 5.0));
    Assert.assertEquals(result.getPredictedUpperBound(),
        DoubleSeries.buildFrom(1.0, 1.0, 2.0, 3.0, 4.0, 5.0));
    Assert.assertEquals(result.getPredictedLowerBound(),
        DoubleSeries.buildFrom(1.0, 1.0, 2.0, 3.0, 4.0, 5.0));
  }

  @Test
  public void testConsolidateTimeSeriesWithNull() {
    TimeSeries ts1 = TimeSeries.empty();
    TimeSeries ts2 = new TimeSeries(LongSeries.buildFrom(1L, 2L, 3L, 4L, 5L),
        DoubleSeries.buildFrom(1.0, 20.0, 30.0, 40.0, 50.0));
    TimeSeries ts3 = new TimeSeries(LongSeries.buildFrom(2L, 3L, 4L, 5L, 6L),
        DoubleSeries.buildFrom(2.0, 3.0, 4.0, Double.NaN, 6.0));
    TimeSeries result1 = AnomalyDetectorWrapper.consolidateTimeSeries(ts1, ts2);
    TimeSeries result = AnomalyDetectorWrapper.consolidateTimeSeries(result1, ts3);
    Assert.assertEquals(result.getTime(), LongSeries.buildFrom(1L, 2L, 3L, 4L, 5L, 6L));
    Assert.assertEquals(result.getPredictedBaseline(),
        DoubleSeries.buildFrom(1.0, 2.0, 3.0, 4.0, Double.NaN, 6.0));
  }
}
