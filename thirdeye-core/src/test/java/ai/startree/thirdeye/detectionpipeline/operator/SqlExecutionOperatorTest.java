/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.operator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SqlExecutionOperatorTest {

  @Test
  public void testSqlExecutionDefaultAdapter() throws Exception {
    testSqlExecution(new HashMap<>());
  }

  @Test
  public void testSqlExecutionHyperSQLAdapter() throws Exception {
    testSqlExecution(ImmutableMap.of(
        "sql.engine", "HyperSql"
    ));
  }

  @Test
  public void testSqlExecutionCalciteAdapter() throws Exception {
    testSqlExecution(ImmutableMap.of(
        "sql.engine", "Calcite"
    ));
  }

  private void testSqlExecution(Map<String, Object> customParams) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("sql.queries", ImmutableList.of(
        "SELECT ts as timestamp_res, met as value_res FROM baseline_data",
        "SELECT ts as timestamp_res, met as value_res FROM current_data",
        "SELECT ts, met FROM baseline_data UNION ALL SELECT ts, met FROM current_data"
    ));
    // put custom params
    params.putAll(customParams);

    final DetectionPipelineOperator sqlExecutionOperator = new SqlExecutionOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setName("root")
        .setType("SqlExecution")
        .setParams(params)
        .setInputs(ImmutableList.of(
            new InputBean().setTargetProperty("baseline_data")
                .setSourceProperty("baselineOutput")
                .setSourcePlanNode("baselineDataFetcher"),
            new InputBean().setTargetProperty("current_data")
                .setSourceProperty("currentOutput")
                .setSourcePlanNode("currentDataFetcher")
        ))
        .setOutputs(ImmutableList.of(
            new OutputBean().setOutputKey("0").setOutputName("sql_0"),
            new OutputBean().setOutputKey("1").setOutputName("sql_1")
        ));
    final Map<String, Object> properties = ImmutableMap.of();
    final Interval detectionInterval = new Interval(startTime, endTime, DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setInputsMap(ImmutableMap.of("baseline_data", new SimpleDataTable(
            ImmutableList.of("ts", "met"),
            ImmutableList.of(new ColumnType(ColumnDataType.LONG),
                new ColumnType(ColumnDataType.DOUBLE)),
            ImmutableList.of(new Object[]{123L, 0.123})
        ), "current_data", new SimpleDataTable(
            ImmutableList.of("ts", "met"),
            ImmutableList.of(new ColumnType(ColumnDataType.LONG),
                new ColumnType(ColumnDataType.DOUBLE)),
            ImmutableList.of(new Object[]{456L, 0.456})
        )))
        .setProperties(properties);
    sqlExecutionOperator.init(context);
    assertThat(sqlExecutionOperator.getDetectionInterval()).isEqualTo(detectionInterval);
    sqlExecutionOperator.execute();
    Assert.assertEquals(sqlExecutionOperator.getOutputs().size(), 3);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_0")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .size(), 2);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_0")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("timestamp_res")
        .size(), 1);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_0")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("value_res")
        .size(), 1);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_0")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("timestamp_res")
        .get(0), 123L);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_0")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("value_res")
        .get(0), 0.123);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_1")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .size(), 2);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_1")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("timestamp_res")
        .size(), 1);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_1")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("value_res")
        .size(), 1);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_1")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("timestamp_res")
        .get(0), 456L);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_1")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("value_res")
        .get(0), 0.456);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("2")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .size(), 2);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("2")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("ts")
        .size(), 2);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("2")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("met")
        .size(), 2);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("2")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("ts")
        .get(0), 123L);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("2")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("met")
        .get(0), 0.123);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("2")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("ts")
        .get(1), 456L);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("2")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("met")
        .get(1), 0.456);
  }
}
