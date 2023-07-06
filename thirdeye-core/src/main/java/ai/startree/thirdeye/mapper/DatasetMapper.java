/*
 * Copyright 2023 StarTree Inc
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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.TimeColumnApi;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DatasetMapper {

  DatasetMapper INSTANCE = Mappers.getMapper(DatasetMapper.class);

  default DatasetConfigDTO toBean(DatasetApi api) {
    if (api == null) {
      return null;
    }
    final DatasetConfigDTO dto = new DatasetConfigDTO()
        .setDataset(api.getName())
        .setActive(api.getActive())
        .setCompletenessDelay(api.getCompletenessDelay())
        .setDataSource(optional(api.getDataSource()).map(DataSourceApi::getName).orElse(null))
        .setDimensions(api.getDimensions())
        .setRcaExcludedDimensions(api.getRcaExcludedDimensions())
        .setTimeColumns(api.getTimeColumns())
        ;
    optional(api.getTimeColumn()).ifPresent(timeColumn -> {
      dto.setTimeColumn(timeColumn.getName());
      updateTimeSpecOnDataset(dto, timeColumn);
      optional(timeColumn.getFormat()).ifPresent(dto::setTimeFormat);
      optional(timeColumn.getTimezone()).ifPresent(dto::setTimezone);
    });
    optional(api.getAuth())
        .map(ApiBeanMapper::toAuthorizationConfigurationDTO)
        .ifPresent(dto::setAuth);
    return dto;
  }

  default DatasetApi toApi(DatasetConfigDTO dto) {
    if (dto == null) {
      return null;
    }
    final DatasetApi datasetApi = new DatasetApi()
        .setId(dto.getId())
        .setActive(dto.getActive())
        .setDimensions(dto.getDimensions())
        .setName(dto.getDataset())
        .setDataSource(optional(dto.getDataSource())
            .map(datasourceName -> new DataSourceApi().setName(datasourceName))
            .orElse(null))
        .setCompletenessDelay(optional(dto.getCompletenessDelay()).orElse(null))
        .setAuth(optional(dto.getAuth())
            .map(ApiBeanMapper::toApi).orElse(null))
        .setTimeColumns(dto.getTimeColumns());
    optional(dto.getRcaExcludedDimensions()).ifPresent(datasetApi::setRcaExcludedDimensions);
    optional(dto.getTimeColumn()).ifPresent(timeColumn -> datasetApi.setTimeColumn(
        new TimeColumnApi()
            .setName(timeColumn)
            .setInterval(optional(dto.bucketTimeGranularity()).map(TimeGranularity::toDuration)
                .orElse(null))
            .setFormat(dto.getTimeFormat())
            .setTimezone(dto.getTimezone())));

    return datasetApi;
  }

  @VisibleForTesting
  static void updateTimeSpecOnDataset(final DatasetConfigDTO dto,
      final TimeColumnApi timeColumn) {
    //TODO cyril fixme. best would be to use a ISO-8601 period instead of a Duration
    final Duration d = timeColumn.getInterval();

    if (d.toMillis() < 1000) {
      dto.setTimeDuration(Math.toIntExact(d.toMillis()));
      dto.setTimeUnit(TimeUnit.MILLISECONDS);
    } else if (isDaily(d)) {
      dto.setTimeDuration(Math.toIntExact(d.toDays()));
      dto.setTimeUnit(TimeUnit.DAYS);
    } else if (isHourly(d)) {
      dto.setTimeDuration(Math.toIntExact(d.toHours()));
      dto.setTimeUnit(TimeUnit.HOURS);
    } else if (isMinutely(d)) {
      dto.setTimeDuration(Math.toIntExact(d.toMinutes()));
      dto.setTimeUnit(TimeUnit.MINUTES);
    } else {
      dto.setTimeDuration(Math.toIntExact(d.getSeconds()));
      dto.setTimeUnit(TimeUnit.SECONDS);
    }
  }

  private static boolean isDaily(final Duration d) {
    return d.toMillis() % Duration.ofDays(1).toMillis() == 0;
  }

  static boolean isHourly(final Duration d) {
    return d.toMillis() % Duration.ofHours(1).toMillis() == 0;
  }

  static boolean isMinutely(final Duration d) {
    return d.toMillis() % Duration.ofMinutes(1).toMillis() == 0;
  }
}
