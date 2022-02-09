/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.detection.annotation.registry.DetectionRegistry;
import ai.startree.thirdeye.detection.components.MockModelEvaluator;
import ai.startree.thirdeye.detection.components.MockTunableDetector;
import ai.startree.thirdeye.detection.spec.MockModelEvaluatorSpec;
import ai.startree.thirdeye.detection.spi.components.ModelEvaluator;
import ai.startree.thirdeye.detection.spi.model.ModelStatus;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.joda.time.Instant;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultModelMaintenanceFlowTest {

  private static final String METRIC_NAME = "test_metric";
  private static final String DATASET_NAME = "test_dataset";

  private ModelMaintenanceFlow maintenanceFlow;
  private DataProvider provider;
  private InputDataFetcher dataFetcher;
  private long configId;

  @BeforeMethod
  public void setUp() {
    MetricConfigDTO metric = new MetricConfigDTO();
    metric.setId(101L);
    metric.setName(METRIC_NAME);
    metric.setDataset(DATASET_NAME);
    DatasetConfigDTO dataset = new DatasetConfigDTO();
    dataset.setId(102L);
    dataset.setDataset(DATASET_NAME);
    this.provider = new MockDataProvider().setMetrics(Collections.singletonList(metric))
        .setDatasets(Collections.singletonList(dataset));
    this.configId = 100L;
    this.dataFetcher = new DefaultInputDataFetcher(this.provider, this.configId);
    this.maintenanceFlow = new ModelRetuneFlow(this.provider, new DetectionRegistry());
  }

  @Test
  public void testMaintainNotTunable() {
    AlertDTO configDTO = new AlertDTO();
    configDTO.setId(this.configId);
    ModelEvaluator evaluator = new MockModelEvaluator();
    MockModelEvaluatorSpec spec = new MockModelEvaluatorSpec();
    spec.setMockModelStatus(ModelStatus.GOOD);
    configDTO.setComponents(ImmutableMap.of("evaluator_1", evaluator));
    AlertDTO maintainedConfig = this.maintenanceFlow.maintain(configDTO, Instant.now());
    Assert.assertEquals(configDTO, maintainedConfig);
  }

  @Test
  public void testMaintainTunableGood() {
    AlertDTO configDTO = new AlertDTO();
    configDTO.setId(this.configId);
    ModelEvaluator evaluator = new MockModelEvaluator();
    MockModelEvaluatorSpec spec = new MockModelEvaluatorSpec();
    spec.setMockModelStatus(ModelStatus.GOOD);
    evaluator.init(spec, this.dataFetcher);
    MockTunableDetector detector = new MockTunableDetector();
    configDTO.setComponents(ImmutableMap.of("evaluator_1", evaluator, "detector", detector));
    AlertDTO maintainedConfig = this.maintenanceFlow.maintain(configDTO, Instant.now());
    Assert.assertEquals(configDTO, maintainedConfig);
  }

  @Test
  public void testMaintainTunableBad() {
    DetectionRegistry.registerTunableComponent(MockTunableDetector.class.getName(), "MOCK_TUNABLE",
        "MOCK_TUNABLE");
    AlertDTO configDTO = new AlertDTO();
    configDTO.setId(this.configId);
    configDTO.setYaml(String.format("metric: %s\ndataset: %s\n", METRIC_NAME, DATASET_NAME));
    ModelEvaluator evaluator = new MockModelEvaluator();
    MockModelEvaluatorSpec spec = new MockModelEvaluatorSpec();
    spec.setMockModelStatus(ModelStatus.BAD);
    evaluator.init(spec, this.dataFetcher);
    MockTunableDetector tunableDetector = new MockTunableDetector();
    configDTO.setComponents(ImmutableMap.of("evaluator_1", evaluator, "detector", tunableDetector));
    configDTO.setLastTuningTimestamp(1559175301000L);
    configDTO.setComponentSpecs(ImmutableMap.of("detector:MOCK_TUNABLE",
        ImmutableMap.of("className", MockTunableDetector.class.getName())));
    Instant maintainTimestamp = Instant.now();
    AlertDTO maintainedConfig = this.maintenanceFlow.maintain(configDTO, maintainTimestamp);
    Assert.assertEquals(maintainedConfig.getLastTuningTimestamp(), maintainTimestamp.getMillis());
  }
}
