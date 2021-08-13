package org.apache.pinot.thirdeye.mapper;

import org.apache.pinot.thirdeye.spi.api.EventApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EventMapper {

  EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

  @Mapping(source = "type", target = "eventType")
  EventDTO toDto(EventApi api);

  @Mapping(source = "eventType", target = "type")
  EventApi toApi(EventDTO dto);
}
