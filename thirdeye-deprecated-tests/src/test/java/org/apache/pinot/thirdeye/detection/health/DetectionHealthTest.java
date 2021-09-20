/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.apache.pinot.thirdeye.detection.health;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EvaluationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.spi.detection.health.DetectionHealth;
import org.apache.pinot.thirdeye.spi.detection.health.HealthStatus;
import org.apache.pinot.thirdeye.spi.task.TaskStatus;
import org.apache.pinot.thirdeye.spi.task.TaskType;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DetectionHealthTest {

  private TestDbEnv testDAOProvider;
  private MergedAnomalyResultManager anomalyDAO;
  private TaskManager taskDAO;
  private EvaluationManager evaluationDAO;
  private long configId;

  @BeforeMethod
  public void setUp() {
    this.testDAOProvider = new TestDbEnv();
    DAORegistry daoRegistry = TestDbEnv.getInstance();
    this.anomalyDAO = daoRegistry.getMergedAnomalyResultDAO();
    this.taskDAO = daoRegistry.getTaskDAO();
    this.evaluationDAO = daoRegistry.getEvaluationManager();
    this.configId = 1;
  }

  @Test
  public void testBuildRegressionStatus() {
    long startTime = 100;
    long endTime = 200;
    EvaluationDTO evaluation = new EvaluationDTO();
    evaluation.setDetectionConfigId(this.configId);
    evaluation.setStartTime(110);
    evaluation.setEndTime(120);
    evaluation.setMape(0.1);
    evaluation.setDetectorName("detection_rule_1");
    this.evaluationDAO.save(evaluation);

    DetectionHealth
        health = new DetectionHealth.Builder(configId, startTime, endTime)
        .addRegressionStatus(this.evaluationDAO).build();
    Assert.assertEquals(health.getRegressionStatus().getDetectorMapes(),
        ImmutableMap.of(evaluation.getDetectorName(), evaluation.getMape()));
    Assert.assertEquals(health.getRegressionStatus().getDetectorHealthStatus(),
        ImmutableMap.of(evaluation.getDetectorName(),
            HealthStatus.GOOD));
    Assert.assertEquals(health.getRegressionStatus().getHealthStatus(), HealthStatus.GOOD);
  }

  @Test
  public void testBuildTaskStatus() {
    long startTime = 100;
    long endTime = 200;
    TaskDTO task1 = new TaskDTO();
    task1.setJobName("DETECTION_" + this.configId);
    task1.setStatus(TaskStatus.COMPLETED);
    task1.setStartTime(110);
    task1.setEndTime(120);
    task1.setTaskType(TaskType.DETECTION);
    this.taskDAO.save(task1);

    TaskDTO task2 = new TaskDTO();
    task2.setJobName("DETECTION_" + this.configId);
    task2.setStatus(TaskStatus.FAILED);
    task2.setStartTime(130);
    task2.setEndTime(140);
    task2.setTaskType(TaskType.DETECTION);
    this.taskDAO.save(task2);
    DetectionHealth health = new DetectionHealth.Builder(configId, startTime, endTime)
        .addDetectionTaskStatus(this.taskDAO, 2).build();
    Assert.assertEquals(health.getDetectionTaskStatus().getHealthStatus(), HealthStatus.MODERATE);
    Assert.assertEquals(health.getDetectionTaskStatus().getTaskCounts(),
        ImmutableMap
            .of(TaskStatus.COMPLETED, 1L, TaskStatus.TIMEOUT, 0L,
                TaskStatus.WAITING, 0L, TaskStatus.FAILED, 1L));
    Assert.assertEquals(health.getDetectionTaskStatus().getTaskSuccessRate(), 0.5);
    Assert.assertEquals(health.getDetectionTaskStatus().getTasks(), Arrays.asList(task2, task1));
  }

  @Test
  public void testAnomalyCoverageStatus() {
    long startTime = 100;
    long endTime = 200;
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setDetectionConfigId(this.configId);
    anomaly.setStartTime(110);
    anomaly.setEndTime(120);
    anomaly.setMetricUrn("thirdeye:metric:1:country%3Dus");
    this.anomalyDAO.save(anomaly);
    MergedAnomalyResultDTO anomaly2 = new MergedAnomalyResultDTO();
    anomaly2.setDetectionConfigId(this.configId);
    anomaly2.setStartTime(115);
    anomaly2.setEndTime(125);
    anomaly2.setMetricUrn("thirdeye:metric:1:country%3Dcn");
    this.anomalyDAO.save(anomaly2);
    DetectionHealth health = new DetectionHealth.Builder(configId, startTime, endTime)
        .addAnomalyCoverageStatus(this.anomalyDAO).build();
    Assert.assertEquals(health.getAnomalyCoverageStatus().getAnomalyCoverageRatio(), 0.15);
    Assert.assertEquals(health.getAnomalyCoverageStatus().getHealthStatus(), HealthStatus.GOOD);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() {
    this.testDAOProvider.cleanup();
  }
}
