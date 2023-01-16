/*
 * Copyright 2023 StarTree Inc
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

import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class SqlExecutionOperatorTest {

  @Test
  public void testSqlExecutionDefaultAdapter() throws Exception {
    testSqlExecution(new HashMap<>());
  }

  @Test
  public void testSqlExecutionHyperSQLAdapter() throws Exception {
    testSqlExecution(ImmutableMap.of("sql.engine", "HyperSql"));
  }

  @Test
  public void testSqlExecutionCalciteAdapter() throws Exception {
    testSqlExecution(ImmutableMap.of("sql.engine", "Calcite"));
  }

  private void testSqlExecution(Map<String, Object> customParams) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("sql.queries",
        ImmutableList.of("SELECT ts as timestamp_res, met as value_res FROM baseline_data",
            "SELECT ts as timestamp_res, met as value_res FROM current_data",
            "SELECT ts, met FROM baseline_data UNION ALL SELECT ts, met FROM current_data"));
    // put custom params
    params.putAll(customParams);

    final DetectionPipelineOperator sqlExecutionOperator = new SqlExecutionOperator();
    final PlanNodeBean planNodeBean = new PlanNodeBean().setName("root")
        .setType("SqlExecution")
        .setParams(TemplatableMap.fromValueMap(params))
        .setInputs(ImmutableList.of(new InputBean().setTargetProperty("baseline_data")
                .setSourceProperty("baselineOutput")
                .setSourcePlanNode("baselineDataFetcher"),
            new InputBean().setTargetProperty("current_data")
                .setSourceProperty("currentOutput")
                .setSourcePlanNode("currentDataFetcher")))
        .setOutputs(ImmutableList.of(new OutputBean().setOutputKey("0").setOutputName("sql_0"),
            new OutputBean().setOutputKey("1").setOutputName("sql_1")));
    final Map<String, Object> properties = ImmutableMap.of();
    final long startTime = System.currentTimeMillis();
    final long endTime = startTime + 1000L;
    final Interval detectionInterval = new Interval(startTime, endTime, DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext().setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setInputsMap(ImmutableMap.of("baseline_data",
            SimpleDataTable.fromDataFrame(new DataFrame()
                .addSeries("ts", LongSeries.buildFrom(123L))
                .addSeries("met", DoubleSeries.buildFrom(0.123))
            ),
            "current_data",
            SimpleDataTable.fromDataFrame(new DataFrame()
                .addSeries("ts", LongSeries.buildFrom(456L))
                .addSeries("met", DoubleSeries.buildFrom(0.456))
            )))
        .setProperties(properties);
    sqlExecutionOperator.init(context);
    assertThat(sqlExecutionOperator.getDetectionInterval()).isEqualTo(detectionInterval);
    sqlExecutionOperator.execute();
    assertThat(sqlExecutionOperator.getOutputs().size()).isEqualTo(3);
    final DataFrame dataframe0 = ((DataTable) sqlExecutionOperator.getOutputs()
        .get("sql_0")).getDataFrame();
    assertThat(dataframe0.getSeriesNames().size()).isEqualTo(2);
    assertThat(dataframe0.size()).isEqualTo(1);
    assertThat(dataframe0.getLong("timestamp_res", 0)).isEqualTo(123L);
    assertThat(dataframe0.getDouble("value_res", 0)).isEqualTo(0.123);

    final DataFrame dataFrame1 = ((DataTable) sqlExecutionOperator.getOutputs()
        .get("sql_1")).getDataFrame();
    assertThat(dataFrame1.getSeriesNames().size()).isEqualTo(2);
    assertThat(dataFrame1.size()).isEqualTo(1);
    assertThat(dataFrame1.getLong("timestamp_res", 0)).isEqualTo(456L);
    assertThat(dataFrame1.getDouble("value_res", 0)).isEqualTo(0.456);

    final DataFrame dataFrame2 = ((DataTable) sqlExecutionOperator.getOutputs()
        .get("2")).getDataFrame();
    assertThat(dataFrame2.getSeriesNames().size()).isEqualTo(2);
    assertThat(dataFrame2.size()).isEqualTo(2);
    assertThat(dataFrame2.getLong("ts", 0)).isEqualTo(123L);
    assertThat(dataFrame2.getDouble("met", 0)).isEqualTo(0.123);
    assertThat(dataFrame2.getLong("ts", 1)).isEqualTo(456L);
    assertThat(dataFrame2.getDouble("met", 1)).isEqualTo(0.456);
  }
}
