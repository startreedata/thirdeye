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
package ai.startree.thirdeye.detection.anomaly.detection.trigger;

import ai.startree.thirdeye.datalayer.bao.TestDbEnv;
import ai.startree.thirdeye.detection.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.DetectionPipelineTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataAvailabilityTaskSchedulerTest {

  private static final Logger LOG = LoggerFactory
      .getLogger(DataAvailabilityTaskSchedulerTest.class);
  private static final long TEST_TIME = System.currentTimeMillis();
  private static final String TEST_DATASET_PREFIX = "ds_trigger_scheduler_";
  private TestDbEnv testDAOProvider;
  private DataAvailabilityTaskScheduler dataAvailabilityTaskScheduler;
  private AlertManager detectionConfigDAO;
  private long metricId1;
  private long metricId2;

  @BeforeMethod
  public void beforeMethod() {
    testDAOProvider = new TestDbEnv();
    detectionConfigDAO = TestDbEnv.getInstance().getDetectionConfigManager();
    MetricConfigManager metricConfigManager = TestDbEnv.getInstance().getMetricConfigDAO();
    final String TEST_METRIC_PREFIX = "metric_trigger_scheduler_";

    MetricConfigDTO metric1 = new MetricConfigDTO();
    metric1.setDataset(TEST_DATASET_PREFIX + 1);
    metric1.setName(TEST_METRIC_PREFIX + 1);
    metric1.setActive(true);
    metric1.setAlias("");
    metricId1 = metricConfigManager.save(metric1);

    MetricConfigDTO metric2 = new MetricConfigDTO();
    metric2.setDataset(TEST_DATASET_PREFIX + 2);
    metric2.setName(TEST_METRIC_PREFIX + 2);
    metric2.setActive(true);
    metric2.setAlias("");
    metricId2 = metricConfigManager.save(metric2);

    dataAvailabilityTaskScheduler = new DataAvailabilityTaskScheduler(
        new DataAvailabilitySchedulingConfiguration()
            .setScheduleDelayInSec(60)
            .setTaskTriggerFallBackTimeInSec(TimeUnit.DAYS.toSeconds(1))
            .setSchedulingWindowInSec(TimeUnit.MINUTES.toSeconds(30))
            .setScheduleDelayInSec(TimeUnit.MINUTES.toSeconds(10)),
        TestDbEnv.getInstance().getDetectionConfigManager(),
        TestDbEnv.getInstance().getDatasetConfigDAO(),
        TestDbEnv.getInstance().getTaskDAO(),
        TestDbEnv.getInstance().getMetricConfigDAO()
    );
  }

  @AfterMethod
  public void afterMethod() {
    testDAOProvider.cleanup();
  }

  @Test
  public void testCreateOneTask() {
    List<Long> metrics = Arrays.asList(metricId1, metricId2);
    long detectionId = createDetection(1, metrics, TEST_TIME - TimeUnit.DAYS.toMillis(1), 0);
    long tenMinAgo = TEST_TIME - TimeUnit.MINUTES.toMillis(10);
    createDataset(1, TEST_TIME, tenMinAgo);
    createDataset(2, TEST_TIME, tenMinAgo);
    dataAvailabilityTaskScheduler.run();
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    List<TaskDTO> tasks = taskManager.findAll();
    Assert.assertEquals(tasks.size(), 1);
    TaskDTO task = tasks.get(0);
    Assert.assertEquals(task.getStatus(), TaskStatus.WAITING);
    Assert.assertEquals(task.getJobName(),
        TaskType.DETECTION.toString() + "_" + detectionId);
  }

  @Test
  public void testCreateMultipleTasks() {
    List<Long> metrics1 = Arrays.asList(metricId1, metricId2);
    long oneDayAgo = TEST_TIME - TimeUnit.DAYS.toMillis(1);
    long detection1 = createDetection(1, metrics1, oneDayAgo, 0);
    long detection2 = createDetection(2, metrics1, oneDayAgo, 0);
    List<Long> singleMetric = Collections.singletonList(metricId2);
    long detection3 = createDetection(3, singleMetric, oneDayAgo, 0);
    long tenMinAgo = TEST_TIME - TimeUnit.MINUTES.toMillis(10);
    createDataset(1, TEST_TIME, tenMinAgo);
    createDataset(2, TEST_TIME, tenMinAgo);
    dataAvailabilityTaskScheduler.run();
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    List<TaskDTO> tasks = taskManager.findAll();
    Assert.assertEquals(tasks.size(), 3);
    Assert.assertEquals(tasks.get(0).getStatus(), TaskStatus.WAITING);
    Assert.assertEquals(tasks.get(1).getStatus(), TaskStatus.WAITING);
    Assert.assertEquals(tasks.get(2).getStatus(), TaskStatus.WAITING);
    Assert.assertEquals(
        Stream.of(detection1, detection2, detection3)
            .map(x -> TaskType.DETECTION.toString() + "_" + x)
            .collect(Collectors.toSet()),
        new HashSet<>(Arrays.asList(tasks.get(0).getJobName(), tasks.get(1).getJobName(),
            tasks.get(2).getJobName())));
  }

  @Test
  public void testNoReadyDetection() {
    List<Long> metrics1 = Arrays.asList(metricId1, metricId2);
    long detection1 = createDetection(1, metrics1, TEST_TIME, 0);
    long detection2 = createDetection(2, Collections.singletonList(metricId2), TEST_TIME, 0);
    createDataset(1, TEST_TIME + TimeUnit.HOURS.toMillis(1), TEST_TIME); // updated dataset
    createDataset(2, TEST_TIME - TimeUnit.HOURS.toMillis(1), TEST_TIME); // not updated dataset
    createDetectionTask(detection1, TEST_TIME - 60_000, TaskStatus.COMPLETED);
    createDetectionTask(detection2, TEST_TIME - 60_000, TaskStatus.COMPLETED);
    List<AlertDTO> detectionConfigs = detectionConfigDAO.findAll();
    Assert.assertEquals(detectionConfigs.size(), 2);
    dataAvailabilityTaskScheduler.run();
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    List<TaskDTO> tasks = taskManager.findAll();
    Assert.assertEquals(tasks.size(), 2);
  }

  @Test
  public void testDetectionExceedNotRunThreshold() {
    List<Long> metrics1 = Arrays.asList(metricId1, metricId2);
    long oneDayAgo = TEST_TIME - TimeUnit.DAYS.toMillis(1);
    long detection1 = createDetection(1, metrics1, oneDayAgo, 0);
    long detection2 = createDetection(2, Collections.singletonList(metricId2), oneDayAgo, 0);
    long detection3 = createDetection(3, Collections.singletonList(metricId2), oneDayAgo,
        2 * 24 * 60 * 60);
    createDataset(1, oneDayAgo - 60_000, TEST_TIME); // not updated dataset
    createDataset(2, oneDayAgo - 60_000, TEST_TIME); // not updated dataset
    createDetectionTask(detection1, oneDayAgo - 60_000, TaskStatus.COMPLETED);
    createDetectionTask(detection2, oneDayAgo - 60_000, TaskStatus.COMPLETED);
    createDetectionTask(detection3, oneDayAgo - 60_000, TaskStatus.COMPLETED);
    dataAvailabilityTaskScheduler.run();
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    List<TaskDTO> tasks = taskManager.findAll();
    Assert.assertEquals(tasks.size(), 5);
    Assert.assertEquals(
        tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count(), 3);
    Assert.assertEquals(
        tasks.stream().filter(t -> t.getStatus() == TaskStatus.WAITING).count(), 2);
  }

  @Test
  public void testScheduleWithUnfinishedTask() {
    List<Long> metrics1 = Arrays.asList(metricId1, metricId2);
    long oneDayAgo = TEST_TIME - TimeUnit.DAYS.toMillis(1);
    long detection1 = createDetection(1, metrics1, oneDayAgo, 0);
    long detection2 = createDetection(2, metrics1, oneDayAgo, 0);
    List<Long> singleMetric = Collections.singletonList(metricId2);
    long detection3 = createDetection(3, singleMetric, oneDayAgo, 0);
    long tenMinAgo = TEST_TIME - TimeUnit.MINUTES.toMillis(10);
    createDataset(1, TEST_TIME, tenMinAgo);
    createDataset(2, TEST_TIME, tenMinAgo);
    createDetectionTask(detection1, oneDayAgo, TaskStatus.RUNNING);
    dataAvailabilityTaskScheduler.run();
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    List<TaskDTO> tasks = taskManager.findAll();
    Assert.assertEquals(tasks.size(), 3);
    List<TaskDTO> waitingTasks = tasks.stream()
        .filter(t -> t.getStatus() == TaskStatus.WAITING).collect(
            Collectors.toList());
    Assert.assertEquals(
        tasks.stream().filter(t -> t.getStatus() == TaskStatus.RUNNING).count(), 1);
    Assert.assertEquals(waitingTasks.size(), 2);
    Assert.assertEquals(
        Stream.of(detection2, detection3)
            .map(x -> TaskType.DETECTION.toString() + "_" + x)
            .collect(Collectors.toSet()),
        new HashSet<>(
            Arrays.asList(waitingTasks.get(0).getJobName(), waitingTasks.get(1).getJobName())));
  }

  @Test
  public void testScheduleOutOfSchedulingWindow() {
    long oneDayAgo = TEST_TIME - TimeUnit.DAYS.toMillis(1);
    long halfHourAgo = TEST_TIME - TimeUnit.MINUTES.toMillis(30);
    long tenMinAgo = TEST_TIME - TimeUnit.MINUTES.toMillis(10);
    List<Long> metrics2 = Arrays.asList(metricId1, metricId2);
    long detection2 = createDetection(2, metrics2, oneDayAgo, 0);
    createDataset(1, TEST_TIME, halfHourAgo);
    createDataset(2, TEST_TIME, tenMinAgo);
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    Assert.assertEquals(taskManager.findAll().size(), 0);
    dataAvailabilityTaskScheduler.run();
    List<TaskDTO> tasks = taskManager.findAll();
    Assert.assertEquals(tasks.size(), 1);
    Assert.assertEquals(TaskType.DETECTION.toString() + "_" + detection2,
        tasks.get(0).getJobName());
  }

  @Test
  public void testFallbackOutOfSchedulingWindow() {
    List<Long> metrics1 = Collections.singletonList(metricId1);
    long oneDayAgo = TEST_TIME - TimeUnit.DAYS.toMillis(1);
    long detection1 = createDetection(1, metrics1, oneDayAgo, 0);
    long halfHourAgo = TEST_TIME - TimeUnit.MINUTES.toMillis(30);
    createDataset(1, TEST_TIME, halfHourAgo);
    createDetectionTask(detection1, oneDayAgo - 60_000, TaskStatus.COMPLETED);
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    dataAvailabilityTaskScheduler.run();
    List<TaskDTO> tasks = taskManager.findAll();
    List<TaskDTO> waitingTasks = tasks.stream()
        .filter(t -> t.getStatus() == TaskStatus.WAITING).collect(
            Collectors.toList());
    Assert.assertEquals(waitingTasks.size(), 1);
    Assert.assertEquals(TaskType.DETECTION.toString() + "_" + detection1,
        waitingTasks.get(0).getJobName());
  }

  @Test
  public void testSkipSchedulingWithinDelay() {
    List<Long> metrics1 = Collections.singletonList(metricId1);
    long fiveMinAgo = TEST_TIME - TimeUnit.MINUTES.toMillis(5);
    long halfDayAgo = TEST_TIME - TimeUnit.HOURS.toMillis(12);
    long detection1 = createDetection(1, metrics1, halfDayAgo, 0);
    createDataset(1, TEST_TIME, fiveMinAgo);
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    dataAvailabilityTaskScheduler.run();
    List<TaskDTO> tasks = taskManager.findAll();
    Assert.assertEquals(tasks.size(), 0);
  }

  private long createDataset(int intSuffix, long refreshTime, long refreshEventTime) {
    DatasetConfigManager datasetConfigDAO = TestDbEnv.getInstance().getDatasetConfigDAO();
    DatasetConfigDTO ds1 = new DatasetConfigDTO();
    ds1.setDataset(TEST_DATASET_PREFIX + intSuffix);
    ds1.setLastRefreshTime(refreshTime);
    ds1.setLastRefreshEventTime(refreshEventTime);
    return datasetConfigDAO.save(ds1);
  }

  private long createDetection(int intSuffix, List<Long> metrics, long lastTimestamp,
      int notRunThreshold) {
    return detectionConfigDAO
        .save(generateDetectionConfig(intSuffix, metrics, lastTimestamp, notRunThreshold));
  }

  private AlertDTO generateDetectionConfig(int intSuffix, List<Long> metrics, long lastTimestamp,
      int notRunThreshold) {
    final String TEST_DETECTION_PREFIX = "detection_trigger_listener_";

    AlertDTO detect = new AlertDTO();
    detect.setName(TEST_DETECTION_PREFIX + intSuffix);
    detect.setActive(true);
    Map<String, Object> props = new HashMap<>();
    List<String> metricUrns = metrics.stream().map(x -> "thirdeye:metric:" + x)
        .collect(Collectors.toList());
    props.put("nestedMetricUrns", metricUrns);
    detect.setProperties(props);
    detect.setLastTimestamp(lastTimestamp);
    detect.setDataAvailabilitySchedule(true);
    detect.setTaskTriggerFallBackTimeInSec(notRunThreshold);

    return detect;
  }

  private long createDetectionTask(long detectionId, long createTime,
      TaskStatus status) {
    TaskManager taskManager = TestDbEnv.getInstance().getTaskDAO();
    TaskDTO task = new TaskDTO();
    DetectionPipelineTaskInfo taskInfo = new DetectionPipelineTaskInfo(detectionId, createTime - 1,
        createTime);
    String taskInfoJson = null;
    try {
      taskInfoJson = new ObjectMapper().writeValueAsString(taskInfo);
    } catch (JsonProcessingException e) {
      LOG.error("Exception when converting DetectionPipelineTaskInfo {} to jsonString", taskInfo,
          e);
    }
    task.setTaskType(TaskType.DETECTION);
    task.setJobName(TaskType.DETECTION.toString() + "_" + detectionId);
    task.setStatus(status);
    task.setTaskInfo(taskInfoJson);
    return taskManager.save(task);
  }
}
