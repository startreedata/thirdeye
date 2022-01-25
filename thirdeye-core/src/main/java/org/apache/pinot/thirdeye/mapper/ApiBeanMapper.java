package org.apache.pinot.thirdeye.mapper;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertNodeApi;
import org.apache.pinot.thirdeye.spi.api.AlertTemplateApi;
import org.apache.pinot.thirdeye.spi.api.AnomalyApi;
import org.apache.pinot.thirdeye.spi.api.AnomalyFeedbackApi;
import org.apache.pinot.thirdeye.spi.api.ApplicationApi;
import org.apache.pinot.thirdeye.spi.api.DataSourceApi;
import org.apache.pinot.thirdeye.spi.api.DataSourceMetaApi;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.apache.pinot.thirdeye.spi.api.EventApi;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.apache.pinot.thirdeye.spi.api.NotificationSchemesApi;
import org.apache.pinot.thirdeye.spi.api.SubscriptionGroupApi;
import org.apache.pinot.thirdeye.spi.api.TaskApi;
import org.apache.pinot.thirdeye.spi.api.TimeColumnApi;
import org.apache.pinot.thirdeye.spi.api.TimeWindowSuppressorApi;
import org.apache.pinot.thirdeye.spi.api.UserApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertNode;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertNodeType;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EventDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyFeedback;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.util.SpiUtils;

public abstract class ApiBeanMapper {

  private static final String DEFAULT_ALERTER_PIPELINE_CLASS_NAME = "org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter";
  private static final String DEFAULT_ALERTER_PIPELINE = "DEFAULT_ALERTER_PIPELINE";
  private static final String PROP_CLASS_NAME = "className";

  private static Boolean boolApi(final boolean value) {
    return value ? true : null;
  }

  public static ApplicationApi toApi(final ApplicationDTO o) {
    return new ApplicationApi()
        .setId(o.getId())
        .setName(o.getApplication())
        ;
  }

  public static ApplicationDTO toApplicationDto(final ApplicationApi applicationApi) {
    final ApplicationDTO applicationDTO = new ApplicationDTO();
    applicationDTO.setApplication(applicationApi.getName());
    applicationDTO.setRecipients("");
    return applicationDTO;
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

  public static MetricApi toApi(final MetricConfigDTO dto) {
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

  public static AlertApi toApi(final AlertDTO dto) {
    return new AlertApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setActive(dto.isActive())
        .setCron(dto.getCron())
        .setNodes(optional(dto.getNodes())
            .map(ApiBeanMapper::toAlertNodeApiMap)
            .orElse(null))
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

  @Deprecated
  private static Map<String, AlertNodeApi> toAlertNodeApiMap(
      final Map<String, AlertNode> nodes) {
    Map<String, AlertNodeApi> map = new HashMap<>(nodes.size());
    for (Map.Entry<String, AlertNode> e : nodes.entrySet()) {
      map.put(e.getKey(), toAlertNodeApi(e.getValue()));
    }
    return map;
  }

  @Deprecated
  private static AlertNodeApi toAlertNodeApi(final AlertNode dto) {
    return new AlertNodeApi()
        .setName(dto.getName())
        .setType(dto.getType())
        .setSubType(dto.getSubType())
        .setDependsOn(dto.getDependsOn())
        .setParams(dto.getParams())
        .setMetric(optional(dto.getMetric())
            .map(ApiBeanMapper::toApi)
            .map(m -> m // TODO suvodeep fix hack. The
                .setRollupThreshold(null)
                .setAggregationFunction(null)
                .setActive(null))
            .orElse(null))
        ;
  }

  @Deprecated
  public static Map<String, AlertNode> toAlertNodeMap(
      final Map<String, AlertNodeApi> nodes) {
    Map<String, AlertNode> map = new HashMap<>(nodes.size());
    for (Map.Entry<String, AlertNodeApi> e : nodes.entrySet()) {
      map.put(e.getKey(), toAlertNode(e.getValue()));
    }
    return map;
  }

  @Deprecated
  public static AlertNode toAlertNode(final AlertNodeApi api) {
    return new AlertNode()
        .setName(api.getName())
        .setType(api.getType())
        .setSubType(api.getSubType())
        .setDependsOn(api.getDependsOn())
        .setParams(api.getParams())
        .setMetric(optional(api.getMetric())
            .map(ApiBeanMapper::toMetricConfigDto)
            .orElse(null)
        )
        ;
  }

  public static DatasetConfigDTO toDatasetConfigDto(final DatasetApi api) {
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

  public static MetricConfigDTO toMetricConfigDto(final MetricApi api) {
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
        .setApplication(new ApplicationApi()
            .setName(dto.getApplication()))
        .setAlerts(alertApis)
        .setNotificationSchemes(toApi(dto.getNotificationSchemes()));
  }

  public static SubscriptionGroupDTO toSubscriptionGroupDTO(final SubscriptionGroupApi api) {
    final SubscriptionGroupDTO dto = new SubscriptionGroupDTO();
    dto.setId(api.getId());
    dto.setName(api.getName());
    dto.setActive(optional(api.getActive()).orElse(true));

    optional(api.getApplication())
        .map(ApplicationApi::getName)
        .ifPresent(dto::setApplication);

    // TODO spyne implement translation of alert schemes, suppressors etc.

    dto.setType(optional(api.getType()).orElse(DEFAULT_ALERTER_PIPELINE));
    dto.setProperties(buildProperties());

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

  private static Map<String, Object> buildProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(PROP_CLASS_NAME, DEFAULT_ALERTER_PIPELINE_CLASS_NAME);
    return properties;
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
