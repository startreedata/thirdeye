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
package ai.startree.thirdeye.detectionpipeline.operator;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class TimeIndexFillerOperatorTest {

  private static final long OCTOBER_22_MILLIS = 1634860800000L;
  private static final long OCTOBER_23_MILLIS = 1634947200000L;
  private static final long OCTOBER_24_MILLIS = 1635033600000L;
  private static final long OCTOBER_25_MILLIS = 1635120000000L;
  private static final long OCTOBER_26_MILLIS = 1635206400000L;

  private static final double METRIC_VALUE = 1.1;
  private static final double ZERO_FILLER = 0;

  @Test
  public void testTimeIndexFillerExecutionFillMiddleInferBoundsFromData() throws Exception {
    final TimeIndexFillerOperator timeIndexFillerOperator = new TimeIndexFillerOperator();

    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setName("root")
        .setType("TimeIndexFiller")
        .setParams(TemplatableMap.fromValueMap(ImmutableMap.of(
                "component.monitoringGranularity", "P1D",
                "component.timestamp", "ts",
                "component.minTimeInference", "FROM_DATA",
                "component.maxTimeInference", "FROM_DATA"
            )
        ))
        .setInputs(ImmutableList.of(
            new InputBean().setTargetProperty("baseline")
                .setSourceProperty("currentDataFetcher")
                .setSourcePlanNode("currentOutput")
        ))
        .setOutputs(ImmutableList.of(
            new OutputBean().setOutputKey("filler").setOutputName("currentOutput")
        ));
    final Map<String, Object> properties = ImmutableMap.of();
    final DataFrame dataFrame = new DataFrame();
    // october 23 missing in the middle
    dataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(new Interval(0L, 1L, DateTimeZone.UTC)) // ignored with FROM_DATA
        .setPlanNode(planNodeBean)
        .setInputsMap(ImmutableMap.of("baseline", inputDataTable))
        .setProperties(properties);

    timeIndexFillerOperator.init(context);
    timeIndexFillerOperator.execute();
    assertThat(timeIndexFillerOperator.getOutputs().size()).isEqualTo(1);

    DataTable detectionPipelineResult = (DataTable) timeIndexFillerOperator.getOutputs()
        .get("currentOutput");
    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);
    assertThat(detectionPipelineResult.getDataFrame()).isEqualTo(expectedDataFrame);
  }

  @Test
  public void testTimeIndexFillerExecutionFillLeftRightInferBoundsFromDetectionTime()
      throws Exception {
    final TimeIndexFillerOperator timeIndexFillerOperator = new TimeIndexFillerOperator();

    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setName("root")
        .setType("TimeIndexFiller")
        .setParams(TemplatableMap.fromValueMap(ImmutableMap.of(
                "component.monitoringGranularity", "P1D",
                "component.timestamp", "ts",
                // use detection time to infer bounds
                "component.minTimeInference", "FROM_DETECTION_TIME_WITH_LOOKBACK",
                "component.maxTimeInference", "FROM_DETECTION_TIME",
                "component.lookback", "P1D"
            )
        ))
        .setInputs(ImmutableList.of(
            new InputBean().setTargetProperty("baseline")
                .setSourceProperty("currentDataFetcher")
                .setSourcePlanNode("currentOutput")
        ))
        .setOutputs(ImmutableList.of(
            new OutputBean().setOutputKey("filler").setOutputName("currentOutput")
        ));
    final Map<String, Object> properties = ImmutableMap.of();
    final DataFrame dataFrame = new DataFrame();
    // october 22 missing left - october 25 missing right
    dataFrame.addSeries("ts", OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    dataFrame.addSeries("met", METRIC_VALUE, METRIC_VALUE);
    final DataTable inputDataTable = SimpleDataTable.fromDataFrame(dataFrame);

    final Interval detectionInterval = new Interval(OCTOBER_23_MILLIS, OCTOBER_26_MILLIS,
        DateTimeZone.UTC);
    final OperatorContext context = new OperatorContext()
        .setDetectionInterval(detectionInterval)
        .setPlanNode(planNodeBean)
        .setInputsMap(ImmutableMap.of("baseline", inputDataTable))
        .setProperties(properties);

    timeIndexFillerOperator.init(context);
    assertThat(timeIndexFillerOperator.getDetectionInterval()).isEqualTo(detectionInterval);

    timeIndexFillerOperator.execute();

    assertThat(timeIndexFillerOperator.getOutputs().size()).isEqualTo(1);

    DataTable detectionPipelineResult = (DataTable) timeIndexFillerOperator.getOutputs()
        .get("currentOutput");
    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts",
        OCTOBER_22_MILLIS,
        OCTOBER_23_MILLIS,
        OCTOBER_24_MILLIS,
        OCTOBER_25_MILLIS);
    expectedDataFrame.addSeries("met", ZERO_FILLER, METRIC_VALUE, METRIC_VALUE, ZERO_FILLER);
    assertThat(detectionPipelineResult.getDataFrame()).isEqualTo(expectedDataFrame);
  }
}
