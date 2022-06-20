package ai.startree.thirdeye.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class HeartbeatTest {

  long taskDelay = Duration.ofSeconds(5).toMillis();

  @Test
  public void heartbeatPulseCheck() {
    Timestamp startTime = new Timestamp(System.currentTimeMillis());
    AtomicBoolean shutdown = new AtomicBoolean();
    TaskDTO taskDTO = getTaskDTO();
    final AtomicInteger pulseCount = new AtomicInteger();

    TaskManager taskManager = Mockito.mock(TaskManager.class);
    when(taskManager.updateStatusAndWorkerId(anyLong(), anyLong(), anySet(), anyInt())).thenReturn(true);
    when(taskManager.findByStatusOrderByCreateTime(any(TaskStatus.class), anyInt(), anyBoolean())).thenReturn(List.of(taskDTO));
    doNothing().when(taskManager).updateStatusAndTaskEndTime(anyLong(), any(), any(), anyLong(), any());
    doAnswer(invocation -> {
      taskDTO.setLastModified(invocation.getArgument(1));
      pulseCount.getAndIncrement();
      // to ensure the worker stops after executing one task
      shutdown.set(true);
      return null;
    }).when(taskManager).updateLastModified(anyLong(), any());

    TaskDriverConfiguration config = new TaskDriverConfiguration()
        .setRandomWorkerIdEnabled(true)
        .setHeartbeatInterval(Duration.ofSeconds(1));

    TaskDriverRunnable taskDriverRunnable = getTaskDriverRunnable(taskManager, shutdown, config);
    taskDriverRunnable.run();

    assertThat(taskDTO.getLastModified()).isAfter(new Timestamp(startTime.getTime() + taskDelay));
    // heartbeat should tick for aleast 5 times as task delay is heartbeatInterval*5 (5 seconds)
    assertThat(pulseCount.get()).isGreaterThanOrEqualTo(5);
  }

  private TaskDriverRunnable getTaskDriverRunnable(final TaskManager taskManager,
      final AtomicBoolean shutdown,
      final TaskDriverConfiguration config) {
    return new TaskDriverRunnable(taskManager,
        null,
        shutdown,
        getExecutorService(),
        config,
        0,
        getTaskRunnerFactory(),
        new MetricRegistry());
  }

  private TaskRunnerFactory getTaskRunnerFactory() {
    TaskRunnerFactory factory = Mockito.mock(TaskRunnerFactory.class);
    when(factory.get(any())).thenReturn((taskInfo, taskContext) -> {
      Thread.sleep(taskDelay);
      return null;
    });
    return factory;
  }

  private ExecutorService getExecutorService() {
    return Executors.newFixedThreadPool(1,
        new ThreadFactoryBuilder()
            .setNameFormat("test-executor-%d")
            .build());
  }

  private TaskDTO getTaskDTO() {
    try {
      return (TaskDTO) new TaskDTO()
          .setStatus(TaskStatus.WAITING)
          .setJobName("TestJob")
          .setTaskType(TaskType.DETECTION)
          .setTaskInfo(new ObjectMapper().writeValueAsString(new DetectionPipelineTaskInfo()))
          .setId(1L);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }
}
