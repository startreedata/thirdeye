/*
 * Copyright 2023 StarTree Inc
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.datalayer.dao.TaskDao;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.codahale.metrics.MetricRegistry;
import java.sql.Timestamp;
import java.util.List;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class TestTaskManager {

  @Test
  public void taskLatencyMetricTest() {
    final TaskDTO pendingTask = buildTask("test-job-1", TaskType.DETECTION, TaskStatus.WAITING);
    final TaskDTO completedTask = buildTask("test-job-2", TaskType.NOTIFICATION, TaskStatus.COMPLETED);
    final List<TaskDTO> tasks = List.of(pendingTask, completedTask);

    final Timestamp currentTime = new Timestamp(System.currentTimeMillis());

    // Case 1 - Task execution fulfilled with a higher latency than pending tasks
    pendingTask.setCreateTime(currentTime);
    completedTask.setCreateTime(new Timestamp(currentTime.getTime() - 60000))
        .setUpdateTime(currentTime);
    Long value = getTaskLatencyGaugeValue(tasks);
    assertThat(value).isGreaterThanOrEqualTo(60000);

    // Case 2 - Pending tasks have a higher latency than completed task runs
    pendingTask.setCreateTime(new Timestamp(currentTime.getTime() - 300000));
    value = getTaskLatencyGaugeValue(tasks);
    assertThat(value).isGreaterThanOrEqualTo(300000);
  }

  private Long getTaskLatencyGaugeValue(final List<TaskDTO> tasks) {
    final TaskDao dao = Mockito.mock(TaskDao.class);
    when(dao.filter(any())).thenReturn(tasks);
    final MetricRegistry metricRegistry = new MetricRegistry();
    new TaskManagerImpl(dao, metricRegistry);
    return (Long) metricRegistry.getGauges().get("taskLatencyInMillis").getValue();
  }

  private TaskDTO buildTask(String name, TaskType type, TaskStatus status) {
    return new TaskDTO()
        .setJobName(name)
        .setTaskType(type)
        .setStatus(status);
  }
}
