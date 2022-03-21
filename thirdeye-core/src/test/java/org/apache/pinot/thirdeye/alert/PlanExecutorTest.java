package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.detection.v2.operator.ForkJoinOperator.K_COMBINER;
import static org.apache.pinot.thirdeye.detection.v2.operator.ForkJoinOperator.K_ENUMERATOR;
import static org.apache.pinot.thirdeye.detection.v2.operator.ForkJoinOperator.K_ROOT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.detection.v2.operator.CombinerOperator;
import org.apache.pinot.thirdeye.detection.v2.operator.CombinerOperator.CombinerResult;
import org.apache.pinot.thirdeye.detection.v2.operator.EchoOperator;
import org.apache.pinot.thirdeye.detection.v2.operator.EchoOperator.EchoResult;
import org.apache.pinot.thirdeye.detection.v2.plan.CombinerPlanNode;
import org.apache.pinot.thirdeye.detection.v2.plan.EchoPlanNode;
import org.apache.pinot.thirdeye.detection.v2.plan.EnumeratorPlanNode;
import org.apache.pinot.thirdeye.detection.v2.plan.ForkJoinPlanNode;
import org.apache.pinot.thirdeye.detection.v2.plan.PlanNodeFactory;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PlanExecutorTest {

  private PlanExecutor planExecutor;

  @BeforeMethod
  public void setUp() {
    final PlanNodeFactory planNodeFactory = new PlanNodeFactory(mock(DataSourceCache.class));
    planExecutor = new PlanExecutor(planNodeFactory);
  }

  @Test
  public void testExecutePlanNode() throws Exception {
    final EchoPlanNode node = new EchoPlanNode();
    final String echoInput = "test_input";
    final String nodeName = "root";
    node.init(new PlanNodeContext()
        .setName(nodeName)
        .setPlanNodeBean(new PlanNodeBean()
            .setInputs(Collections.emptyList())
            .setParams(ImmutableMap.of(EchoOperator.DEFAULT_INPUT_KEY, echoInput))
        )
    );
    final HashMap<ContextKey, DetectionPipelineResult> context = new HashMap<>();
    final HashMap<String, PlanNode> pipelinePlanNodes = new HashMap<>();
    PlanExecutor.executePlanNode(
        pipelinePlanNodes,
        context,
        node
    );

    assertThat(context.size()).isEqualTo(1);
    final ContextKey key = PlanExecutor.key(nodeName, EchoOperator.DEFAULT_OUTPUT_KEY);
    final DetectionPipelineResult result = context.get(key);
    assertThat(result).isNotNull();

    final EchoResult echoResult = (EchoResult) result;
    assertThat(echoResult.text()).isEqualTo(echoInput);
  }

  @Test
  public void testExecuteSingleForkJoin() throws Exception {
    final PlanNodeBean echoNode = new PlanNodeBean()
        .setName("echo")
        .setType(EchoPlanNode.TYPE)
        .setParams(ImmutableMap.of(
            EchoOperator.DEFAULT_INPUT_KEY, "defaultInput"
        ));

    final PlanNodeBean enumeratorNode = new PlanNodeBean()
        .setName("enumerator")
        .setType(EnumeratorPlanNode.TYPE);

    final PlanNodeBean combinerNode = new PlanNodeBean()
        .setName("combiner")
        .setType(CombinerPlanNode.TYPE);

    final PlanNodeBean forkJoinNode = new PlanNodeBean()
        .setName("root")
        .setType(ForkJoinPlanNode.TYPE)
        .setParams(ImmutableMap.of(
            K_ENUMERATOR, enumeratorNode.getName(),
            K_ROOT, echoNode.getName(),
            K_COMBINER, combinerNode.getName()
        ));

    final List<PlanNodeBean> planNodeBeans = Arrays.asList(
        echoNode,
        enumeratorNode,
        combinerNode,
        forkJoinNode
    );

    final Map<ContextKey, DetectionPipelineResult> context = new HashMap<>();
    final Map<String, PlanNode> pipelinePlanNodes = planExecutor.buildPlanNodeMap(planNodeBeans,
        0L, System.currentTimeMillis());
    PlanExecutor.executePlanNode(
        pipelinePlanNodes,
        context,
        pipelinePlanNodes.get("root")
    );

    assertThat(context.size()).isEqualTo(1);

    final DetectionPipelineResult detectionPipelineResult = context.get(PlanExecutor.key("root",
        CombinerOperator.DEFAULT_OUTPUT_KEY));

    assertThat(detectionPipelineResult).isInstanceOf(CombinerResult.class);

    final CombinerResult combinerResult = (CombinerResult) detectionPipelineResult;
    final Map<String, DetectionPipelineResult> outputMap = combinerResult.getResults();

    assertThat(outputMap).isNotNull();
  }
}
