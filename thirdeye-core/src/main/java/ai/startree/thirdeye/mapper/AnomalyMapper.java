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
package ai.startree.thirdeye.mapper;

import static ai.startree.thirdeye.mapper.ApiBeanMapper.toMetricApi;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertMetadataApi;
import ai.startree.thirdeye.spi.api.AlertNodeApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertNodeType;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.util.Date;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AnomalyFeedbackMapper.class, AnomalyLabelMapper.class, MetricMapper.class,
    EnumerationItemMapper.class})
public interface AnomalyMapper {

  AnomalyMapper INSTANCE = Mappers.getMapper(AnomalyMapper.class);

  private static AlertNodeApi toDetectionAlertNodeApi(final String detectorComponentName) {
    final String[] splitted = detectorComponentName.split(":");
    checkState(splitted.length == 2);

    return new AlertNodeApi()
        .setName(splitted[0])
        .setType(AlertNodeType.DETECTION)
        .setSubType(splitted[1]);
  }

  @Mapping(source = "alert.id", target = "detectionConfigId")
  @Mapping(source = "metadata.metric.name", target = "metric")
  @Mapping(source = "metadata.dataset.name", target = "collection")
  MergedAnomalyResultDTO toDto(AnomalyApi api);

  default long map(@Nullable final Date value) {
    if (value == null) {
      return 0L;
    }
    return value.getTime();
  }

  default AnomalyApi toApi(final MergedAnomalyResultDTO dto) {
    if (dto == null) {
      return null;
    }
    final MetricApi metricApi = optional(dto.getMetric())
        .map(metric -> new MetricApi().setName(metric))
        .orElse(null);

    final DatasetApi datasetApi = optional(dto.getCollection())
        .map(datasetName -> new DatasetApi().setName(datasetName))
        .orElse(null);

    final AnomalyApi anomalyApi = new AnomalyApi()
        .setId(dto.getId())
        .setStartTime(new Date(dto.getStartTime()))
        .setEndTime(new Date(dto.getEndTime()))
        .setCreated(new Date(dto.getCreatedTime()))
        .setAvgCurrentVal(dto.getAvgCurrentVal())
        .setAvgBaselineVal(dto.getAvgBaselineVal())
        .setScore(dto.getScore())
        .setWeight(dto.getWeight())
        .setImpactToGlobal(dto.getImpactToGlobal())
        .setSourceType(dto.getAnomalyResultSource())
        .setNotified(dto.isNotified())
        .setMessage(dto.getMessage())
        .setMetric(metricApi)
        .setMetadata(new AlertMetadataApi()
            .setMetric(metricApi)
            .setDataset(datasetApi)
        )
        .setEnumerationItem(EnumerationItemMapper.INSTANCE.toApi(dto.getEnumerationItem()))
        .setFeedback(toAnomalyFeedbackApi(dto));

    if (dto.getMetricUrn() != null) {
      anomalyApi
          .setMetric(toMetricApi(dto.getMetricUrn())
              .setName(dto.getMetric())
              .setDataset(datasetApi)
          );
    }

    if (dto.getAnomalyLabels() != null) {
      anomalyApi.setAnomalyLabels(dto.getAnomalyLabels()
          .stream()
          .map(ApiBeanMapper::toApi)
          .collect(Collectors.toList()));
    }

    anomalyApi.setAlert(new AlertApi()
            .setId(dto.getDetectionConfigId())
        )
        .setAlertNode(optional(dto.getProperties())
            .map(p -> p.get("detectorComponentName"))
            .map(AnomalyMapper::toDetectionAlertNodeApi)
            .orElse(null));
    return anomalyApi;
  }

  private static AnomalyFeedbackApi toAnomalyFeedbackApi(final MergedAnomalyResultDTO dto) {
    if (dto.getFeedback() != null) {
      return ApiBeanMapper.toApi((AnomalyFeedbackDTO) dto.getFeedback());
    } else if (dto.getAnomalyFeedbackId() != null) {
      return new AnomalyFeedbackApi().setId(dto.getAnomalyFeedbackId());
    }
    return null;
  }
}
