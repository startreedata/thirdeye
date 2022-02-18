/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses={DatasetMapper.class, MetricMapper.class})
public interface AlertTemplateMapper {

  AlertTemplateMapper INSTANCE = Mappers.getMapper(AlertTemplateMapper.class);

  AlertTemplateDTO toBean(AlertTemplateApi api);

  AlertTemplateApi toApi(AlertTemplateDTO dto);

}
