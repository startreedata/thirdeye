/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.detectionpipeline.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datalayer.core.EnumerationItemMaintainer;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.ApplicationContext;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineConfiguration;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.PostProcessorRegistry;
import ai.startree.thirdeye.detectionpipeline.components.GenericDataFetcher;
import ai.startree.thirdeye.detectionpipeline.spec.DataFetcherSpec;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.detection.BaseComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataFetcherOperatorTest {

  public static final String TABLE_NAME = "myTable";
  private String dataSourceName;
  private PlanNodeContext planNodeContext;

  @BeforeMethod
  public void setUp() {
    dataSourceName = "pinot-cluster-1";
    final DataSourceDTO dataSourceDTO = new DataSourceDTO().setName("datasource1");
    dataSourceDTO.setId(1L);
    final DataSourceCache dataSourceCache = mock(DataSourceCache.class);
    final DatasetConfigManager datasetDao = mock(DatasetConfigManager.class);
    when(datasetDao.findByDatasetAndNamespaceOrUnsetNamespace(anyString(), nullable(String.class)))
        .thenReturn(new DatasetConfigDTO().setDataset(TABLE_NAME));
    final ThirdEyeDataSource thirdEyeDataSource = mock(ThirdEyeDataSource.class);
    when(dataSourceCache.getDataSource(dataSourceDTO))
        .thenReturn(thirdEyeDataSource);
    final DataSourceManager dataSourceDao = mock(DataSourceManager.class);
    when(dataSourceDao.findUniqueByNameAndNamespace(anyString(), nullable(String.class)))
        .thenReturn(dataSourceDTO);
    planNodeContext = new PlanNodeContext().setDetectionPipelineContext(
        new DetectionPipelineContext().setApplicationContext(
            new ApplicationContext(
                dataSourceCache,
                mock(DetectionRegistry.class),
                mock(PostProcessorRegistry.class),
                mock(EventManager.class),
                dataSourceDao, 
                datasetDao,
                mock(ExecutorService.class),
                new DetectionPipelineConfiguration(),
                mock(EnumerationItemMaintainer.class))
    ));
  }

  @Test
  public void testNewInstance() {
    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setParams(TemplatableMap.fromValueMap(ImmutableMap.of(
            "component.dataSource", dataSourceName,
            "component.tableName", TABLE_NAME)))
        .setOutputs(ImmutableList.of());
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final Interval detectionInterval = new Interval(startTime, endTime, DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setPlanNodeContext(planNodeContext);
    dataFetcherOperator.init(context);
    assertThat(dataFetcherOperator.getDetectionInterval()).isEqualTo(detectionInterval);
  }

  @Test
  public void testInitComponents() {
    final Map<String, Object> params = new HashMap<>();

    params.put("component.dataSource", dataSourceName);
    params.put("component.query", "SELECT * FROM " + TABLE_NAME);
    params.put("component.tableName", TABLE_NAME);

    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setOutputs(ImmutableList.of())
        .setInputs(ImmutableList.of())
        .setParams(TemplatableMap.fromValueMap(params));

    final Interval detectionInterval = new Interval(startTime, endTime, DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setPlanNodeContext(planNodeContext);
    dataFetcherOperator.init(context);

    assertThat(dataFetcherOperator.getDetectionInterval()).isEqualTo(detectionInterval);

    final BaseComponent<DataFetcherSpec> pinotDataFetcher = dataFetcherOperator.getDataFetcher();

    Assert.assertTrue(pinotDataFetcher instanceof GenericDataFetcher);
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getQuery(),
        "SELECT * FROM " + TABLE_NAME);
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getTableName(), TABLE_NAME);
  }
}
