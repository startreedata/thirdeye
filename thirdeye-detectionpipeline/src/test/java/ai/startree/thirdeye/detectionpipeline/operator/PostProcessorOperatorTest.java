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

import static ai.startree.thirdeye.detectionpipeline.operator.DetectionPipelineOperator.getComponentSpec;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.ApplicationContext;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineConfiguration;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.PostProcessorRegistry;
import ai.startree.thirdeye.detectionpipeline.operator.AnomalyDetectorOperatorResult.Builder;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import ai.startree.thirdeye.spi.detection.PostProcessorSpec;
import ai.startree.thirdeye.spi.detection.postprocessing.AnomalyPostProcessor;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PostProcessorOperatorTest {

  private static final String TEST_POST_PROCESSOR_NAME = "TestPostProcessor";
  private static final String TEST_POST_PROCESSOR_LABEL_NAME = "testLabel";
  private static final TemplatableMap<String, Object> TEST_POST_PROCESSOR_CONFIG = TemplatableMap.fromValueMap(
      ImmutableMap.of("type",
          TEST_POST_PROCESSOR_NAME,
          "component.labelName",
          TEST_POST_PROCESSOR_LABEL_NAME));
  private static final String METADATA_TEST_VALUE = "testValue";
  private static final String METADATA_TEST_KEY = "testKey";
  private static final String NODE_BEAN_NAME = "NodeBeanName";
  private static final Interval NOT_USED_DETECTION_INTERVAL = new Interval(0L,
      1L,
      DateTimeZone.UTC);

  private PlanNodeContext planNodeContext;

  @BeforeClass
  public void setUp() {
    final PostProcessorRegistry postProcessorRegistry = mock(PostProcessorRegistry.class);
    when(postProcessorRegistry.build(anyString(), anyMap(), any())).thenAnswer(
        i -> {
          final Map<String, Object> nodeParams = i.getArgument(1);
          final Map<String, Object> componentSpec = getComponentSpec(nodeParams);
          return new TestPostProcessor(componentSpec);
        }
    );

    planNodeContext = new PlanNodeContext().setDetectionPipelineContext(
        new DetectionPipelineContext().setApplicationContext(
            new ApplicationContext(
                mock(DataSourceCache.class),
                mock(DetectionRegistry.class),
                postProcessorRegistry,
                mock(EventManager.class),
                mock(DatasetConfigManager.class),
                mock(ExecutorService.class),
                mock(EnumerationItemManager.class),
                new DetectionPipelineConfiguration()
        )));
  }

  @Test
  public void testPostProcessorOperatorWithAnomalyDetectionResults() throws Exception {
    final PlanNodeBean planNodeBean = new PlanNodeBean().setName(NODE_BEAN_NAME)
        .setParams(TEST_POST_PROCESSOR_CONFIG);
    final Map<String, OperatorResult> inputsMap = Map.of("detectionResult0",
        detectionResWith0Anomaly(),
        "detectionResult1",
        detectionResWith1Anomaly(),
        "detectionResult2",
        detectionResWith2Anomalies());
    final OperatorContext context = new OperatorContext().setDetectionInterval(
            NOT_USED_DETECTION_INTERVAL)
        .setPlanNode(
            planNodeBean.setInputs(List.of(new InputBean().setSourcePlanNode("DetectorNode"))))
        .setInputsMap(inputsMap)
        .setPlanNodeContext(planNodeContext);
    final PostProcessorOperator operator = new PostProcessorOperator();
    operator.init(context);
    operator.execute();
    final Map<String, OperatorResult> outputs = operator.getOutputs();
    assertThat(outputs.size()).isEqualTo(3);
    for (final String inputName : List.of("detectionResult0",
        "detectionResult1",
        "detectionResult2")) {
      assertThat(outputs.containsKey(inputName)).isTrue();
      final OperatorResult r = outputs.get(inputName);
      assertLabelsAreCorrect(r);
    }
  }

  @Test
  public void testPostProcessorOperatorWithCombinerResult() throws Exception {
    final PlanNodeBean planNodeBean = new PlanNodeBean().setName(NODE_BEAN_NAME)
        .setParams(TEST_POST_PROCESSOR_CONFIG);
    final Map<String, OperatorResult> detectionMap = Map.of("detectionResult1",
        detectionResWith1Anomaly(),
        "detectionResult2",
        detectionResWith2Anomalies());
    final CombinerResult combinerResult = new CombinerResult(detectionMap);
    final Map<String, OperatorResult> inputsMap = Map.of("combinerResult1", combinerResult);
    final OperatorContext context = new OperatorContext().setDetectionInterval(
            NOT_USED_DETECTION_INTERVAL)
        .setPlanNode(
            planNodeBean.setInputs(List.of(new InputBean().setSourcePlanNode("DetectorNode"))))
        .setInputsMap(inputsMap)
        .setPlanNodeContext(planNodeContext);
    final PostProcessorOperator operator = new PostProcessorOperator();
    operator.init(context);
    operator.execute();
    final Map<String, OperatorResult> outputs = operator.getOutputs();
    assertThat(outputs.size()).isEqualTo(1);
    assertThat(outputs.containsKey("combinerResult1")).isTrue();
    final OperatorResult result = outputs.get("combinerResult1");
    assertThat(result.getAnomalies().size()).isEqualTo(3);
    assertLabelsAreCorrect(result);
  }

  private void assertLabelsAreCorrect(final OperatorResult r) {
    for (final MergedAnomalyResultDTO anomaly : r.getAnomalies()) {
      assertThat(anomaly.getAnomalyLabels().size()).isEqualTo(1);
      final AnomalyLabelDTO label = anomaly.getAnomalyLabels().get(0);
      assertThat(label.getName()).isEqualTo(TEST_POST_PROCESSOR_LABEL_NAME);
      assertThat(label.getSourceNodeName()).isEqualTo(NODE_BEAN_NAME);
      assertThat(label.getSourcePostProcessor()).isEqualTo(TEST_POST_PROCESSOR_NAME);
      assertThat(label.getMetadata()).isEqualTo(Map.of(METADATA_TEST_KEY, METADATA_TEST_VALUE));
      assertThat(label.isIgnore()).isTrue();
    }
  }

  /**
   * Create a detection result from a list of anomalies and time series
   *
   * @param anomalies the list of anomalies generated
   * @return the detection result contains the list of anomalies and the time series
   */
  private static AnomalyDetectorOperatorResult from(final List<MergedAnomalyResultDTO> anomalies) {
    // timeseries is not used by the TestPostProcessor
    return new Builder().setAnomalies(anomalies).setTimeseries(null).build();
  }

  private static AnomalyDetectorOperatorResult detectionResWith1Anomaly() {
    return from(List.of(new MergedAnomalyResultDTO()));
  }

  private static AnomalyDetectorOperatorResult detectionResWith2Anomalies() {
    return from(List.of(new MergedAnomalyResultDTO(), new MergedAnomalyResultDTO()));
  }

  private static AnomalyDetectorOperatorResult detectionResWith0Anomaly() {
    return from(List.of());
  }

  private static class TestPostProcessorSpec extends PostProcessorSpec {

    private String labelName;

    public String getLabelName() {
      return labelName;
    }

    public TestPostProcessorSpec setLabelName(final String labelName) {
      this.labelName = labelName;
      return this;
    }
  }

  private static class TestPostProcessor implements AnomalyPostProcessor {

    private String labelName;

    public TestPostProcessor(final Map<String, Object> params) {
      final TestPostProcessorSpec spec = new ObjectMapper().convertValue(params,
          TestPostProcessorSpec.class);
      this.labelName = spec.getLabelName();
    }

    @Override
    public String name() {
      return TEST_POST_PROCESSOR_NAME;
    }

    @Override
    public Map<String, OperatorResult> postProcess(final Interval detectionInterval,
        final Map<String, OperatorResult> resultMap) throws Exception {
      for (final OperatorResult r : resultMap.values()) {
        if (r instanceof AnomalyDetectorOperatorResult) {
          final AnomalyDetectorOperatorResult detectionResult = (AnomalyDetectorOperatorResult) r;
          for (final MergedAnomalyResultDTO anomaly : detectionResult.getAnomalies()) {
            // override existing labels - don't do this in real implementation - ok for tests
            final AnomalyLabelDTO anomalyLabel = new AnomalyLabelDTO().setIgnore(true)
                .setName(labelName)
                .setMetadata(Map.of(METADATA_TEST_KEY, METADATA_TEST_VALUE));
            anomaly.setAnomalyLabels(List.of(anomalyLabel));
          }
        }
      }
      return resultMap;
    }
  }
}
