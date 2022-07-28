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
package ai.startree.thirdeye.datalayer.bao;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.aspect.TimeProvider;
import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class uses weaving to control the java system time.
 *
 * Note: if run within IntelliJ, run with the following JVM option:
 * -javaagent:[USER_PATH]/.m2/repository/org/aspectj/aspectjweaver/1.9.6/aspectjweaver-1.9.6.jar
 * IntelliJ does not use the pom surefire config: https://youtrack.jetbrains.com/issue/IDEA-52286
 *
 * In command line: ./mvnw -pl 'thirdeye-persistence' -Dtest=TestAnomalyTaskManager test
 *
 */
public class TestAnomalyTaskManager {

  private static final Set<TaskStatus> allowedOldTaskStatus = new HashSet<>();

  private static final TimeProvider CLOCK = TimeProvider.instance();
  // use a time big enough because Timestamp(small int) parses to hours instead of millis
  public static final long JANUARY_1_2022 = 1640998861000L;

  static {
    allowedOldTaskStatus.add(TaskStatus.FAILED);
    allowedOldTaskStatus.add(TaskStatus.WAITING);
  }

  private Long anomalyTaskId1;
  private Long anomalyTaskId2;
  private Long anomalyJobId;
  private JobManager jobDAO;
  private TaskManager taskDAO;

  @BeforeClass
  void beforeClass() {
    assertThat(CLOCK.isTimeMockWorking()).isTrue();
    CLOCK.useMockTime(JANUARY_1_2022);  // JANUARY 1 2022
    Injector injector = new TestDatabase().createInjector();
    jobDAO = injector.getInstance(JobManager.class);
    taskDAO = injector.getInstance(TaskManager.class);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    CLOCK.useSystemTime();
  }

  @Test
  public void testCreate() throws JsonProcessingException {
    JobDTO testAnomalyJobSpec = DatalayerTestUtils.getTestJobSpec();
    anomalyJobId = jobDAO.save(testAnomalyJobSpec);
    anomalyTaskId1 = taskDAO.save(getTestTaskSpec(testAnomalyJobSpec));
    Assert.assertNotNull(anomalyTaskId1);
    anomalyTaskId2 = taskDAO.save(getTestTaskSpec(testAnomalyJobSpec));
    Assert.assertNotNull(anomalyTaskId2);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFindAll() {
    List<TaskDTO> anomalyTasks = taskDAO.findAll();
    Assert.assertEquals(anomalyTasks.size(), 2);
  }

  @Test(dependsOnMethods = {"testFindAll"})
  public void testUpdateStatusAndWorkerId() {
    CLOCK.tick(1);
    Long workerId = 1L;
    TaskDTO taskDTO = taskDAO.findById(anomalyTaskId1);
    boolean status =
        taskDAO.updateStatusAndWorkerId(workerId, anomalyTaskId1, allowedOldTaskStatus,
            taskDTO.getVersion());
    TaskDTO anomalyTask = taskDAO.findById(anomalyTaskId1);
    Assert.assertTrue(status);
    Assert.assertEquals(anomalyTask.getStatus(), TaskStatus.RUNNING);
    Assert.assertEquals(anomalyTask.getWorkerId(), workerId);
    Assert.assertEquals(anomalyTask.getVersion(), taskDTO.getVersion() + 1);
  }

  @Test(dependsOnMethods = {"testUpdateStatusAndWorkerId"})
  public void testFindByStatusOrderByCreationTimeAsc() {
    List<TaskDTO> anomalyTasks =
        taskDAO.findByStatusOrderByCreateTime(TaskStatus.WAITING, Integer.MAX_VALUE, true);
    Assert.assertEquals(anomalyTasks.size(), 1);
  }

  @Test(dependsOnMethods = {"testFindByStatusOrderByCreationTimeAsc"})
  public void testUpdateStatusAndTaskEndTime() {
    TaskStatus oldStatus = TaskStatus.RUNNING;
    TaskStatus newStatus = TaskStatus.COMPLETED;
    CLOCK.tick(50);
    long taskEndTime = System.currentTimeMillis();
    taskDAO.updateStatusAndTaskEndTime(anomalyTaskId1, oldStatus, newStatus, taskEndTime,
        "testMessage");
    TaskDTO anomalyTask = taskDAO.findById(anomalyTaskId1);
    Assert.assertEquals(anomalyTask.getStatus(), newStatus);
    Assert.assertEquals(anomalyTask.getEndTime(), taskEndTime);
    Assert.assertEquals(anomalyTask.getMessage(), "testMessage");
  }

  @Test(dependsOnMethods = {"testUpdateStatusAndTaskEndTime"})
  public void testFindByJobIdStatusNotIn() throws InterruptedException {
    TaskStatus status = TaskStatus.COMPLETED;
    List<TaskDTO> anomalyTaskSpecs = taskDAO.findByJobIdStatusNotIn(anomalyJobId, status);
    Assert.assertEquals(anomalyTaskSpecs.size(), 1);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testUpdateTaskStartTime() {
    CLOCK.tick(50);
    long taskStartTime = System.currentTimeMillis();
    taskDAO.updateTaskStartTime(anomalyTaskId1, taskStartTime);
    TaskDTO anomalyTask = taskDAO.findById(anomalyTaskId1);
    Assert.assertEquals(anomalyTask.getStartTime(), taskStartTime);
  }

  @Test(dependsOnMethods = {"testFindByJobIdStatusNotIn"})
  public void testDeleteRecordOlderThanDaysWithStatus() {
    TaskStatus status = TaskStatus.COMPLETED;
    int numRecordsDeleted = taskDAO.deleteRecordsOlderThanDaysWithStatus(0, status);
    Assert.assertEquals(numRecordsDeleted, 1);
  }

  @Test(dependsOnMethods = {"testDeleteRecordOlderThanDaysWithStatus"})
  public void testFindByStatusWithinDays() throws JsonProcessingException {
    JobDTO testAnomalyJobSpec = DatalayerTestUtils.getTestJobSpec();
    anomalyJobId = jobDAO.save(testAnomalyJobSpec);
    anomalyTaskId1 = taskDAO.save(getTestTaskSpec(testAnomalyJobSpec));
    Assert.assertNotNull(anomalyTaskId1);
    anomalyTaskId2 = taskDAO.save(getTestTaskSpec(testAnomalyJobSpec));
    Assert.assertNotNull(anomalyTaskId2);

    CLOCK.tick(2); // To ensure every task has been created more than 1 ms ago

    List<TaskDTO> tasksWithZeroDays = taskDAO.findByStatusWithinDays(TaskStatus.WAITING, 0);
    Assert.assertEquals(tasksWithZeroDays.size(), 0);

    List<TaskDTO> tasksWithOneDays = taskDAO.findByStatusWithinDays(TaskStatus.WAITING, 1);
    Assert.assertTrue(tasksWithOneDays.size() > 0);
  }

  @Test(dependsOnMethods = {"testDeleteRecordOlderThanDaysWithStatus"})
  public void testFindTimeoutTasksWithinDays()
  {
    TaskDTO task1 = taskDAO.findById(anomalyTaskId1);
    task1.setStatus(TaskStatus.RUNNING);
    taskDAO.update(task1);

    CLOCK.tick(55); // To ensure every task has been updated more than 50 ms ago
    List<TaskDTO> timeoutTasksWithinOneDays = taskDAO.findTimeoutTasksWithinDays(7, 50);
    Assert.assertTrue(timeoutTasksWithinOneDays.size() > 0);
  }

  TaskDTO getTestTaskSpec(JobDTO anomalyJobSpec) throws JsonProcessingException {
    TaskDTO jobSpec = new TaskDTO();
    jobSpec.setJobName("Test_Anomaly_Task");
    jobSpec.setStatus(TaskStatus.WAITING);
    jobSpec.setTaskType(TaskType.DETECTION);
    jobSpec.setStartTime(new DateTime(DateTimeZone.UTC).minusDays(20).getMillis());
    jobSpec.setEndTime(new DateTime(DateTimeZone.UTC).minusDays(10).getMillis());
    jobSpec.setTaskInfo(new ObjectMapper().writeValueAsString(new MockTaskInfo()));
    jobSpec.setJobId(anomalyJobSpec.getId());
    return jobSpec;
  }

  public static class MockTaskInfo implements TaskInfo {
    public int property = 10;
  }
}
