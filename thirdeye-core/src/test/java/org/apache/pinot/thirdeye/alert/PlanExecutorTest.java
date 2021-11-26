package org.apache.pinot.thirdeye.alert;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import org.apache.pinot.thirdeye.detection.v2.operator.EchoOperator;
import org.apache.pinot.thirdeye.detection.v2.operator.EchoOperator.EchoResult;
import org.apache.pinot.thirdeye.detection.v2.plan.EchoPlanNode;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;
import org.testng.annotations.Test;

public class PlanExecutorTest {

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
}
