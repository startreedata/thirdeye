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

import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AnomalyDetectorWrapperTest {

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
