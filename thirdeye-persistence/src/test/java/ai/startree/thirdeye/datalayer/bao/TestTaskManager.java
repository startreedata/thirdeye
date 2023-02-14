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
  public void notificationTaskLatencyMetricTest() {
    final Timestamp currentTime = new Timestamp(System.currentTimeMillis());
    final Timestamp olderTS = new Timestamp(currentTime.getTime() - 60000);
    final Timestamp recentTS = new Timestamp(currentTime.getTime() - 30000);

    final List<TaskDTO> tasks = List.of(
        buildTask("test-job-1", TaskType.NOTIFICATION, TaskStatus.WAITING, recentTS),
        buildTask("test-job-2", TaskType.NOTIFICATION, TaskStatus.RUNNING, olderTS));

    final Long latency = getGaugeValue(tasks, "notificationTaskLatencyInMillis");
    assertThat(latency).isGreaterThanOrEqualTo(60000);
  }

  @Test
  public void detectionTaskLatencyMetricTest() {
    final Timestamp currentTime = new Timestamp(System.currentTimeMillis());
    final Timestamp olderTS = new Timestamp(currentTime.getTime() - 60000);
    final Timestamp recentTS = new Timestamp(currentTime.getTime() - 30000);

    final List<TaskDTO> tasks = List.of(
        buildTask("test-job-1", TaskType.DETECTION, TaskStatus.WAITING, recentTS),
        buildTask("test-job-2", TaskType.DETECTION, TaskStatus.RUNNING, olderTS));

    final Long latency = getGaugeValue(tasks, "detectionTaskLatencyInMillis");
    assertThat(latency).isGreaterThanOrEqualTo(60000);
  }

  @Test
  public void taskLatencyMetricWithEmptyTaskListTest() {
    assertThat(getGaugeValue(List.of(), "detectionTaskLatencyInMillis")).isZero();
    assertThat(getGaugeValue(List.of(), "notificationTaskLatencyInMillis")).isZero();
  }

  private Long getGaugeValue(final List<TaskDTO> tasks, final String gaugeName) {
    final TaskDao dao = Mockito.mock(TaskDao.class);
    when(dao.filter(any())).thenReturn(tasks);
    final MetricRegistry metricRegistry = new MetricRegistry();
    new TaskManagerImpl(dao, metricRegistry);
    return (Long) metricRegistry.getGauges().get(gaugeName).getValue();
  }

  private TaskDTO buildTask(String name, TaskType type, TaskStatus status, Timestamp createTme) {
    return (TaskDTO) new TaskDTO()
        .setJobName(name)
        .setTaskType(type)
        .setStatus(status)
        .setCreateTime(createTme);
  }
}
