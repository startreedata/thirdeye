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
 */

package org.apache.pinot.thirdeye.detection;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.apache.pinot.thirdeye.dataframe.DataFrame;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.DAOTestBase;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.datasource.csv.CSVThirdEyeDataSource;
import org.apache.pinot.thirdeye.datasource.loader.AggregationLoader;
import org.apache.pinot.thirdeye.datasource.loader.DefaultAggregationLoader;
import org.apache.pinot.thirdeye.util.DeprecatedInjectorUtil;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CurrentAndBaselineLoaderTest {

  public static final String MY_DATA_SOURCE = "myDataSource";
  private static final String COLLECTION_VALUE = "test_dataset";
  private static final String DETECTION_NAME_VALUE = "test detection";
  private static final String METRIC_VALUE = "test_metric";
  private DAOTestBase testDAOProvider;
  private MetricConfigManager metricDAO;
  private Long detectionConfigId;
  private CurrentAndBaselineLoader currentAndBaselineLoader;

  @BeforeMethod
  public void beforeMethod() {
    this.testDAOProvider = DAOTestBase.getInstance();
    DAORegistry daoRegistry = DAORegistry.getInstance();
    final MergedAnomalyResultManager anomalyDAO = daoRegistry.getMergedAnomalyResultDAO();
    final AlertManager detectionDAO = daoRegistry.getDetectionConfigManager();
    final DatasetConfigManager dataSetDAO = daoRegistry.getDatasetConfigDAO();

    this.metricDAO = daoRegistry.getMetricConfigDAO();

    final ThirdEyeCacheRegistry cacheRegistry = DeprecatedInjectorUtil
        .getInstance(ThirdEyeCacheRegistry.class);
    cacheRegistry.registerQueryCache(buildDataSourceCache());
    cacheRegistry.initMetaDataCaches();

    this.detectionConfigId = detectionDAO.save(createAlertDTO());
    anomalyDAO.save(createMergedAnomalyResultDTO());
    dataSetDAO.save(createDatasetConfigDTO());

    final AggregationLoader aggregationLoader = new DefaultAggregationLoader(this.metricDAO,
        dataSetDAO,
        cacheRegistry.getDataSourceCache(),
        cacheRegistry.getDatasetMaxDataTimeCache());

    this.currentAndBaselineLoader = new CurrentAndBaselineLoader(this.metricDAO, dataSetDAO,
        aggregationLoader);
  }

  private AlertDTO createAlertDTO() {
    final AlertDTO detectionConfig = new AlertDTO();
    detectionConfig.setName(DETECTION_NAME_VALUE);
    return detectionConfig;
  }

  private DatasetConfigDTO createDatasetConfigDTO() {
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(COLLECTION_VALUE);
    datasetConfigDTO.setDataSource(MY_DATA_SOURCE);
    return datasetConfigDTO;
  }

  private MergedAnomalyResultDTO createMergedAnomalyResultDTO() {
    MergedAnomalyResultDTO anomalyResultDTO = new MergedAnomalyResultDTO();
    anomalyResultDTO.setStartTime(1000L);
    anomalyResultDTO.setEndTime(2000L);
    anomalyResultDTO.setDetectionConfigId(this.detectionConfigId);
    anomalyResultDTO.setCollection(COLLECTION_VALUE);
    anomalyResultDTO.setMetric(METRIC_VALUE);
    return anomalyResultDTO;
  }

  private DataSourceCache buildDataSourceCache() {
    long metricId = createMetric();

    final Map<String, ThirdEyeDataSource> dataSourceMap = new HashMap<>();
    dataSourceMap.put(MY_DATA_SOURCE, CSVThirdEyeDataSource.fromDataFrame(
        buildDatasetsMap(),
        ImmutableMap.of(metricId, "value")));
    return new DataSourceCache(dataSourceMap, Executors.newSingleThreadExecutor());
  }

  private long createMetric() {
    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    metricConfigDTO.setName(METRIC_VALUE);
    metricConfigDTO.setDataset(COLLECTION_VALUE);
    metricConfigDTO.setAlias("test");
    long metricId = this.metricDAO.save(metricConfigDTO);
    return metricId;
  }

  private Map<String, DataFrame> buildDatasetsMap() {
    DataFrame data = new DataFrame();
    data.addSeries("timestamp", 1526414678000L, 1527019478000L);
    data.addSeries("value", 100, 200);
    Map<String, DataFrame> datasets = new HashMap<>();
    datasets.put(COLLECTION_VALUE, data);
    return datasets;
  }

  @AfterMethod
  public void afterMethod() {
    this.testDAOProvider.cleanup();
  }

  @Test
  public void testfillInCurrentAndBaselineValue() throws Exception {
    List<MergedAnomalyResultDTO> anomalies = new ArrayList<>();
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setMetric(METRIC_VALUE);
    anomaly.setCollection(COLLECTION_VALUE);
    anomaly.setStartTime(1527019478000L);
    anomaly.setEndTime(1527023078000L);

    anomalies.add(anomaly);

    this.currentAndBaselineLoader.fillInCurrentAndBaselineValue(anomalies);

    Assert.assertEquals(anomaly.getAvgBaselineVal(), 100.0);
    Assert.assertEquals(anomaly.getAvgCurrentVal(), 200.0);
  }
}
