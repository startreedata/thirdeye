/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.csv;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponseRow;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CSVThirdEyeResponseTest {

  CSVThirdEyeResponse response;

  @BeforeMethod
  public void beforeMethod() {
    ThirdEyeRequest request = ThirdEyeRequest.
        newBuilder()
        .setStartTimeInclusive(new DateTime(0, DateTimeZone.UTC))
        .setEndTimeExclusive(new DateTime(100, DateTimeZone.UTC))
        .addGroupBy("country")
        .addMetricFunction(
            new MetricFunction(MetricAggFunction.AVG, "views", 0L, "source", null, null))
        .build("");
    response = new CSVThirdEyeResponse(
        request,
        new TimeSpec("timestamp", new TimeGranularity(1, TimeUnit.HOURS),
            TimeSpec.SINCE_EPOCH_FORMAT),
        new DataFrame().addSeries("timestamp", 100).addSeries("country", "us")
            .addSeries("AVG_views", 1000)
    );
  }

  @Test
  public void testGetNumRows() {
    Assert.assertEquals(response.getNumRows(), 1);
  }

  @Test
  public void testGetRow() {
    ThirdEyeResponseRow responseRow = response.getRow(0);
    Assert.assertEquals(responseRow.getDimensions(), Collections.singletonList("us"));
    Assert.assertEquals(responseRow.getMetrics(), Collections.singletonList(1000.0));
    Assert.assertEquals(responseRow.getTimeBucketId(), 0);
  }

  @Test
  public void testGetNumRowsFor() {
    Assert.assertEquals(response.getNumRows(), 1);
  }

  @Test
  public void testGetRowMap() {
    Map<String, String> map = new HashMap<>();
    map.put("country", "us");
    map.put("AVG_views", "1000");
    Assert.assertEquals(
        response.getRow(
            new MetricFunction(MetricAggFunction.AVG, "views", 0L, "source", null, null),
            0), map);
  }
}
