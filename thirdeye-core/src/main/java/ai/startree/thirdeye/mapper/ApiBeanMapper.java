/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.mapper;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertNodeApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DataSourceMetaApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.EventApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.api.NotificationSchemesApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.api.TimeWindowSuppressorApi;
import ai.startree.thirdeye.spi.api.UserApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertNodeType;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ApiBeanMapper {

  private static final String DEFAULT_ALERTER_PIPELINE = "DEFAULT_ALERTER_PIPELINE";

  private static Boolean boolApi(final boolean value) {
    return value ? true : null;
  }

  public static DataSourceApi toApi(final DataSourceDTO dto) {
    return new DataSourceApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setProperties(dto.getProperties())
        .setType(dto.getType())
        .setMetaList(optional(dto.getMetaList())
            .map(l -> l.stream().map(ApiBeanMapper::toApi)
                .collect(Collectors.toList()))
            .orElse(null));
  }

  private static DataSourceMetaApi toApi(final DataSourceMetaBean metaBean) {
    return new DataSourceMetaApi()
        .setClassRef(metaBean.getClassRef())
        .setProperties(metaBean.getProperties());
  }

  public static DataSourceDTO toDataSourceDto(final DataSourceApi api) {
    final DataSourceDTO dto = new DataSourceDTO();
    dto
        .setName(api.getName())
        .setProperties(api.getProperties())
        .setType(api.getType())
        .setMetaList(optional(api.getMetaList())
            .map(l -> l.stream().map(ApiBeanMapper::toDataSourceMetaBean)
                .collect(Collectors.toList()))
            .orElse(null));
    dto.setId(api.getId());
    return dto;
  }

  private static DataSourceMetaBean toDataSourceMetaBean(final DataSourceMetaApi api) {
    return new DataSourceMetaBean()
        .setClassRef(api.getClassRef())
        .setProperties(api.getProperties());
  }

  public static DatasetApi toApi(final DatasetConfigDTO dto) {
    return DatasetMapper.INSTANCE.toApi(dto);
  }

  public static MetricApi toApi(final MetricConfigDTO dto) {
    return MetricMapper.INSTANCE.toApi(dto);
  }

  public static AlertApi toApi(final AlertDTO dto) {
    return new AlertApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setActive(dto.isActive())
        .setCron(dto.getCron())
        .setTemplate(optional(dto.getTemplate())
            .map(ApiBeanMapper::toAlertTemplateApi)
            .orElse(null))
        .setTemplateProperties(dto.getTemplateProperties())
        // keep lastTimestamp internal - TE-340
        //.setLastTimestamp(new Date(dto.getLastTimestamp()))
        .setOwner(new UserApi()
            .setPrincipal(dto.getCreatedBy()))
        ;
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

  private static AlertNodeApi toDetectionAlertNodeApi(final String detectorComponentName) {
    final String[] splitted = detectorComponentName.split(":");
    checkState(splitted.length == 2);

    return new AlertNodeApi()
        .setName(splitted[0])
        .setType(AlertNodeType.DETECTION)
        .setSubType(splitted[1]);
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

    return new SubscriptionGroupApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setCron(dto.getCronExpression())
        .setAlerts(alertApis)
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

    if (api.getNotificationSchemes() != null) {
      dto.setNotificationSchemes(toNotificationSchemeDto(api.getNotificationSchemes()));
    }

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
    AnomalyApi anomalyApi = new AnomalyApi()
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
        .setMessage(dto.getMessage());
    if (dto.getMetricUrn() != null) {
      anomalyApi
          .setMetric(toMetricApi(dto.getMetricUrn())
              .setName(dto.getMetric())
              .setDataset(new DatasetApi()
                  .setName(dto.getCollection())
              )
          );
    }
    anomalyApi.setAlert(new AlertApi()
            .setId(dto.getDetectionConfigId())
        )
        .setAlertNode(optional(dto.getProperties())
            .map(p -> p.get("detectorComponentName"))
            .map(ApiBeanMapper::toDetectionAlertNodeApi)
            .orElse(null))
        .setFeedback(optional(dto.getFeedback())
            .map(ApiBeanMapper::toApi)
            .orElse(null));
    return anomalyApi;
  }

  private static AnomalyFeedbackApi toApi(final AnomalyFeedback feedbackDto) {
    return new AnomalyFeedbackApi()
        .setComment(feedbackDto.getComment())
        .setType(feedbackDto.getFeedbackType());
  }

  public static AlertTemplateDTO toAlertTemplateDto(final AlertTemplateApi api) {
    return AlertTemplateMapper.INSTANCE.toBean(api);
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
}
