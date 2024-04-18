/*
 * Copyright 2024 StarTree Inc
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datalayer.core.EnumerationItemMaintainer;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.operator.EchoOperator;
import ai.startree.thirdeye.detectionpipeline.operator.EchoOperator.EchoResult;
import ai.startree.thirdeye.detectionpipeline.plan.EchoPlanNode;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.detection.Enumerator;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.Collections;
import java.util.HashMap;
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
    final DataSourceManager dataSourceDao = mock(DataSourceManager.class);
    final PlanNodeFactory planNodeFactory = new PlanNodeFactory(
    );
    final EnumerationItemManager enumerationItemManager = mock(EnumerationItemManager.class);
    when(enumerationItemManager.save(any())).thenAnswer(e -> {
      ((EnumerationItemDTO) e.getArguments()[0]).setId(1L);
      return 1L;
    });
    planExecutor = new PlanExecutor(planNodeFactory,
        dataSourceCache,
        detectionRegistry,
        postProcessorRegistry,
        eventManager,
        dataSourceDao, 
        datasetConfigManager,
        new DetectionPipelineConfiguration(),
        mock(EnumerationItemMaintainer.class));
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
}
