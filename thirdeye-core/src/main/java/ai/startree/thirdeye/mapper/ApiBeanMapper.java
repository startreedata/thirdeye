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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.api.AnomalyLabelApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.api.NotificationSchemesApi;
import ai.startree.thirdeye.spi.api.NotificationSpecApi;
import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.api.TimeWindowSuppressorApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSpecDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ApiBeanMapper {

  private static final String DEFAULT_ALERTER_PIPELINE = "DEFAULT_ALERTER_PIPELINE";

  public static DataSourceApi toApi(final DataSourceDTO dto) {
    return DataSourceMapper.INSTANCE.toApi(dto);
  }

  public static DataSourceDTO toDataSourceDto(final DataSourceApi api) {
    return DataSourceMapper.INSTANCE.toBean(api);
  }

  public static DatasetApi toApi(final DatasetConfigDTO dto) {
    return DatasetMapper.INSTANCE.toApi(dto);
  }

  public static MetricApi toApi(final MetricConfigDTO dto) {
    return MetricMapper.INSTANCE.toApi(dto);
  }

  public static AlertApi toApi(final AlertDTO dto) {
    return AlertMapper.INSTANCE.toApi(dto);
  }

  public static AlertDTO toAlertDto(final AlertApi alertApi) {
    return AlertMapper.INSTANCE.toAlertDTO(alertApi);
  }

  public static AlertTemplateApi toAlertTemplateApi(final AlertTemplateDTO alertTemplateDTO) {
    return AlertTemplateMapper.INSTANCE.toApi(alertTemplateDTO);
  }

  public static DatasetConfigDTO toDatasetConfigDto(final DatasetApi api) {
    return DatasetMapper.INSTANCE.toBean(api);
  }

  public static MetricConfigDTO toMetricConfigDto(final MetricApi api) {
    return MetricMapper.INSTANCE.toBean(api);
  }

  public static MetricApi toMetricApi(final String metricUrn) {
    final String[] parts = metricUrn.split(":");
    checkState(parts.length >= 3);
    return new MetricApi()
        .setId(Long.parseLong(parts[2]))
        .setUrn(metricUrn);
  }

  public static SubscriptionGroupApi toApi(final SubscriptionGroupDTO dto) {
    final List<AlertApi> alertApis = optional(dto.getProperties())
        .map(o1 -> o1.get("detectionConfigIds"))
        .map(l -> ((List<Number>) l).stream()
            .map(Number::longValue)
            .map(o -> new AlertApi().setId(o))
            .collect(Collectors.toList()))
        .orElse(null);

    final List<NotificationSpecApi> specs = optional(dto.getSpecs())
        .map(l -> l.stream()
            .map(ApiBeanMapper::toApi)
            .collect(Collectors.toList())
        )
        .orElse(null);

    return new SubscriptionGroupApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setCron(dto.getCronExpression())
        .setAlerts(alertApis)
        .setSpecs(specs)
        .setNotificationSchemes(toApi(dto.getNotificationSchemes()));
  }

  public static SubscriptionGroupDTO toSubscriptionGroupDTO(final SubscriptionGroupApi api) {
    final SubscriptionGroupDTO dto = new SubscriptionGroupDTO();
    dto.setId(api.getId());
    dto.setName(api.getName());
    dto.setActive(optional(api.getActive()).orElse(true));

    // TODO spyne implement translation of alert schemes, suppressors etc.

    dto.setType(optional(api.getType()).orElse(DEFAULT_ALERTER_PIPELINE));
    dto.setProperties(new HashMap<>());

    final List<Long> alertIds = optional(api.getAlerts())
        .orElse(Collections.emptyList())
        .stream()
        .map(AlertApi::getId)
        .collect(Collectors.toList());

    dto.getProperties().put("detectionConfigIds", alertIds);
    dto.setCronExpression(api.getCron());

    optional(api.getSpecs())
        .map(l -> l.stream()
            .map(ApiBeanMapper::toNotificationSpecDto)
            .collect(Collectors.toList())
        )
        .ifPresent(dto::setSpecs);

    optional(api.getNotificationSchemes())
        .map(ApiBeanMapper::toNotificationSchemeDto)
        .ifPresent(dto::setNotificationSchemes);

    dto.setVectorClocks(toVectorClocks(alertIds));
    dto.setAlertSuppressors(toAlertSuppressors(api.getAlertSuppressors()));
    return dto;
  }

  private static Map<Long, Long> toVectorClocks(List<Long> detectionIds) {
    long currentTimestamp = 0L;
    Map<Long, Long> vectorClocks = new HashMap<>();
    for (long detectionConfigId : detectionIds) {
      vectorClocks.put(detectionConfigId, currentTimestamp);
    }
    return vectorClocks;
  }

  private static NotificationSpecDTO toNotificationSpecDto(final NotificationSpecApi api) {
    return NotificationSpecMapper.INSTANCE.toBean(api);
  }

  private static NotificationSpecApi toApi(final NotificationSpecDTO dto) {
    return NotificationSpecMapper.INSTANCE.toApi(dto);
  }

  public static NotificationSchemesDto toNotificationSchemeDto(
      final NotificationSchemesApi notificationSchemesApi) {
    return NotificationSchemeMapper.INSTANCE.toDto(notificationSchemesApi);
  }

  public static NotificationSchemesApi toApi(
      NotificationSchemesDto notificationSchemesDto) {
    return NotificationSchemeMapper.INSTANCE.toApi(notificationSchemesDto);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> toAlertSuppressors(
      final TimeWindowSuppressorApi timeWindowSuppressorApi) {
    Map<String, Object> alertSuppressors = new HashMap<>();
    if (timeWindowSuppressorApi != null) {
      alertSuppressors = new ObjectMapper().convertValue(timeWindowSuppressorApi, Map.class);
    }
    //alertSuppressors.put(PROP_CLASS_NAME, DEFAULT_ALERT_SUPPRESSOR);
    return alertSuppressors;
  }

  public static AnomalyApi toApi(final MergedAnomalyResultDTO dto) {
    return AnomalyMapper.INSTANCE.toApi(dto);
  }

  public static MergedAnomalyResultDTO toDto(final AnomalyApi api) {
    return AnomalyMapper.INSTANCE.toDto(api);
  }

  public static AnomalyFeedbackApi toApi(final AnomalyFeedback feedbackDto) {
    return new AnomalyFeedbackApi()
        .setComment(feedbackDto.getComment())
        .setType(feedbackDto.getFeedbackType());
  }

  public static AnomalyFeedbackDTO toAnomalyFeedbackDTO(AnomalyFeedbackApi api) {
    return new AnomalyFeedbackDTO()
        .setFeedbackType(api.getType())
        .setComment(api.getComment());
  }

  public static AlertTemplateDTO toAlertTemplateDto(final AlertTemplateApi api) {
    return AlertTemplateMapper.INSTANCE.toBean(api);
  }

  public static AnomalyLabelDTO toDto(final AnomalyLabelApi api) {
    return AnomalyLabelMapper.INSTANCE.toDto(api);
  }

  public static AnomalyLabelApi toApi(final AnomalyLabelDTO dto) {
    return AnomalyLabelMapper.INSTANCE.toApi(dto);
  }

  public static TaskDTO toTaskDto(TaskApi api) {
    return TaskMapper.INSTANCE.toDto(api);
  }

  public static TaskApi toApi(TaskDTO dto) {
    return TaskMapper.INSTANCE.toApi(dto);
  }

  public static EventApi toApi(final EventDTO dto) {
    return EventMapper.INSTANCE.toApi(dto);
  }

  public static EventDTO toEventDto(final EventApi api) {
    return EventMapper.INSTANCE.toDto(api);
  }

  public static RcaInvestigationApi toApi(final RcaInvestigationDTO dto) {
    return RcaInvestigationMapper.INSTANCE.toApi(dto);
  }

  public static RcaInvestigationDTO toDto(final RcaInvestigationApi api) {
    return RcaInvestigationMapper.INSTANCE.toDto(api);
  }

  public static EnumerationItemDTO toEnumerationItemDTO(final EnumerationItemApi api) {
    return EnumerationItemMapper.INSTANCE.toDto(api);
  }

  public static EnumerationItemApi toApi(final EnumerationItemDTO dto) {
    return EnumerationItemMapper.INSTANCE.toApi(dto);
  }
}
