/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.MockDataProvider;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.AlgorithmUtils;
import ai.startree.thirdeye.spi.detection.DataProvider;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.BeforeTest;

/**
 * Test class for HoltWinters detector
 */
public class HoltWintersDetectorTest {

  @Deprecated
  private DataProvider provider;

  @Deprecated
  @BeforeTest
  public void setUp() throws Exception {
    DataFrame dailyData;
    DataFrame hourlyData;
    try (Reader dataReader = new InputStreamReader(
        AlgorithmUtils.class.getResourceAsStream("/csv/daily.csv"))) {
      dailyData = DataFrame.fromCsv(dataReader);
      dailyData.setIndex(DataFrame.COL_TIME);
    }

    try (Reader dataReader = new InputStreamReader(
        AlgorithmUtils.class.getResourceAsStream("/csv/hourly.csv"))) {
      hourlyData = DataFrame.fromCsv(dataReader);
      hourlyData.setIndex(DataFrame.COL_TIME);
    }

    MetricConfigDTO dailyMetricConfig = new MetricConfigDTO();
    dailyMetricConfig.setId(1L);
    dailyMetricConfig.setName("thirdeye-test-daily");
    dailyMetricConfig.setDataset("thirdeye-test-dataset-daily");

    DatasetConfigDTO dailyDatasetConfig = new DatasetConfigDTO();
    dailyDatasetConfig.setTimeUnit(TimeUnit.DAYS);
    dailyDatasetConfig.setDataset("thirdeye-test-dataset-daily");
    dailyDatasetConfig.setTimeDuration(1);

    MetricConfigDTO hourlyMetricConfig = new MetricConfigDTO();
    hourlyMetricConfig.setId(123L);
    hourlyMetricConfig.setName("thirdeye-test-hourly");
    hourlyMetricConfig.setDataset("thirdeye-test-dataset-hourly");

    DatasetConfigDTO hourlyDatasetConfig = new DatasetConfigDTO();
    hourlyDatasetConfig.setTimeUnit(TimeUnit.HOURS);
    hourlyDatasetConfig.setDataset("thirdeye-test-dataset-hourly");
    hourlyDatasetConfig.setTimeDuration(1);

    Map<MetricSlice, DataFrame> timeseries = new HashMap<>();
    timeseries.put(MetricSlice.from(1L, 1301443200000L, 1309219200000L), dailyData);
    timeseries.put(MetricSlice.from(123L, 1317585600000L, 1323378000000L), hourlyData);
    // For Travis CI
    timeseries.put(MetricSlice.from(123L, 1317589200000L, 1323378000000L), hourlyData);

    this.provider = new MockDataProvider()
        .setTimeseries(timeseries)
        .setMetrics(Arrays.asList(hourlyMetricConfig, dailyMetricConfig))
        .setDatasets(Arrays.asList(hourlyDatasetConfig, dailyDatasetConfig));
  }
}
