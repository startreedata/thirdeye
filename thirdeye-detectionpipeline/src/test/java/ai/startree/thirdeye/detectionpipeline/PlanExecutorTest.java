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
package ai.startree.thirdeye.detectionpipeline;

import static ai.startree.thirdeye.detectionpipeline.operator.ForkJoinOperator.K_COMBINER;
import static ai.startree.thirdeye.detectionpipeline.operator.ForkJoinOperator.K_ENUMERATOR;
import static ai.startree.thirdeye.detectionpipeline.operator.ForkJoinOperator.K_ROOT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.operator.CombinerOperator;
import ai.startree.thirdeye.detectionpipeline.operator.CombinerResult;
import ai.startree.thirdeye.detectionpipeline.operator.EchoOperator;
import ai.startree.thirdeye.detectionpipeline.operator.EchoOperator.EchoResult;
import ai.startree.thirdeye.detectionpipeline.plan.CombinerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EchoPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EnumeratorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.ForkJoinPlanNode;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.detection.Enumerator;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PlanExecutorTest {

  private PlanExecutor planExecutor;
  private Enumerator enumerator;

  @BeforeMethod
  public void setUp() {
    final DetectionRegistry detectionRegistry = mock(DetectionRegistry.class);
    final DataSourceCache dataSourceCache = mock(DataSourceCache.class);
    final PostProcessorRegistry postProcessorRegistry = mock(PostProcessorRegistry.class);
    final EventManager eventManager = mock(EventManager.class);
    final DatasetConfigManager datasetConfigManager = mock(DatasetConfigManager.class);
    final PlanNodeFactory planNodeFactory = new PlanNodeFactory(
    );
    planExecutor = new PlanExecutor(planNodeFactory,
        dataSourceCache,
        detectionRegistry,
        postProcessorRegistry,
        eventManager,
        datasetConfigManager,
        new DetectionPipelineConfiguration());
    enumerator = mock(Enumerator.class);

    when(detectionRegistry.buildEnumerator("default")).thenReturn(enumerator);
  }

  @Test
  public void testExecutePlanNode() throws Exception {
    final EchoPlanNode node = new EchoPlanNode();
    final String echoInput = "test_input";
    final String nodeName = "root";
    node.init(new PlanNodeContext()
        .setName(nodeName)
        .setDetectionPipelineContext(new DetectionPipelineContext()
            .setDetectionInterval(new Interval(0L, 0L, DateTimeZone.UTC)))
        .setPlanNodeBean(new PlanNodeBean()
            .setInputs(Collections.emptyList())
            .setParams(TemplatableMap.ofValue(EchoOperator.DEFAULT_INPUT_KEY, echoInput))
        )
    );
    final HashMap<ContextKey, OperatorResult> resultMap = new HashMap<>();
    final HashMap<String, PlanNode> pipelinePlanNodes = new HashMap<>();
    PlanExecutor.executePlanNode(
        pipelinePlanNodes,
        node,
        resultMap
    );

    assertThat(resultMap.size()).isEqualTo(1);
    final ContextKey key = PlanExecutor.key(nodeName, EchoOperator.DEFAULT_OUTPUT_KEY);
    final OperatorResult result = resultMap.get(key);
    assertThat(result).isNotNull();

    final EchoResult echoResult = (EchoResult) result;
    assertThat(echoResult.text()).isEqualTo(echoInput);
  }

  @Test
  public void testExecuteSingleForkJoin() throws Exception {
    final PlanNodeBean echoNode = new PlanNodeBean()
        .setName("echo")
        .setType(EchoPlanNode.TYPE)
        .setParams(TemplatableMap.ofValue(
            EchoOperator.DEFAULT_INPUT_KEY, "${key}"
        ));

    final List<Map<String, Map<String, Object>>> items = List.of(
        Map.of("params", Map.of("key", 1)),
        Map.of("params", Map.of("key", 2)),
        Map.of("params", Map.of("key", 3))
    );
    final PlanNodeBean enumeratorNode = new PlanNodeBean()
        .setName("enumerator")
        .setType(EnumeratorPlanNode.TYPE)
        .setParams(TemplatableMap.ofValue("items", items));

    when(enumerator.enumerate(any())).thenReturn(items
        .stream()
        .map(m -> new EnumerationItemDTO().setParams(m.get("params")))
        .collect(Collectors.toList())
    );

    final PlanNodeBean combinerNode = new PlanNodeBean()
        .setName("combiner")
        .setType(CombinerPlanNode.TYPE);

    final PlanNodeBean forkJoinNode = new PlanNodeBean()
        .setName("root")
        .setType(ForkJoinPlanNode.TYPE)
        .setParams(TemplatableMap.fromValueMap(ImmutableMap.of(
            K_ENUMERATOR, enumeratorNode.getName(),
            K_ROOT, echoNode.getName(),
            K_COMBINER, combinerNode.getName()
        )));

    final List<PlanNodeBean> planNodeBeans = Arrays.asList(
        echoNode,
        enumeratorNode,
        combinerNode,
        forkJoinNode
    );

    final Map<ContextKey, OperatorResult> resultMap = new HashMap<>();
    final Interval detectionInterval = new Interval(
        0L,
        System.currentTimeMillis(),
        DateTimeZone.UTC);
    final DetectionPipelineContext runTimeContext = new DetectionPipelineContext()
        .setApplicationContext(planExecutor.applicationContext)
        .setDetectionInterval(detectionInterval);
    final Map<String, PlanNode> pipelinePlanNodes = planExecutor.buildPlanNodeMap(planNodeBeans,
        runTimeContext);
    PlanExecutor.executePlanNode(
        pipelinePlanNodes,
        pipelinePlanNodes.get("root"),
        resultMap
    );

    assertThat(resultMap.size()).isEqualTo(1);

    final OperatorResult detectionPipelineResult = resultMap.get(PlanExecutor.key("root",
        CombinerOperator.DEFAULT_OUTPUT_KEY));

    assertThat(detectionPipelineResult).isInstanceOf(CombinerResult.class);

    final CombinerResult combinerResult = (CombinerResult) detectionPipelineResult;
    final Map<String, OperatorResult> outputMap = combinerResult.getResults();

    assertThat(outputMap).isNotNull();

    assertThat(outputMap.values().size()).isEqualTo(3);
  }
}
