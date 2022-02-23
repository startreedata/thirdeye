/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.mapper;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.TimeColumnApi;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DatasetMapper {

  DatasetMapper INSTANCE = Mappers.getMapper(DatasetMapper.class);

  default DatasetConfigDTO toBean(DatasetApi api) {
    final DatasetConfigDTO dto = new DatasetConfigDTO();
    optional(api.getDataSource())
        .map(DataSourceApi::getName)
        .ifPresent(dto::setDataSource);
    dto.setDataset(api.getName());
    dto.setDisplayName(api.getName());
    optional(api.getDimensions()).ifPresent(dto::setDimensions);
    optional(api.getTimeColumn()).ifPresent(timeColumn -> {
      dto.setTimeColumn(timeColumn.getName());

      updateTimeGranularityOnDataset(dto, timeColumn);
      optional(timeColumn.getFormat()).ifPresent(dto::setTimeFormat);
      optional(timeColumn.getTimezone()).ifPresent(dto::setTimezone);
    });
    optional(api.getExpectedDelay())
        .map(TimeGranularity::fromDuration)
        .ifPresent(dto::setExpectedDelay);

    return dto;

  }

  default DatasetApi toApi(DatasetConfigDTO dto) {
    return new DatasetApi()
        .setId(dto.getId())
        .setActive(dto.isActive())
        .setAdditive(dto.isAdditive())
        .setDimensions(dto.getDimensions())
        .setName(dto.getDataset())
        .setTimeColumn(new TimeColumnApi()
            .setName(dto.getTimeColumn())
            .setInterval(dto.bucketTimeGranularity().toDuration())
            .setFormat(dto.getTimeFormat())
            .setTimezone(dto.getTimezone())
        )
        .setExpectedDelay(dto.getExpectedDelay().toDuration())
        .setDataSource(new DataSourceApi()
            .setName(dto.getDataSource()))
        ;
  }

  private static void updateTimeGranularityOnDataset(final DatasetConfigDTO dto,
      final TimeColumnApi timeColumn) {
    TimeGranularity timeGranularity = TimeGranularity.fromDuration(timeColumn.getInterval());
    /*
     * TODO spyne fixme. this covers up the 86400 bug where 1_DAYS is different from 86400_SECONDS.
     */
    if (isDaily(timeGranularity)) {
      dto.setTimeDuration((int) timeGranularity.toDuration().toDays());
      dto.setTimeUnit(TimeUnit.DAYS);
    } else {
      dto.setTimeDuration((int) timeGranularity.toDuration().getSeconds());
      dto.setTimeUnit(TimeUnit.SECONDS);
    }
  }

  private static boolean isDaily(final TimeGranularity timeGranularity) {
    return timeGranularity.toDuration().getSeconds() % Duration.ofDays(1).getSeconds() == 0;
  }
}
