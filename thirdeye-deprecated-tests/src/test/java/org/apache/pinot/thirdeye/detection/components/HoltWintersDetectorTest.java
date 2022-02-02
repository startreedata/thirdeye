/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.detection.components;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.MockDataProvider;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.AlgorithmUtils;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
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
