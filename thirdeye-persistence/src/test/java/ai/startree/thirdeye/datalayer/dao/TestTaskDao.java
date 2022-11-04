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
package ai.startree.thirdeye.datalayer.dao;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.google.inject.Injector;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestTaskDao {

  private TaskDao dao;

  @BeforeClass
  void beforeClass() {
    Injector injector = MySqlTestDatabase.sharedInjector();
    dao = injector.getInstance(TaskDao.class);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    dao.getAll().forEach(task -> dao.delete(task.getId()));
  }

  private TaskDTO buildTask() {
    return new TaskDTO()
        .setTaskType(TaskType.DETECTION)
        .setStatus(TaskStatus.WAITING)
        .setJobName("xyz")
        .setJobId(1L)
        .setTaskInfo("info");
  }

  @Test
  public void saveTest() {
    TaskDTO dto = buildTask();
    Long id = dao.put(dto);
    assertThat(id).isGreaterThan(0L);
    dto.setId(id);
    TaskDTO dbDto = dao.get(id);
    assertThat(dbDto).isEqualTo(dto);
  }

  @Test(dependsOnMethods = {"saveTest"})
  public void updateTest() {
    TaskDTO dto = dao.getAll().get(0);
    dto.setVersion(dto.getVersion()+1);
    assertThat(dao.update(dto)).isEqualTo(1);
    assertThat(dao.get(dto.getId()).getVersion()).isEqualTo(dto.getVersion());
  }

  @Test(dependsOnMethods = {"updateTest"})
  public void deleteTest() {
    List<TaskDTO> tasks = dao.getAll();
    assertThat(tasks.size()).isGreaterThan(0);
    TaskDTO taskToDelete = tasks.get(0);
    dao.delete(taskToDelete.getId());
    assertThat(dao.get(taskToDelete.getId())).isNull();
    assertThat(dao.count()).isEqualTo(tasks.size()-1);
  }

  @Test
  public void deleteByPredicateTest() {
    TaskDTO dto = buildTask()
        .setJobName("deleteByPredicateTest");
    dao.put(dto);
    assertThat(dao.deleteByPredicate(Predicate.EQ("name", dto.getJobName()))).isEqualTo(1);
    assertThat(dao.get(dto.getId())).isNull();
  }
}
