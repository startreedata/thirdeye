/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.datasource.csv;

import static org.mockito.Mockito.mock;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.util.DataFrameUtils;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    ThirdEyeRequest thirdEyeRequest = makeAggregateRequest(slice,
        Collections.emptyList(),
        -1,
        "ref");
    ThirdEyeResponse response = dataSourceCache.getQueryResult(thirdEyeRequest);
    DataFrame df = DataFrameUtils.evaluateResponse(response);

    Assert.assertEquals(df.getDoubles(Constants.COL_VALUE).toList(),
        Collections.singletonList(1503d));
  }

  private ThirdEyeRequest makeAggregateRequest(MetricSlice slice, List<String> dimensions,
      int limit, String reference) {
    return DataFrameUtils.makeAggregateRequest(slice,
        dimensions,
        limit,
        reference,
        metricConfigDAO,
        datasetConfigDAO);
  }
}
