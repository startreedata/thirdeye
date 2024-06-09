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
package ai.startree.thirdeye.datalayer.bao;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.aspect.TimeProvider;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
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
 * -javaagent:[USER_PATH]/.m2/repository/org/aspectj/aspectjweaver/1.9.21/aspectjweaver-1.9.21.jar --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED
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
  private TaskManager taskDAO;

  @BeforeClass
  void beforeClass() {
    assertThat(CLOCK.isTimeMockWorking()).isTrue();
    CLOCK.useMockTime(JANUARY_1_2022);  // JANUARY 1 2022
    Injector injector = MySqlTestDatabase.sharedInjector();
    taskDAO = injector.getInstance(TaskManager.class);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    CLOCK.useSystemTime();
    taskDAO.findAll().forEach(taskDAO::delete);
  }

  @Test
  public void testCreate() throws JsonProcessingException {
    anomalyTaskId1 = taskDAO.save(getTestTaskSpec( 1));
    Assert.assertNotNull(anomalyTaskId1);
    anomalyTaskId2 = taskDAO.save(getTestTaskSpec( 2));
    Assert.assertNotNull(anomalyTaskId2);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFindAll() {
    List<TaskDTO> anomalyTasks = taskDAO.findAll();
    Assert.assertEquals(anomalyTasks.size(), 2);
  }

  @Test(dependsOnMethods = {"testFindAll"})
  public void testAcquireTaskToRun() {
    CLOCK.tick(1);
    Long workerId = 1L;
    TaskDTO taskDTO = taskDAO.findById(anomalyTaskId1);
    final long currentVersion = taskDTO.getVersion();
    boolean status = taskDAO.acquireTaskToRun(taskDTO, workerId);
    // refetch task from the persistence layer and check its values
    TaskDTO anomalyTask = taskDAO.findById(anomalyTaskId1);
    Assert.assertTrue(status);
    Assert.assertEquals(anomalyTask.getStatus(), TaskStatus.RUNNING);
    Assert.assertEquals(anomalyTask.getWorkerId(), workerId);
    Assert.assertEquals(anomalyTask.getVersion(), currentVersion + 1);
  }

  @Test(dependsOnMethods = {"testAcquireTaskToRun"})
  public void testFindNextTaskToRun() {
    TaskDTO anomalyTask = taskDAO.findNextTaskToRun();
    assertThat(anomalyTask).isNotNull();
  }

  @Test(dependsOnMethods = {"testFindNextTaskToRun"})
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

  @Test(dependsOnMethods = {"testCreate"})
  public void testUpdateTaskStartTime() {
    CLOCK.tick(50);
    long taskStartTime = System.currentTimeMillis();
    taskDAO.updateTaskStartTime(anomalyTaskId1, taskStartTime);
    TaskDTO anomalyTask = taskDAO.findById(anomalyTaskId1);
    Assert.assertEquals(anomalyTask.getStartTime(), taskStartTime);
  }

  TaskDTO getTestTaskSpec(final long refId) throws JsonProcessingException {
    TaskDTO taskSpec = new TaskDTO();
    taskSpec.setJobName("Test_Anomaly_Task");
    taskSpec.setStatus(TaskStatus.WAITING);
    taskSpec.setTaskType(TaskType.DETECTION);
    taskSpec.setStartTime(new DateTime(DateTimeZone.UTC).minusDays(20).getMillis());
    taskSpec.setEndTime(new DateTime(DateTimeZone.UTC).minusDays(10).getMillis());
    taskSpec.setTaskInfo(new ObjectMapper().writeValueAsString(new MockTaskInfo()));
    taskSpec.setRefId(refId);
    return taskSpec;
  }

  public static class MockTaskInfo implements TaskInfo {
    public int property = 10;

    @Override
    public long getRefId() {
      return 0;
    }
  }
}
