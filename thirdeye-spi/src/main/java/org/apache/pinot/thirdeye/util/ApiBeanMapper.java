package org.apache.pinot.thirdeye.util;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.pinot.thirdeye.datalayer.pojo.AlertNodeType.DETECTION;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.anomalydetection.context.AnomalyFeedback;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertNodeApi;
import org.apache.pinot.thirdeye.api.AnomalyApi;
import org.apache.pinot.thirdeye.api.AnomalyFeedbackApi;
import org.apache.pinot.thirdeye.api.ApplicationApi;
import org.apache.pinot.thirdeye.api.DatasetApi;
import org.apache.pinot.thirdeye.api.EmailSchemeApi;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.api.NotificationSchemesApi;
import org.apache.pinot.thirdeye.api.SubscriptionGroupApi;
import org.apache.pinot.thirdeye.api.TimeColumnApi;
import org.apache.pinot.thirdeye.api.UserApi;
import org.apache.pinot.thirdeye.common.time.TimeGranularity;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.datalayer.pojo.AlertNode;
import org.apache.pinot.thirdeye.datalayer.pojo.ApplicationBean;
import org.apache.pinot.thirdeye.datalayer.pojo.MetricConfigBean;

public abstract class ApiBeanMapper {

  private static Boolean boolApi(final boolean value) {
    return value ? true : null;
  }

  public static ApplicationApi toApi(final ApplicationBean o) {
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
        .setDataSource(dto.getDataSource())
        ;
  }

  public static MetricApi toApi(final MetricConfigBean dto) {
    return new MetricApi()
        .setId(dto.getId())
        .setActive(boolApi(dto.isActive()))
        .setName(dto.getName())
        .setUpdated(dto.getUpdateTime())
        .setDataset(new DatasetApi()
            .setName(dto.getDataset())
        )
        .setDerived(boolApi(dto.isDerived()))
        .setDerivedMetricExpression(dto.getDerivedMetricExpression())
        .setWhere(dto.getWhere())
        .setAggregationColumn(dto.getAggregationColumn())
        .setAggregationFunction(dto.getDefaultAggFunction())
        .setRollupThreshold(dto.getRollupThreshold())
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
        .setLastTimestamp(new Date(dto.getLastTimestamp()))
        .setOwner(new UserApi()
            .setPrincipal(dto.getCreatedBy()))
        ;
  }

  private static Map<String, AlertNodeApi> toAlertNodeApiMap(
      final Map<String, AlertNode> nodes) {
    Map<String, AlertNodeApi> map = new HashMap<>(nodes.size());
    for (Map.Entry<String, AlertNode> e : nodes.entrySet()) {
      map.put(e.getKey(), toAlertNodeApi(e.getValue()));
    }
    return map;
  }

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

  public static Map<String, AlertNode> toAlertNodeMap(
      final Map<String, AlertNodeApi> nodes) {
    Map<String, AlertNode> map = new HashMap<>(nodes.size());
    for (Map.Entry<String, AlertNodeApi> e : nodes.entrySet()) {
      map.put(e.getKey(), toAlertNode(e.getValue()));
    }
    return map;
  }

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
    optional(api.getDataSource()).ifPresent(dto::setDataSource);
    dto.setDataset(api.getName());
    dto.setDisplayName(api.getName());
    optional(api.getDimensions()).ifPresent(dto::setDimensions);
    optional(api.getTimeColumn()).ifPresent(timeColumn -> {
      dto.setTimeColumn(timeColumn.getName());
      dto.setTimeDuration(TimeGranularity.fromDuration(timeColumn.getInterval()).getSize());
      dto.setTimeUnit(TimeUnit.MILLISECONDS);
      optional(timeColumn.getFormat()).ifPresent(dto::setTimeFormat);
      optional(timeColumn.getTimezone()).ifPresent(dto::setTimezone);
    });

    return dto;
  }

  public static MetricConfigDTO toMetricConfigDto(final MetricApi api) {
    final MetricConfigDTO dto = new MetricConfigDTO();

    dto.setId(api.getId());
    dto
        .setName(api.getName())
        .setDataset(optional(api.getDataset())
            .map(DatasetApi::getName)
            .orElse(null))
        .setRollupThreshold(api.getRollupThreshold())
        .setAggregationColumn(api.getAggregationColumn())
        .setDefaultAggFunction(api.getAggregationFunction())
        // TODO suvodeep Revisit this: Assume false if active is not set.
        .setActive(optional(api.getActive()).orElse(false))
        .setViews(api.getViews())
        .setWhere(api.getWhere())
        .setDerived(optional(api.getDerived()).orElse(false))
        .setDerivedMetricExpression(api.getDerivedMetricExpression());

    return dto;
  }

  private static AlertNodeApi toDetectionAlertNodeApi(final String detectorComponentName) {
    final String[] splitted = detectorComponentName.split(":");
    checkState(splitted.length == 2);

    return new AlertNodeApi()
        .setName(splitted[0])
        .setType(DETECTION)
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

    // TODO spyne This entire bean to be refactored, current optimistic conversion is a hack.
    final EmailSchemeApi emailSchemeApi = optional(dto.getAlertSchemes())
        .map(o -> o.get("emailScheme"))
        .map(o -> ((Map) o).get("recipients"))
        .map(m -> (Map) m)
        .map(m -> new EmailSchemeApi()
            .setTo(optional(m.get("to"))
                .map(l -> new ArrayList<>((List<String>) l))
                .orElse(null)
            )
            .setCc(optional(m.get("cc"))
                .map(l -> new ArrayList<>((List<String>) l))
                .orElse(null)
            )
            .setBcc(optional(m.get("bcc"))
                .map(l -> new ArrayList<>((List<String>) l))
                .orElse(null)
            ))
        .orElse(null);

    return new SubscriptionGroupApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setApplication(new ApplicationApi()
            .setName(dto.getApplication()))
        .setAlerts(alertApis)
        .setNotificationSchemes(new NotificationSchemesApi()
            .setEmail(emailSchemeApi))
        ;
  }

  public static SubscriptionGroupDTO toSubscriptionGroupDTO(final SubscriptionGroupApi api) {
    final SubscriptionGroupDTO dto = new SubscriptionGroupDTO();
    dto.setId(api.getId());
    dto.setName(api.getName());

    optional(api.getApplication())
        .map(ApplicationApi::getName)
        .ifPresent(dto::setApplication);

    // TODO spyne implement translation of alert schemes, suppressors etc.

    dto.setProperties(new HashMap<>());

    final List<Long> alertIds = optional(api.getAlerts())
        .orElse(Collections.emptyList())
        .stream()
        .map(AlertApi::getId)
        .collect(Collectors.toList());

    dto.getProperties().put("detectionConfigIds", alertIds);

    if (api.getNotificationSchemes() != null) {
      dto.setAlertSchemes(toAlertSchemes(api.getNotificationSchemes()));
    }

    return dto;
  }

  public static ImmutableMap<String, Object> toAlertSchemes(
      final NotificationSchemesApi notificationSchemes) {
    final EmailSchemeApi email = notificationSchemes.getEmail();
    return ImmutableMap.of(
        "emailScheme", ImmutableMap.of(
            "recipients", ImmutableMap.of(
                "to", optional(email.getTo()).orElse(Collections.emptyList()),
                "cc", optional(email.getCc()).orElse(Collections.emptyList()),
                "bcc", optional(email.getBcc()).orElse(Collections.emptyList())
            )));
  }

  public static AnomalyApi toApi(final MergedAnomalyResultDTO dto) {
    return new AnomalyApi()
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
        .setMetric(toMetricApi(dto.getMetricUrn())
            .setName(dto.getMetric())
            .setDataset(new DatasetApi()
                .setName(dto.getCollection())
            )
        )
        .setAlert(new AlertApi()
            .setId(dto.getDetectionConfigId())
            .setName(optional(dto.getProperties())
                .map(p -> p.get("subEntityName"))
                .orElse(null))
        )
        .setAlertNode(optional(dto.getProperties())
            .map(p -> p.get("detectorComponentName"))
            .map(ApiBeanMapper::toDetectionAlertNodeApi)
            .orElse(null))
        .setFeedback(optional(dto.getFeedback())
            .map(ApiBeanMapper::toApi)
            .orElse(null))
        ;
  }

  private static AnomalyFeedbackApi toApi(final AnomalyFeedback feedbackDto) {
    return new AnomalyFeedbackApi()
        .setComment(feedbackDto.getComment())
        .setType(feedbackDto.getFeedbackType());
  }
}
