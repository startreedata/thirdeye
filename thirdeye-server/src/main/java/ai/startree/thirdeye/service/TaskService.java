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
package ai.startree.thirdeye.service;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskService extends CrudService<TaskApi, TaskDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("type", "type")
      .put("status", "status")
      .put("created", "createTime")
      .put("updated", "updateTime")
      .put("startTime", "startTime")
      .put("endTime", "endTime")
      .put("refId", "refId")
      .build();

  private final TaskManager taskManager;

  @Inject
  public TaskService(final TaskManager taskManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, taskManager, API_TO_INDEX_FILTER_MAP);
    this.taskManager = taskManager;
  }

  // operation not supported to prevent creation of tasks via API
  @Override
  protected void prepareCreatedDto(final ThirdEyePrincipal principal, final TaskDTO taskDTO) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected TaskApi toApi(final TaskDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  public void purge(final ThirdEyeServerPrincipal principal, final int nDaysToDelete, final int limit) {
    authorizationManager.ensureHasRootAccess(principal);
    taskManager.purge(Duration.ofDays(nDaysToDelete), limit);
  }
}
