package org.apache.pinot.thirdeye.util;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.pinot.thirdeye.datalayer.pojo.AlertNodeType.DETECTION;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertNodeApi;
import org.apache.pinot.thirdeye.api.AnomalyApi;
import org.apache.pinot.thirdeye.api.ApplicationApi;
import org.apache.pinot.thirdeye.api.DatasetApi;
import org.apache.pinot.thirdeye.api.EmailSettingsApi;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.api.SubscriptionGroupApi;
import org.apache.pinot.thirdeye.api.TimeColumnApi;
import org.apache.pinot.thirdeye.api.UserApi;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.datalayer.pojo.AlertNode;
import org.apache.pinot.thirdeye.datalayer.pojo.ApplicationBean;

public abstract class ApiBeanMapper {

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
        ;
  }

  public static MetricApi toApi(final MetricConfigDTO dto) {
    return new MetricApi()
        .setId(dto.getId())
        .setActive(dto.isActive())
        .setName(dto.getName())
        .setUpdated(dto.getUpdateTime())
        .setDataset(new DatasetApi()
            .setName(dto.getDataset())
        )
        ;
  }

  public static AlertApi toApi(final AlertDTO dto) {
    return new AlertApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setDescription(dto.getDescription())
        .setActive(dto.isActive())
        .setCron(dto.getCron())
        .setNodes(toAlertNodeApiMap(dto.getNodes()))
        .setLastTimestamp(new Date(dto.getLastTimestamp()))
        .setOwner(new UserApi()
            .setPrincipal(dto.getCreatedBy()))
        ;
  }

  private static Map<String, AlertNodeApi> toAlertNodeApiMap(
      final Map<String, AlertNode> nodes) {
    Map<String, AlertNodeApi> map = new HashMap<>(nodes.size());
    for (Map.Entry<String, AlertNode> e : nodes.entrySet()) {
      map.put(e.getKey(), toNodeApi(e.getValue()));
    }
    return map;
  }

  private static AlertNodeApi toNodeApi(final AlertNode dto) {
    return new AlertNodeApi()
        .setName(dto.getName())
        .setType(dto.getType())
        .setSubType(dto.getSubType())
        .setDependsOn(dto.getDependsOn())
        .setParams(dto.getParams())
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
        ;
  }

  private static Map<String, AlertNodeApi> toNodeMap(
      final Map<String, Object> componentSpecs) {
    if (componentSpecs == null) {
      return null;
    }
    Map<String, AlertNodeApi> map = new HashMap<>(componentSpecs.size());
    for (Map.Entry<String, Object> e : componentSpecs.entrySet()) {

      final Map<String, Object> valueMap = (Map<String, Object>) e.getValue();
      final String metricUrn = (String) valueMap.get("metricUrn");
      valueMap.remove("metricUrn");
      valueMap.remove("className");

      final AlertNodeApi alertComponentApi = toDetectionAlertNodeApi(e.getKey());
      map.put(alertComponentApi.getName(), alertComponentApi
          .setParams(valueMap)
          .setMetric(toMetricApi(metricUrn))
      );
    }

    return map;
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
    checkState(parts.length == 3);
    return new MetricApi()
        .setId(Long.parseLong(parts[2]))
        .setUrn(metricUrn);
  }

  public static SubscriptionGroupApi toApi(final SubscriptionGroupDTO dto) {
    final List<AlertApi> alertApis = optional(dto.getProperties())
        .map(o1 -> o1.get("detectionConfigIds"))
        .map(l -> ((List<Integer>) l).stream()
            .map(o -> new AlertApi().setId(Long.valueOf(o)))
            .collect(Collectors.toList()))
        .orElse(null);

    // TODO spyne This entire bean to be refactored, current optimistic conversion is a hack.
    final EmailSettingsApi emailSettings = optional(dto.getAlertSchemes())
        .map(o -> o.get("emailScheme"))
        .map(o -> ((Map) o).get("recipients"))
        .map(m -> (Map) m)
        .map(m -> new EmailSettingsApi()
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
        .setEmailSettings(emailSettings)
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

    return dto;
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
        ;
  }
}
