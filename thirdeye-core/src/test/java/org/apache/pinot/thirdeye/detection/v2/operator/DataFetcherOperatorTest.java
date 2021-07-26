package org.apache.pinot.thirdeye.detection.v2.operator;

import static org.apache.pinot.thirdeye.detection.v2.plan.PlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.detection.v2.components.datafetcher.GenericDataFetcher;
import org.apache.pinot.thirdeye.detection.v2.plan.DetectionPipelinePlanNode;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DataFetcherOperatorTest {

  @Test
  public void testNewInstance() {
    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long currentTimeMillis = System.currentTimeMillis();
    final String startTime = String.valueOf(currentTimeMillis);
    final String endTime = String.valueOf(currentTimeMillis + 1000L);
    final PlanNodeBean planNodeBean = new PlanNodeBean().setOutputs(ImmutableList.of());
    final DataSourceCache mockDataSourceCache = mock(DataSourceCache.class);
    final Map<String, Object> properties = ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY,
        mockDataSourceCache);
    final OperatorContext context = new OperatorContext().setStartTime(startTime)
        .setEndTime(endTime)
        .setDetectionPlanApi(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);
    Assert.assertEquals(String.valueOf(dataFetcherOperator.getStartTime()), startTime);
    Assert.assertEquals(String.valueOf(dataFetcherOperator.getEndTime()), endTime);
  }

  @Test
  public void testInitComponents() {
    Map<String, Object> params = new HashMap<>();

    final String dataSourceName = "pinot-cluster-1";
    params.put("component.pinot.dataSource", dataSourceName);
    params.put("component.pinot.query", "SELECT * FROM myTable");
    params.put("component.pinot.tableName", "myTable");
    params.put("component.pinot.className",
        "org.apache.pinot.thirdeye.detection.v2.components.datafetcher.GenericDataFetcher");

    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long currentTimeMillis = System.currentTimeMillis();
    final String startTime = String.valueOf(currentTimeMillis);
    final String endTime = String.valueOf(currentTimeMillis + 1000L);
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setOutputs(ImmutableList.of())
        .setInputs(ImmutableList.of())
        .setParams(params);
    final DataSourceCache dataSourceCache = mock(DataSourceCache.class);
    final ThirdEyeDataSource thirdEyeDataSource = mock(ThirdEyeDataSource.class);
    when(dataSourceCache.getDataSource(dataSourceName))
        .thenReturn(thirdEyeDataSource);

    final Map<String, Object> properties = ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY,
        dataSourceCache);
    final OperatorContext context = new OperatorContext().setStartTime(startTime)
        .setEndTime(endTime)
        .setDetectionPlanApi(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);

    Assert.assertEquals(String.valueOf(dataFetcherOperator.getStartTime()), startTime);
    Assert.assertEquals(String.valueOf(dataFetcherOperator.getEndTime()), endTime);
    Assert.assertEquals(dataFetcherOperator.getComponents().size(), 1);

    final BaseComponent pinotDataFetcher = dataFetcherOperator.getComponents().get("pinot");

    Assert.assertTrue(pinotDataFetcher instanceof GenericDataFetcher);
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getQuery(), "SELECT * FROM myTable");
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getTableName(), "myTable");
  }
}
