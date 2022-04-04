/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.operator;

import static ai.startree.thirdeye.detectionpipeline.plan.PlanNodeFactory.DATA_SOURCE_CACHE_REF_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.components.GenericDataFetcher;
import ai.startree.thirdeye.detectionpipeline.spec.DataFetcherSpec;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.detection.BaseComponent;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
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
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setParams(ImmutableMap.of("component.dataSource", dataSourceName))
        .setOutputs(ImmutableList.of());
    final Map<String, Object> properties = ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY,
        dataSourceCache);
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final Interval detectionInterval = new Interval(startTime, endTime, DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);
    assertThat(dataFetcherOperator.getDetectionInterval()).isEqualTo(detectionInterval);
  }

  @Test
  public void testInitComponents() {
    Map<String, Object> params = new HashMap<>();

    params.put("component.dataSource", dataSourceName);
    params.put("component.query", "SELECT * FROM myTable");
    params.put("component.tableName", "myTable");
    params.put("component.className",
        "ai.startree.thirdeye.detection.v2.components.datafetcher.GenericDataFetcher");

    final DataFetcherOperator dataFetcherOperator = new DataFetcherOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setOutputs(ImmutableList.of())
        .setInputs(ImmutableList.of())
        .setParams(params);

    final Map<String, Object> properties = ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY,
        dataSourceCache);
    final Interval detectionInterval = new Interval(startTime, endTime, DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setProperties(properties);
    dataFetcherOperator.init(context);

    assertThat(dataFetcherOperator.getDetectionInterval()).isEqualTo(detectionInterval);

    final BaseComponent<DataFetcherSpec> pinotDataFetcher = dataFetcherOperator.getDataFetcher();

    Assert.assertTrue(pinotDataFetcher instanceof GenericDataFetcher);
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getQuery(),
        "SELECT * FROM myTable");
    Assert.assertEquals(((GenericDataFetcher) pinotDataFetcher).getTableName(), "myTable");
  }
}
