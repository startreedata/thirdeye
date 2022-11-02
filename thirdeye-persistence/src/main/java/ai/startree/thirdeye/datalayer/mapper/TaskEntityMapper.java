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
package ai.startree.thirdeye.datalayer.mapper;

import ai.startree.thirdeye.datalayer.entity.TaskEntity;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskEntityMapper {
  TaskEntityMapper INSTANCE = Mappers.getMapper(TaskEntityMapper.class);

  @Mapping(source = "jobName", target = "name")
  @Mapping(source = "taskType", target = "type")
  @Mapping(target = "jsonVal", ignore = true)
  TaskEntity toTaskEntity(TaskDTO dto);
}
