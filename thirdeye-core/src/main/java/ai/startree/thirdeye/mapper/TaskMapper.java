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
package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskMapper {

  TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

  @Mapping(source = "job.id", target = "jobId")
  @Mapping(source = "job.jobName", target = "jobName")
  @Mapping(source = "created", target = "createTime")
  @Mapping(source = "updated", target = "updateTime")
  TaskDTO toDto(TaskApi api);

  @Mapping(target = "job.id", source = "jobId")
  @Mapping(target = "job.jobName", source = "jobName")
  @Mapping(source = "createTime", target = "created")
  @Mapping(source = "updateTime", target = "updated")
  TaskApi toApi(TaskDTO dto);
}
