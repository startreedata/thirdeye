package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertComponentApi;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricAttributeHolder;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricProperties;
import org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionPropertiesBuilder;

@Singleton
public class AlertCreater {

  private final DataProvider dataProvider;
  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final AlertManager alertManager;

  @Inject
  public AlertCreater(
      final DataProvider dataProvider,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final AlertManager alertManager) {
    this.dataProvider = dataProvider;
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.alertManager = alertManager;
  }

  public Long create(AlertApi api) {
    final AlertDTO dto = toAlertDTO(api);

    final DetectionMetricAttributeHolder metricAttributesMap =
        new DetectionMetricAttributeHolder(dataProvider);

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

    final Map<String, Object> detectionProperties = detectionTranslatorBuilder
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

    dto.setProperties(detectionProperties);


    final Long id = alertManager.save(dto);
    return id;
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

  private AlertDTO toAlertDTO(final AlertApi api) {
    final AlertDTO dto = new AlertDTO();

    dto.setName(api.getName());
    dto.setDescription(api.getDescription());
    dto.setActive(true);
    dto.setCron(api.getCron());
    dto.setLastTimestamp(optional(api.getLastTimestamp())
        .map(d -> d.toInstant().toEpochMilli())
        .orElse(0L));
    dto.setUpdateTime(new Timestamp(System.currentTimeMillis()));

    return dto;
  }
}
