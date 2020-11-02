package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.CoreConstants.ONBOARDING_REPLAY_LOOKBACK;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.anomaly.task.TaskConstants;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertComponentApi;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.datalayer.util.Predicate;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.TaskUtils;
import org.apache.pinot.thirdeye.detection.onboard.YamlOnboardingTaskInfo;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricAttributeHolder;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricProperties;
import org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionPropertiesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertCreater {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertCreater.class);

  private final DataProvider dataProvider;
  private final AlertManager alertManager;
  private final TaskManager taskManager;

  @Inject
  public AlertCreater(
      final DataProvider dataProvider,
      final AlertManager alertManager,
      final TaskManager taskManager) {
    this.dataProvider = dataProvider;
    this.alertManager = alertManager;
    this.taskManager = taskManager;
  }

  public AlertDTO create(AlertApi api) {
    ensure(alertManager
        .findByPredicate(Predicate.EQ("name", api.getName()))
        .isEmpty(), ERR_DUPLICATE_NAME);

    final AlertDTO dto = toAlertDTO(api);

    final Long id = alertManager.save(dto);
    dto.setId(id);

    createOnboardingTask(dto, 0, 0);
    return dto;
  }

  private Map<String, Object> buildDetectionProperties(final AlertApi api,
      final DetectionMetricAttributeHolder metricAttributesMap) {

    final DetectionPropertiesBuilder detectionTranslatorBuilder =
        new DetectionPropertiesBuilder(metricAttributesMap, dataProvider);

    final String name = api.getDetections().keySet().iterator().next();
    final AlertComponentApi component = api.getDetections().get(name);

    final String key = metricAttributesMap.loadMetricCache(
        component.getMetric().getName(),
        component.getMetric().getDataset().getName(),
        api.getCron());

    final DetectionMetricProperties detectionMetricProperties = metricAttributesMap
        .getDetectionMetricProperties(key);
    final MetricConfigDTO metricConfigDTO = detectionMetricProperties.getMetricConfigDTO();
    final DatasetConfigDTO datasetConfigDTO = detectionMetricProperties.getDatasetConfigDTO();

    return detectionTranslatorBuilder
        .buildMetricAlertExecutionPlan(
            metricConfigDTO,
            datasetConfigDTO,
            name,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            false,
            toRuleYamls(name, component),
            Collections.emptyList()
        );
  }

  private List<Map<String, Object>> toRuleYamls(final String name,
      final AlertComponentApi component) {
    final LinkedHashMap<String, Object> ruleMap = new LinkedHashMap<>();
    ruleMap.put("name", name);
    ruleMap.put("type", component.getType());
    ruleMap.put("params", component.getParams());

    final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("detection", Collections.singletonList(ruleMap));

    final List<Map<String, Object>> ruleYamls = Collections.singletonList(map);
    return ruleYamls;
  }

  public AlertDTO toAlertDTO(final AlertApi api) {
    final AlertDTO dto = new AlertDTO();

    dto.setName(api.getName());
    dto.setDescription(api.getDescription());
    dto.setActive(true);
    dto.setCron(api.getCron());
    dto.setLastTimestamp(optional(api.getLastTimestamp())
        .map(d -> d.toInstant().toEpochMilli())
        .orElse(0L));
    dto.setUpdateTime(new Timestamp(System.currentTimeMillis()));
    dto.setCreatedBy(api.getOwner().getPrincipal());

    final DetectionMetricAttributeHolder metricAttributesMap =
        new DetectionMetricAttributeHolder(dataProvider);

    dto.setProperties(buildDetectionProperties(api, metricAttributesMap));
    dto.setComponentSpecs(metricAttributesMap.getAllComponents());

    return dto;
  }

  private void createOnboardingTask(
      final AlertDTO alertDTO,
      long tuningWindowStart,
      long tuningWindowEnd
  ) {
    YamlOnboardingTaskInfo info = new YamlOnboardingTaskInfo();
    info.setConfigId(alertDTO.getId());
    if (tuningWindowStart == 0L && tuningWindowEnd == 0L) {
      // default tuning window 28 days
      tuningWindowEnd = System.currentTimeMillis();
      tuningWindowStart = tuningWindowEnd - TimeUnit.DAYS.toMillis(28);
    }
    info.setTuningWindowStart(tuningWindowStart);
    info.setTuningWindowEnd(tuningWindowEnd);
    info.setEnd(System.currentTimeMillis());

    long lastTimestamp = alertDTO.getLastTimestamp();
    // If no value is present, set the default lookback
    if (lastTimestamp < 0) {
      lastTimestamp = info.getEnd() - ONBOARDING_REPLAY_LOOKBACK;
    }
    info.setStart(lastTimestamp);

    String taskInfoJson;
    try {
      taskInfoJson = new ObjectMapper().writeValueAsString(info);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Error while serializing %s: %s",
          YamlOnboardingTaskInfo.class.getSimpleName(), info), e);
    }

    TaskDTO taskDTO = TaskUtils.buildTask(alertDTO.getId(), taskInfoJson,
        TaskConstants.TaskType.YAML_DETECTION_ONBOARD);
    long taskId = taskManager.save(taskDTO);
    LOG.info("Created {} task {} with taskId {}", TaskConstants.TaskType.YAML_DETECTION_ONBOARD,
        taskDTO, taskId);
  }
}
