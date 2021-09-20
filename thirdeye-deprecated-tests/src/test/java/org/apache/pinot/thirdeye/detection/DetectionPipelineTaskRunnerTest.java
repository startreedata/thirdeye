/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection;

import static org.mockito.Mockito.mock;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.alert.AlertTemplateRenderer;
import org.apache.pinot.thirdeye.alert.PlanExecutor;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.task.DetectionPipelineTaskInfo;
import org.apache.pinot.thirdeye.task.TaskContext;
import org.apache.pinot.thirdeye.task.runner.AnomalyMerger;
import org.apache.pinot.thirdeye.task.runner.DetectionPipelineRunner;
import org.apache.pinot.thirdeye.task.runner.DetectionPipelineTaskRunner;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DetectionPipelineTaskRunnerTest {

  private List<MockPipeline> runs;
  private List<MockPipelineOutput> outputs;

  private DetectionPipelineTaskRunner runner;
  private DetectionPipelineTaskInfo info;
  private TaskContext context;

  private TestDbEnv testDAOProvider;
  private DataProvider provider;
  private Map<String, Object> properties;

  private long detectorId;

  @BeforeMethod
  public void beforeMethod() {
    runs = new ArrayList<>();

    outputs = new ArrayList<>();

    testDAOProvider = new TestDbEnv();
    final Injector injector = testDAOProvider.getInjector();
    final AlertManager detectionDAO = injector.getInstance(AlertManager.class);
    final EvaluationManager evaluationDAO = injector.getInstance(EvaluationManager.class);
    provider = new MockDataProvider();

    properties = new HashMap<>();
    properties.put("metricUrn", "thirdeye:metric:1");
    properties.put("className", "myClassName");

    final AlertDTO detector = new AlertDTO();
    detector.setProperties(properties);
    detector.setName("myName");
    detector.setDescription("myDescription");
    detector.setCron("myCron");
    detectorId = detectionDAO.save(detector);

    final DetectionPipelineFactory detectionPipelineFactory = new MockPipelineLoader(runs,
        outputs,
        provider);
    final PlanExecutor planExecutor = injector.getInstance(PlanExecutor.class);
    final DetectionPipelineRunner detectionPipelineRunner = new DetectionPipelineRunner(
        detectionPipelineFactory,
        planExecutor,
        mock(AlertTemplateRenderer.class));

    runner = new DetectionPipelineTaskRunner(
        detectionDAO,
        evaluationDAO,
        new ModelRetuneFlow(provider, new DetectionRegistry()),
        injector.getInstance(AnomalySubscriptionGroupNotificationManager.class),
        new MetricRegistry(),
        detectionPipelineRunner,
        mock(AnomalyMerger.class));

    info = new DetectionPipelineTaskInfo();
    info.setConfigId(detectorId);
    info.setStart(1250);
    info.setEnd(1500);

    context = new TaskContext();
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethod() {
    testDAOProvider.cleanup();
  }

  @Test
  public void testTaskRunnerLoading() throws Exception {
    runner.execute(info, context);

    Assert.assertEquals(runs.size(), 1);
    Assert.assertEquals(runs.get(0).getStartTime(), 1250);
    Assert.assertEquals(runs.get(0).getEndTime(), 1500);
    Assert.assertEquals(runs.get(0).getConfig().getName(), "myName");
    Assert.assertEquals(runs.get(0).getConfig().getDescription(), "myDescription");
    Assert
        .assertEquals(runs.get(0).getConfig().getProperties().get("className"), "myClassName");
    Assert.assertEquals(runs.get(0).getConfig().getCron(), "myCron");
  }

  @Test
  public void testTaskRunnerPersistenceFailTimestamp() throws Exception {
    final MergedAnomalyResultDTO anomaly = DetectionTestUtils
        .makeAnomaly(detectorId, 1300, 1400, null, null,
            Collections.singletonMap("myKey", "myValue"));

    outputs.add(new MockPipelineOutput(Collections.singletonList(anomaly), -1));

    runner.execute(info, context);

    Assert.assertNull(anomaly.getId());
  }
}
