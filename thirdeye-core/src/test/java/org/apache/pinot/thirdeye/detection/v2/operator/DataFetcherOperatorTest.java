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
import org.apache.pinot.thirdeye.detection.v2.spec.DataFetcherSpec;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataFetcherOperatorTest {

  private DataSourceCache dataSourceCache;
  private String dataSourceName;

  @BeforeMethod
  public void setUp() {
    dataSourceName = "pinot-cluster-1";

    dataSourceCache = mock(DataSourceCache.class);
    final ThirdEyeDataSource thirdEyeDataSource = mock(ThirdEyeDataSource.class);
    when(dataSourceCache.getDataSource(dataSourceName))
        .thenReturn(thirdEyeDataSource);
  }

  @Test
  public void testNewInstance() {
    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setParams(ImmutableMap.of("component.dataSource", dataSourceName))
        .setOutputs(ImmutableList.of());
    final Map<String, Object> properties = ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY,
        dataSourceCache);
    final OperatorContext context = new OperatorContext().setStartTime(startTime)
        .setEndTime(endTime)
        .setPlanNode(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);
    Assert.assertEquals(dataFetcherOperator.getStartTime(), startTime);
    Assert.assertEquals(dataFetcherOperator.getEndTime(), endTime);
  }

  @Test
  public void testInitComponents() {
    Map<String, Object> params = new HashMap<>();

    params.put("component.dataSource", dataSourceName);
    params.put("component.query", "SELECT * FROM myTable");
    params.put("component.tableName", "myTable");
    params.put("component.className",
        "org.apache.pinot.thirdeye.detection.v2.components.datafetcher.GenericDataFetcher");

    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setOutputs(ImmutableList.of())
        .setInputs(ImmutableList.of())
        .setParams(params);

    final Map<String, Object> properties = ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY,
        dataSourceCache);
    final OperatorContext context = new OperatorContext().setStartTime(startTime)
        .setEndTime(endTime)
        .setPlanNode(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);

    Assert.assertEquals(dataFetcherOperator.getStartTime(), startTime);
    Assert.assertEquals(dataFetcherOperator.getEndTime(), endTime);

    final BaseComponent<DataFetcherSpec> pinotDataFetcher = dataFetcherOperator.getDataFetcher();

    Assert.assertTrue(pinotDataFetcher instanceof GenericDataFetcher);
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getQuery(),
        "SELECT * FROM myTable");
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getTableName(), "myTable");
  }
}
