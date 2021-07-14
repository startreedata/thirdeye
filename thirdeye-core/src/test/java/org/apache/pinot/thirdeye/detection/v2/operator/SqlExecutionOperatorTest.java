package org.apache.pinot.thirdeye.detection.v2.operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SqlExecutionOperatorTest {

  @Test
  public void testSqlExecution() throws Exception {
    final SqlExecutionOperator sqlExecutionOperator = new SqlExecutionOperator();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setName("root")
        .setType("SqlExecution")
        .setParams(ImmutableMap.of("sql.queries", ImmutableList.of(
            "SELECT ts as timestamp, met as value FROM baseline",
            "SELECT ts as timestamp, met as value FROM current",
            "SELECT ts, met FROM baseline UNION ALL SELECT ts, met FROM current"
        ), "jdbc.connection", "jdbc:hsqldb:mem:SqlExecutionOperatorTest"))
        .setInputs(ImmutableList.of(
            new InputBean().setTargetProperty("baseline")
                .setSourceProperty("baselineOutput")
                .setSourcePlanNode("baselineDataFetcher"),
            new InputBean().setTargetProperty("current")
                .setSourceProperty("currentOutput")
                .setSourcePlanNode("currentDataFetcher")
        ))
        .setOutputs(ImmutableList.of(
            new OutputBean().setOutputKey("0").setOutputName("sql_0"),
            new OutputBean().setOutputKey("1").setOutputName("sql_1")
        ));
    final Map<String, Object> properties = ImmutableMap.of();
    final OperatorContext context = new OperatorContext().setStartTime(startTime)
        .setEndTime(endTime)
        .setDetectionPlanApi(planNodeBean)
        .setInputsMap(ImmutableMap.of("baseline", new SimpleDataTable(
            ImmutableList.of("ts", "met"),
            ImmutableList.of(new ColumnType(ColumnDataType.LONG),
                new ColumnType(ColumnDataType.DOUBLE)),
            ImmutableList.of(new Object[]{123L, 0.123})
        ), "current", new SimpleDataTable(
            ImmutableList.of("ts", "met"),
            ImmutableList.of(new ColumnType(ColumnDataType.LONG),
                new ColumnType(ColumnDataType.DOUBLE)),
            ImmutableList.of(new Object[]{456L, 0.456})
        )))
        .setProperties(properties);
    sqlExecutionOperator.init(context);
    Assert.assertEquals(sqlExecutionOperator.getStartTime(), startTime);
    Assert.assertEquals(sqlExecutionOperator.getEndTime(), endTime);
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
        .get("timestamp")
        .size(), 1);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_0")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("value")
        .size(), 1);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_0")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("timestamp")
        .get(0), 123L);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_0")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("value")
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
        .get("timestamp")
        .size(), 1);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_1")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("value")
        .size(), 1);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_1")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("timestamp")
        .get(0), 456L);
    Assert.assertEquals(sqlExecutionOperator.getOutputs()
        .get("sql_1")
        .getDetectionResults()
        .get(0)
        .getRawData()
        .get("value")
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
