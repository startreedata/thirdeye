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
package ai.startree.thirdeye.worker.task;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HeartbeatTest {

  private static final Logger LOG = LoggerFactory.getLogger(HeartbeatTest.class);

  private static final Duration TASK_DELAY = Duration.ofSeconds(3);
  private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(1);
  public static final long TASK_ID = 1L;

  private TaskManager taskManager;
  private TaskDriverThreadPoolManager taskDriverThreadPoolManager;
  private TaskDriverConfiguration config;
  private TaskRunnerFactory taskRunnerFactory;
  private int pollingCount = 0;

  private static String toJson(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (final JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  @BeforeClass
  public void setUp() {
    config = new TaskDriverConfiguration()
        .setRandomWorkerIdEnabled(true)
        .setHeartbeatInterval(HEARTBEAT_INTERVAL);

    taskManager = Mockito.mock(TaskManager.class);
    when(taskManager.updateStatusAndWorkerId(anyLong(), anyLong(), anySet(), anyInt()))
        .thenReturn(true);

    doNothing().when(taskManager)
        .updateStatusAndTaskEndTime(anyLong(), any(), any(), anyLong(), any());

    taskDriverThreadPoolManager = new TaskDriverThreadPoolManager(config);

    taskRunnerFactory = Mockito.mock(TaskRunnerFactory.class);
    when(taskRunnerFactory.get(any())).thenReturn((taskInfo, taskContext) -> {
      Thread.sleep(TASK_DELAY.toMillis());
      return null;
    });
  }

  @AfterClass
  public void tearDown() {
    taskDriverThreadPoolManager.shutdown();
  }

  @Test
  public void heartbeatPulseCheck() {
    final Timestamp startTime = new Timestamp(System.currentTimeMillis());
    final TaskDTO taskDTO = newTask();
    when(taskManager.findByStatusOrderByCreateTime(eq(TaskStatus.WAITING), anyInt(), anyBoolean()))
        .thenAnswer(i -> pollingCount++ == 0? List.of(taskDTO) : List.of());

    doAnswer(invocation -> {
      taskDTO.setStatus(TaskStatus.COMPLETED);

      // Shutdown after first execution
      taskDriverThreadPoolManager.shutdown();
      return null;
    }).when(taskManager).updateStatusAndTaskEndTime(eq(TASK_ID),
        eq(TaskStatus.RUNNING),
        eq(TaskStatus.COMPLETED),
        any(),
        anyString());
    final AtomicInteger pulseCount = new AtomicInteger();

    doAnswer(invocation -> {
      taskDTO.setLastActive(new Timestamp(System.currentTimeMillis()));
      final int count = pulseCount.getAndIncrement();
      LOG.info("PulseCount: " + count);
      // to ensure the worker stops after executing one task
//      taskDriverThreadPoolManager.shutdown();
      return null;
    }).when(taskManager).updateLastActive(anyLong());

    final TaskContext taskContext = newTaskContext();
    final TaskDriverRunnable taskDriverRunnable = new TaskDriverRunnable(taskContext);
    taskDriverRunnable.run();

    assertThat(taskDTO.getLastActive())
        .isAfter(new Timestamp(startTime.getTime() + TASK_DELAY.toMillis()));

    // heartbeat should tick for aleast 5 times as task delay is heartbeatInterval * 5 (5 seconds)
    assertThat(pulseCount.get())
        .isGreaterThanOrEqualTo((int) (TASK_DELAY.toMillis() / HEARTBEAT_INTERVAL.toMillis()));
  }

  private TaskContext newTaskContext() {
    return new TaskContext()
        .setConfig(config)
        .setWorkerId(0)
        .setTaskManager(taskManager)
        .setTaskRunnerFactory(taskRunnerFactory)
        .setMetricRegistry(new MetricRegistry())
        .setTaskDriverThreadPoolManager(taskDriverThreadPoolManager);
  }

  private TaskDTO newTask() {
    final TaskDTO task = new TaskDTO()
        .setStatus(TaskStatus.WAITING)
        .setJobName("TestJob")
        .setTaskType(TaskType.DETECTION)
        .setTaskInfo(requireNonNull(toJson(new DetectionPipelineTaskInfo())));

    task.setId(TASK_ID);
    return task;
  }
}
