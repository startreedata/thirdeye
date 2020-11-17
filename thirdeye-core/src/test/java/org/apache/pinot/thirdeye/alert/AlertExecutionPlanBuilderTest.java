package org.apache.pinot.thirdeye.alert;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertNodeApi;
import org.apache.pinot.thirdeye.api.DatasetApi;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datalayer.pojo.AlertNodeType;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.algorithm.DimensionWrapper;
import org.apache.pinot.thirdeye.detection.wrapper.AnomalyDetectorWrapper;
import org.apache.pinot.thirdeye.detection.wrapper.BaselineFillingMergeWrapper;
import org.apache.pinot.thirdeye.detection.wrapper.ChildKeepingMergeWrapper;
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
        .setMetric(new MetricApi()
            .setName("views")
            .setDataset(new DatasetApi()
                .setName("pageviews")));
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
  }

  @Test
  public void testBuildDetectionPropertiesEmpty() {
    final AlertApi alertApi = new AlertApi();
    final Map<String, Object> map = instance.process(alertApi).getProperties();

    final Map<String, Object> expected = new HashMap<>();
    assertThat(map).isEqualTo(expected);
  }

  @Test
  public void testBuildDetectionPropertiesSingleDetection() {
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
}
