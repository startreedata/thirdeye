package org.apache.pinot.thirdeye.alert;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.algorithm.DimensionWrapper;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.detection.components.GenericAnomalyDetectorFactory;
import org.apache.pinot.thirdeye.detection.components.detectors.PercentageChangeRuleDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.PercentageChangeRuleDetectorSpec;
import org.apache.pinot.thirdeye.detection.wrapper.AnomalyDetectorWrapper;
import org.apache.pinot.thirdeye.detection.wrapper.AnomalyFilterWrapper;
import org.apache.pinot.thirdeye.detection.wrapper.BaselineFillingMergeWrapper;
import org.apache.pinot.thirdeye.detection.wrapper.ChildKeepingMergeWrapper;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertNodeApi;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertNodeType;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AlertExecutionPlanBuilderTest {

  private AlertExecutionPlanBuilder instance;

  private static AlertNodeApi sampleDetectionNode() {
    final HashMap<String, Object> params = new HashMap<>();
    params.put("offset", "w01w");
    params.put("percentageChange", "0.2");

    return new AlertNodeApi()
        .setName("d1")
        .setType(AlertNodeType.DETECTION)
        .setSubType("PERCENTAGE_RULE")
        .setParams(params)
        .setParams(ImmutableMap.<String, Object>builder()
            .put("offset", "w01w")
            .put("percentageChange", "0.2")
            .build())
        .setMetric(new MetricApi()
            .setName("views")
            .setDataset(new DatasetApi()
                .setName("pageviews")));
  }

  private static AlertNodeApi sampleFilterNode() {
    final HashMap<String, Object> params = new HashMap<>();
    params.put("pattern", "UP_OR_DOWN");
    params.put("threshold", "0.3");

    return new AlertNodeApi()
        .setName("f1")
        .setType(AlertNodeType.FILTER)
        .setSubType("PERCENTAGE_CHANGE_FILTER")
        .setParams(ImmutableMap.<String, Object>builder()
            .put("pattern", "UP_OR_DOWN")
            .put("threshold", "0.3")
            .build())
        ;
  }

  private static DatasetConfigDTO mockDatasetConfigDTO() {
    final DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setId(1L);
    datasetConfigDTO.setDataset("pageviews");
    datasetConfigDTO.setTimeDuration(1);
    datasetConfigDTO.setTimeUnit(TimeUnit.DAYS);

    return datasetConfigDTO;
  }

  private static MetricConfigDTO mockMetricConfigDTO(String datasetName) {
    final MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    metricConfigDTO.setId(11L);
    metricConfigDTO.setName("views");
    metricConfigDTO.setDataset(datasetName);
    return metricConfigDTO;
  }

  @BeforeMethod
  public void setUp() {
    final DataProvider dataProvider = mock(DataProvider.class);
    instance = new AlertExecutionPlanBuilder(dataProvider);

    final DatasetConfigDTO datasetConfigDTO = mockDatasetConfigDTO();
    final MetricConfigDTO metricConfigDTO = mockMetricConfigDTO(datasetConfigDTO.getDisplayName());

    when(dataProvider.fetchMetric("views", datasetConfigDTO.getName()))
        .thenReturn(metricConfigDTO);

    when(dataProvider.fetchDatasetByDisplayName(datasetConfigDTO.getName()))
        .thenReturn(singletonList(datasetConfigDTO));

    new DetectionRegistry().addAnomalyDetectorFactory(new GenericAnomalyDetectorFactory<>(
        "PERCENTAGE_RULE",
        PercentageChangeRuleDetectorSpec.class,
        PercentageChangeRuleDetector.class
    ));
  }

  @Test
  public void testBuildDetectionPropertiesEmpty() {
    final AlertApi alertApi = new AlertApi();
    final Map<String, Object> map = instance.process(alertApi).getProperties();

    final Map<String, Object> expected = new HashMap<>();
    assertThat(map).isEqualTo(expected);
  }

  @Test
  public void testBuildDetectionProperties_1D() {
    final AlertNodeApi alertNodeApi = sampleDetectionNode();
    final AlertApi alertApi = new AlertApi()
        .setName("a1")
        .setNodes(ImmutableMap.of(alertNodeApi.getName(), alertNodeApi));

    final Map<String, Object> map = instance.process(alertApi).getProperties();

    final Map<String, Object> expected = ImmutableMap.<String, Object>builder()
        .put("className", ChildKeepingMergeWrapper.class.getName())
        .put("nested", singletonList(ImmutableMap.<String, Object>builder()
            .put("nestedMetricUrns", singletonList("thirdeye:metric:11"))
            .put("className", DimensionWrapper.class.getName())
            .put("nested", singletonList(ImmutableMap.<String, Object>builder()
                .put("className", BaselineFillingMergeWrapper.class.getName())
                .put("baselineValueProvider", "$d1:PERCENTAGE_RULE")
                .put("detector", "$d1:PERCENTAGE_RULE")
                .put("nested", singletonList(ImmutableMap.<String, Object>builder()
                    .put("className", AnomalyDetectorWrapper.class.getName())
                    .put("bucketPeriod", "P1D")
                    .put("subEntityName", "a1")
                    .build()
                ))
                .build()
            ))
            .build()
        ))
        .build();

    assertThat(map).isEqualTo(expected);
  }

  @Test(enabled = false)
  public void testBuildDetectionProperties_1D_1F() {
    final AlertNodeApi detectionNode = sampleDetectionNode();
    final AlertNodeApi filterNode = sampleFilterNode();
    final AlertApi alertApi = new AlertApi()
        .setName("a1")
        .setNodes(ImmutableMap.of(
            detectionNode.getName(), detectionNode,
            filterNode.getName(), filterNode.setDependsOn(singletonList(detectionNode.getName()))
        ));

    final Map<String, Object> map = instance.process(alertApi).getProperties();

    final Map<String, Object> expected = ImmutableMap.<String, Object>builder()
        .put("className", ChildKeepingMergeWrapper.class.getName())
        .put("nested", singletonList(ImmutableMap.<String, Object>builder()
            .put("nestedMetricUrns", singletonList("thirdeye:metric:11"))
            .put("className", DimensionWrapper.class.getName())
            .put("nested", singletonList(ImmutableMap.<String, Object>builder()
                .put("filter", "$f1:PERCENTAGE_CHANGE_FILTER")
                .put("className", AnomalyFilterWrapper.class.getName())
                .put("nested", singletonList(ImmutableMap.<String, Object>builder()
                    .put("className", BaselineFillingMergeWrapper.class.getName())
                    .put("baselineValueProvider", "$d1:PERCENTAGE_RULE")
                    .put("detector", "$d1:PERCENTAGE_RULE")
                    .put("nested", singletonList(ImmutableMap.<String, Object>builder()
                        .put("className", AnomalyDetectorWrapper.class.getName())
                        .put("bucketPeriod", "P1D")
                        .put("subEntityName", "a1")
                        .build()
                    ))
                    .build()
                ))
                .build()
            ))
            .build()
        ))
        .build();

    assertThat(map).isEqualTo(expected);
  }

  @Test(enabled = false)
  public void testBuildDetectionProperties_2D_1F() {
    final AlertNodeApi d1 = sampleDetectionNode();
    final AlertNodeApi d2 = sampleDetectionNode().setName("d2");
    final AlertNodeApi filterNode = sampleFilterNode();
    final AlertApi alertApi = new AlertApi()
        .setName("a1")
        .setNodes(ImmutableMap.of(
            d1.getName(), d1,
            d2.getName(), d2,
            filterNode.getName(),
            filterNode.setDependsOn(ImmutableList.of(d1.getName(), d2.getName()))
        ));

    final Map<String, Object> map = instance.process(alertApi).getProperties();

    final Map<String, Object> expected = ImmutableMap.<String, Object>builder()
        .put("className", ChildKeepingMergeWrapper.class.getName())
        .put("nested", singletonList(ImmutableMap.<String, Object>builder()
            .put("nestedMetricUrns", singletonList("thirdeye:metric:11"))
            .put("className", DimensionWrapper.class.getName())
            .put("nested", singletonList(ImmutableMap.<String, Object>builder()
                .put("filter", "$f1:PERCENTAGE_CHANGE_FILTER")
                .put("className", AnomalyFilterWrapper.class.getName())
                .put("nested", ImmutableList.of(
                    ImmutableMap.<String, Object>builder()
                        .put("className", BaselineFillingMergeWrapper.class.getName())
                        .put("baselineValueProvider", "$d1:PERCENTAGE_RULE")
                        .put("detector", "$d1:PERCENTAGE_RULE")
                        .put("nested", singletonList(ImmutableMap.<String, Object>builder()
                            .put("className", AnomalyDetectorWrapper.class.getName())
                            .put("bucketPeriod", "P1D")
                            .put("subEntityName", "a1")
                            .build()
                        ))
                        .build(),
                    ImmutableMap.<String, Object>builder()
                        .put("className", BaselineFillingMergeWrapper.class.getName())
                        .put("baselineValueProvider", "$d2:PERCENTAGE_RULE")
                        .put("detector", "$d2:PERCENTAGE_RULE")
                        .put("nested", singletonList(ImmutableMap.<String, Object>builder()
                            .put("className", AnomalyDetectorWrapper.class.getName())
                            .put("bucketPeriod", "P1D")
                            .put("subEntityName", "a1")
                            .build()
                        ))
                        .build()
                ))
                .build()
            ))
            .build()
        ))
        .build();

    assertThat(map).isEqualTo(expected);
  }
}
