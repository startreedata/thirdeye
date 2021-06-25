/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.datasource.csv;

import static org.mockito.Mockito.mock;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.datasource.DataSourcesLoader;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DataSourceManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.apache.pinot.thirdeye.util.DataFrameUtils;
import org.apache.pinot.thirdeye.util.RequestContainer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CSVThirdEyeDataSourceIntegrationTest {

  private DatasetConfigManager datasetConfigDAO;
  private MetricConfigManager metricConfigDAO;

  @BeforeMethod
  void beforeMethod() {
    final Injector injector = new TestDbEnv().getInjector();
    datasetConfigDAO = injector.getInstance(DatasetConfigManager.class);
    metricConfigDAO = injector.getInstance(MetricConfigManager.class);
  }

  @AfterMethod(alwaysRun = true)
  void afterMethod() {
  }

  @Test(enabled = false)
  public void integrationTest() throws Exception {
    URL dataSourcesConfig = this.getClass().getResource("data-sources-config.yml");

    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();

    datasetConfigDTO.setDataset("business");
    datasetConfigDTO.setDataSource("CSVThirdEyeDataSource");
    datasetConfigDTO.setTimeDuration(1);
    datasetConfigDTO.setTimeUnit(TimeUnit.HOURS);

    datasetConfigDAO.save(datasetConfigDTO);
    Assert.assertNotNull(datasetConfigDTO.getId());

    MetricConfigDTO configDTO = new MetricConfigDTO();
    configDTO.setName("views");
    configDTO.setDataset("business");
    configDTO.setAlias("business::views");

    metricConfigDAO.save(configDTO);
    Assert.assertNotNull(configDTO.getId());

    final DataSourcesLoader dataSourcesLoader = new DataSourcesLoader(metricConfigDAO,
        datasetConfigDAO);
    final DataSourceCache dataSourceCache = new DataSourceCache(mock(DataSourceManager.class),
        dataSourcesLoader,
        new MetricRegistry());
    final ThirdEyeCacheRegistry thirdEyeCacheRegistry = new ThirdEyeCacheRegistry(
        metricConfigDAO,
        datasetConfigDAO,
        dataSourceCache);

    thirdEyeCacheRegistry.initializeCaches();

    MetricSlice slice = MetricSlice.from(configDTO.getId(), 0, 7200000);
    RequestContainer requestContainer = makeAggregateRequest(slice,
        Collections.emptyList(),
        -1,
        "ref",
        thirdEyeCacheRegistry);
    ThirdEyeResponse response = dataSourceCache
        .getQueryResult(requestContainer.getRequest());
    DataFrame df = DataFrameUtils.evaluateResponse(response, requestContainer,
        thirdEyeCacheRegistry);

    Assert.assertEquals(df.getDoubles(DataFrame.COL_VALUE).toList(),
        Collections.singletonList(1503d));
  }

  private RequestContainer makeAggregateRequest(MetricSlice slice, List<String> dimensions,
      int limit, String reference, final ThirdEyeCacheRegistry thirdEyeCacheRegistry)
      throws Exception {
    return DataFrameUtils
        .makeAggregateRequest(slice, dimensions, limit, reference, metricConfigDAO, datasetConfigDAO,
            thirdEyeCacheRegistry);
  }
}
