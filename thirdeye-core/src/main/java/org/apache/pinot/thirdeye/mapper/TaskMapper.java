package org.apache.pinot.thirdeye.mapper;

import org.apache.pinot.thirdeye.spi.api.TaskApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
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
