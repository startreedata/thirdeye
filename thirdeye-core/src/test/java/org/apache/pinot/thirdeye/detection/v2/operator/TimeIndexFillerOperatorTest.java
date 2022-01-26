package org.apache.pinot.thirdeye.detection.v2.operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
import org.testng.Assert;
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
        .setParams(ImmutableMap.of(
            "component.monitoringGranularity", "1_DAYS",
            "component.timestamp", "ts",
            "component.minTimeInference", "FROM_DATA",
            "component.maxTimeInference", "FROM_DATA"
            )
        )
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
        .setPlanNode(planNodeBean)
        .setInputsMap(ImmutableMap.of("baseline", inputDataTable))
        .setProperties(properties);

    timeIndexFillerOperator.init(context);
    timeIndexFillerOperator.execute();
    Assert.assertEquals(timeIndexFillerOperator.getOutputs().size(), 1);

    DataTable detectionPipelineResult = (DataTable) timeIndexFillerOperator.getOutputs().get("currentOutput");
    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS);
    expectedDataFrame.addSeries("met", METRIC_VALUE, ZERO_FILLER, METRIC_VALUE);
    Assert.assertEquals(detectionPipelineResult.getDataFrame(), expectedDataFrame);
  }

  @Test
  public void testTimeIndexFillerExecutionFillLeftRightInferBoundsFromDetectionTime() throws Exception {
    final TimeIndexFillerOperator timeIndexFillerOperator = new TimeIndexFillerOperator();
    final long startTime = OCTOBER_23_MILLIS;
    final long endTime = OCTOBER_26_MILLIS;

    final PlanNodeBean planNodeBean = new PlanNodeBean()
        .setName("root")
        .setType("TimeIndexFiller")
        .setParams(ImmutableMap.of(
            "component.monitoringGranularity", "1_DAYS",
            "component.timestamp", "ts",
            // use detection time to infer bounds
            "component.minTimeInference", "FROM_DETECTION_TIME_WITH_LOOKBACK",
            "component.maxTimeInference", "FROM_DETECTION_TIME",
            "component.lookback", "P1D"
            )
        )
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

    final OperatorContext context = new OperatorContext()
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setPlanNode(planNodeBean)
        .setInputsMap(ImmutableMap.of("baseline", inputDataTable))
        .setProperties(properties);

    timeIndexFillerOperator.init(context);
    Assert.assertEquals(timeIndexFillerOperator.getStartTime(), startTime);
    Assert.assertEquals(timeIndexFillerOperator.getEndTime(), endTime);

    timeIndexFillerOperator.execute();

    Assert.assertEquals(timeIndexFillerOperator.getOutputs().size(), 1);

    DataTable detectionPipelineResult = (DataTable) timeIndexFillerOperator.getOutputs().get("currentOutput");
    final DataFrame expectedDataFrame = new DataFrame();
    expectedDataFrame.addSeries("ts", OCTOBER_22_MILLIS, OCTOBER_23_MILLIS, OCTOBER_24_MILLIS, OCTOBER_25_MILLIS);
    expectedDataFrame.addSeries("met", ZERO_FILLER, METRIC_VALUE, METRIC_VALUE, ZERO_FILLER);
    Assert.assertEquals(detectionPipelineResult.getDataFrame(), expectedDataFrame);
  }

}
