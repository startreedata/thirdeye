/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.NotificationSpecApi;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSpecDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NotificationSpecMapper {

  NotificationSpecMapper INSTANCE = Mappers.getMapper(NotificationSpecMapper.class);

  NotificationSpecDTO toBean(NotificationSpecApi api);

  NotificationSpecApi toApi(NotificationSpecDTO dto);
}
