/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource.mock;

import ai.startree.thirdeye.plugins.datasource.mock.MockThirdEyeDataSource;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MockThirdEyeDataSourceTest {

  private static final String COL_PURCHASES = "purchases";
  private static final String COL_REVENUE = "revenue";
  private static final String COL_PAGE_VIEWS = "pageViews";
  private static final String COL_AD_IMPRESSIONS = "adImpressions";

  private MockThirdEyeDataSource dataSource;

  private static DatasetConfigDTO newDataset(final String name) {
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(name);
    return datasetConfigDTO;
  }

  @BeforeMethod
  public void beforeMethod() throws Exception {
    try (Reader dataReader = new InputStreamReader(
        this.getClass().getResourceAsStream("mockThirdEyeDataSource-properties.json"))) {

      final DataSourceDTO dataSourceDTO = new DataSourceDTO();
      dataSourceDTO
          .setName("mock1")
          .setProperties(new ObjectMapper().readValue(dataReader, Map.class));

      this.dataSource = new MockThirdEyeDataSource();
      this.dataSource.init(new ThirdEyeDataSourceContext().setDataSourceDTO(dataSourceDTO));
    }
  }

  @Test
  public void testGetMaxTime() throws Exception {
    long time = System.currentTimeMillis();
    Assert.assertTrue(this.dataSource.getMaxDataTime(
        newDataset("business")) <= time);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetMaxTimeInvalidDataset() throws Exception {
    this.dataSource.getMaxDataTime(
        newDataset("invalid"));
  }

  @Test
  public void testGetDatasets() throws Exception {
    Assert.assertEquals(this.dataSource.getDatasets(),
        new HashSet<>(Arrays.asList("business", "tracking")));
  }

  @Test
  public void testGetDimensionFiltersTracking() throws Exception {
    Map<String, List<String>> filters = this.dataSource.getDimensionFilters(
        newDataset("tracking"));
    Assert.assertEquals(filters.keySet(),
        new HashSet<>(Arrays.asList("country", "browser", "platform")));
    Assert.assertEquals(new HashSet<>(filters.get("country")),
        new HashSet<>(Arrays.asList("ca", "mx", "us")));
    Assert.assertEquals(new HashSet<>(filters.get("browser")),
        new HashSet<>(Arrays.asList("chrome", "edge", "firefox", "safari")));
    Assert.assertEquals(new HashSet<>(filters.get("platform")),
        new HashSet<>(Arrays.asList("desktop", "mobile")));
  }

  @Test
  public void testGetDimensionFiltersBusiness() throws Exception {
    Map<String, List<String>> filters = this.dataSource.getDimensionFilters(newDataset("business"));
    Assert.assertEquals(filters.keySet(), new HashSet<>(Arrays.asList("country", "browser")));
    Assert.assertEquals(new HashSet<>(filters.get("country")),
        new HashSet<>(Arrays.asList("ca", "mx", "us")));
    Assert.assertEquals(new HashSet<>(filters.get("browser")),
        new HashSet<>(Arrays.asList("chrome", "edge", "safari")));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetDimensionFiltersInvalidDataset() throws Exception {
    this.dataSource.getDimensionFilters(newDataset("invalid"));
  }

  @Test(enabled = false)
  public void testDataGenerator() {
    Assert.assertEquals(this.dataSource.datasetData.size(), 2);
    Assert.assertEquals(this.dataSource.datasetData.get("business").size(), 28 * 9);
    Assert
        .assertTrue(this.dataSource.datasetData.get("tracking").size() > 27 * 21); // allow for DST
  }

  @Test
  public void testDataGeneratorHourly() {
    DataFrame data = this.dataSource.datasetData.get("tracking");

    System.out.println(data.getDoubles(COL_PAGE_VIEWS).count());

    // allow for DST
    Assert
        .assertTrue(data.getDoubles(COL_PAGE_VIEWS).count() >= (28 * 24 - 1) * 21); // allow for DST
    Assert.assertTrue(data.getDoubles(COL_PAGE_VIEWS).count() <= (28 * 24 + 1) * 21);
    Assert.assertTrue(
        data.getDoubles(COL_AD_IMPRESSIONS).count() >= (28 * 24 - 1) * 7); // allow for DST
    Assert.assertTrue(data.getDoubles(COL_AD_IMPRESSIONS).count() <= (28 * 24 + 1) * 7);

    Assert.assertTrue(data.getDoubles(COL_PAGE_VIEWS).sum().doubleValue() > 0);
    Assert.assertTrue(data.getDoubles(COL_AD_IMPRESSIONS).sum().doubleValue() > 0);
  }

  @Test(enabled = false)
  public void testDataGeneratorDaily() {
    DataFrame data = this.dataSource.datasetData.get("business");
    Assert.assertEquals(data.getDoubles(COL_PURCHASES).count(), 28 * 9);
    Assert.assertEquals(data.getDoubles(COL_REVENUE).count(), 28 * 9);

    Assert.assertTrue(data.getDoubles(COL_PURCHASES).sum().doubleValue() > 0);
    Assert.assertTrue(data.getDoubles(COL_REVENUE).sum().doubleValue() > 0);
  }

  @Test
  public void testExecute() throws Exception {
    long time = System.currentTimeMillis();

    // reverse lookup hack for metric id
    Long metricId = null;
    for (Map.Entry<Long, String> entry : this.dataSource.metricNameMap.entrySet()) {
      if (COL_PAGE_VIEWS.equals(entry.getValue())) {
        metricId = entry.getKey();
      }
    }

    MetricFunction metricFunction = new MetricFunction(MetricAggFunction.SUM, COL_PAGE_VIEWS,
        metricId, "tracking", null, null);

    ThirdEyeRequest request = ThirdEyeRequest.newBuilder()
        .setStartTimeInclusive(new DateTime(time, DateTimeZone.UTC).minus(Period.days(1)))
        .setEndTimeExclusive(new DateTime(time, DateTimeZone.UTC))
        .setMetricFunction(metricFunction)
        .setGroupBy("browser")
        .build("ref");

    ThirdEyeResponse response = this.dataSource.execute(request);

    Assert.assertEquals(response.getNumRows(), 4);

    Set<String> resultDimensions = new HashSet<>();
    for (int i = 0; i < response.getNumRows(); i++) {
      Assert.assertTrue(response.getRow(i).getMetrics().get(0) > 0);
      resultDimensions.add(response.getRow(i).getDimensions().get(0));
    }

    Assert.assertEquals(resultDimensions,
        new HashSet<>(Arrays.asList("chrome", "edge", "firefox", "safari")));
  }

  @Test
  public void testDeterministicMetricOrder() {
    Assert.assertEquals(this.dataSource.metricNameMap.get(1L), "purchases");
    Assert.assertEquals(this.dataSource.metricNameMap.get(2L), "revenue");
    Assert.assertEquals(this.dataSource.metricNameMap.get(3L), "adImpressions");
    Assert.assertEquals(this.dataSource.metricNameMap.get(4L), "pageViews");
  }
}
