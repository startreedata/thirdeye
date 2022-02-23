/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.mapper;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.util.SpiUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MetricMapper {

  MetricMapper INSTANCE = Mappers.getMapper(MetricMapper.class);

  default MetricConfigDTO toBean(MetricApi api) {
    final MetricConfigDTO dto = new MetricConfigDTO();

    dto.setId(api.getId());
    dto
        .setName(api.getName())
        .setAlias(SpiUtils.constructMetricAlias(api.getDataset().getName(), api.getName()))
        .setDataset(optional(api.getDataset())
            .map(DatasetApi::getName)
            .orElse(null))
        .setRollupThreshold(api.getRollupThreshold())
        .setAggregationColumn(api.getAggregationColumn())
        .setDatatype(api.getDatatype())
        .setDefaultAggFunction(api.getAggregationFunction())
        // TODO suvodeep Revisit this: Assume false if active is not set.
        .setActive(optional(api.getActive()).orElse(false))
        .setViews(api.getViews())
        .setWhere(api.getWhere())
        .setDerivedMetricExpression(api.getDerivedMetricExpression());

    return dto;
  }

  default MetricApi toApi(MetricConfigDTO dto) {
    return new MetricApi()
        .setId(dto.getId())
        .setActive(boolApi(dto.isActive()))
        .setName(dto.getName())
        .setUpdated(dto.getUpdateTime())
        .setDataset(new DatasetApi()
            .setName(dto.getDataset())
        )
        .setDerivedMetricExpression(dto.getDerivedMetricExpression())
        .setWhere(dto.getWhere())
        .setAggregationColumn(dto.getAggregationColumn())
        .setDatatype(dto.getDatatype())
        .setAggregationFunction(dto.getDefaultAggFunction())
        .setRollupThreshold(dto.getRollupThreshold())
        .setViews(dto.getViews())
        ;
  }

  private static Boolean boolApi(final boolean value) {
    return value ? true : null;
  }
}
