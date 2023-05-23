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
        .setGranularityToBucketTimeColumn(api.getGranularityToBucketTimeColumn())
        ;
    optional(api.getTimeColumn()).ifPresent(timeColumn -> {
      dto.setTimeColumn(timeColumn.getName());
      updateTimeGranularityOnDataset(dto, timeColumn);
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
        .setGranularityToBucketTimeColumn(dto.getGranularityToBucketTimeColumn());
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
  static void updateTimeGranularityOnDataset(final DatasetConfigDTO dto,
      final TimeColumnApi timeColumn) {
    /*
     * TODO cyril fixme. best would be to use a ISO-8601 period instead of a Duration, or directly expose the TimeDuration in the API.
     *  minute or hourly
     */
    final TimeGranularity timeGranularity = TimeGranularity.fromDuration(timeColumn.getInterval());
    if (timeGranularity.toMillis() < 1000) {
      dto.setTimeDuration((int) timeGranularity.toMillis());
      dto.setTimeUnit(TimeUnit.MILLISECONDS);
    } else if (isDaily(timeGranularity)) {
      dto.setTimeDuration((int) timeGranularity.toDuration().toDays());
      dto.setTimeUnit(TimeUnit.DAYS);
    } else if (isHourly(timeGranularity)) {
      dto.setTimeDuration((int) timeGranularity.toDuration().toHours());
      dto.setTimeUnit(TimeUnit.HOURS);
    } else if (isMinutely(timeGranularity)) {
      dto.setTimeDuration((int) timeGranularity.toDuration().toMinutes());
      dto.setTimeUnit(TimeUnit.MINUTES);
    } else {
      dto.setTimeDuration((int) timeGranularity.toDuration().getSeconds());
      dto.setTimeUnit(TimeUnit.SECONDS);
    }
  }

  private static boolean isDaily(final TimeGranularity timeGranularity) {
    return timeGranularity.toMillis() % Duration.ofDays(1).toMillis() == 0;
  }

  static boolean isHourly(TimeGranularity timeGranularity) {
    return timeGranularity.toMillis() % Duration.ofHours(1).toMillis() == 0;
  }

  static boolean isMinutely(TimeGranularity timeGranularity) {
    return timeGranularity.toMillis() % Duration.ofMinutes(1).toMillis() == 0;
  }
}
