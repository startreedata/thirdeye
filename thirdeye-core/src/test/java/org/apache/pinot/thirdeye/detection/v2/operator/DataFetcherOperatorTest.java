package org.apache.pinot.thirdeye.detection.v2.operator;

import static org.apache.pinot.thirdeye.detection.v2.plan.DetectionPipelinePlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.detection.v2.components.datafetcher.GenericDataFetcher;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DataFetcherOperatorTest {

  @Test
  public void testNewInstance() {
    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean().setOutputs(ImmutableList.of());
    final DataSourceCache mockDataSourceCache = mock(DataSourceCache.class);
    final Map<String, Object> properties = ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY,
        mockDataSourceCache);
    final OperatorContext context = new OperatorContext().setStartTime(startTime)
        .setEndTime(endTime)
        .setDetectionPlanApi(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);
    Assert.assertEquals(dataFetcherOperator.getStartTime(), startTime);
    Assert.assertEquals(dataFetcherOperator.getEndTime(), endTime);
  }

  @Test
  public void testInitComponents() {
    Map<String, Object> params = new HashMap<>();

    params.put("component.pinot.dataSource", "pinot-cluster-1");
    params.put("component.pinot.query", "SELECT * FROM myTable");
    params.put("component.pinot.tableName", "myTable");
    params.put("component.pinot.className",
        "org.apache.pinot.thirdeye.detection.v2.components.datafetcher.GenericDataFetcher");

    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setOutputs(ImmutableList.of())
        .setInputs(ImmutableList.of())
        .setParams(params);
    final DataSourceCache mockDataSourceCache = mock(DataSourceCache.class);

    final Map<String, Object> properties = ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY,
        mockDataSourceCache);
    final OperatorContext context = new OperatorContext().setStartTime(startTime)
        .setEndTime(endTime)
        .setDetectionPlanApi(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);
    Assert.assertEquals(dataFetcherOperator.getStartTime(), startTime);
    Assert.assertEquals(dataFetcherOperator.getEndTime(), endTime);
    Assert.assertEquals(dataFetcherOperator.getComponents().size(), 1);
    final BaseComponent pinotDataFetcher = dataFetcherOperator.getComponents().get("pinot");
    Assert.assertTrue(pinotDataFetcher instanceof GenericDataFetcher);
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getQuery(), "SELECT * FROM myTable");
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getTableName(), "myTable");
  }
}
