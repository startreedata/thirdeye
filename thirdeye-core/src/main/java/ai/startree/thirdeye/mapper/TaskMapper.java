/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
  TaskDTO toDto(TaskApi api);

  @Mapping(target = "job.id", source = "jobId")
  @Mapping(target = "job.jobName", source = "jobName")
  TaskApi toApi(TaskDTO dto);
}
