package org.apache.pinot.thirdeye.alert;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertComponentApi;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricAttributeHolder;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricProperties;
import org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionPropertiesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertApiBeanMapper {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertApiBeanMapper.class);

  private final DataProvider dataProvider;

  @Inject
  public AlertApiBeanMapper(
      final DataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  private Map<String, Object> buildDetectionProperties(final AlertApi api,
      final DetectionMetricAttributeHolder metricAttributesMap) {

    final DetectionPropertiesBuilder detectionTranslatorBuilder =
        new DetectionPropertiesBuilder(metricAttributesMap, dataProvider);

    // TODO Enhancement. Metrics can be on a per detection basis. Use the first one for now.
    MetricConfigDTO metricConfigDTO = null;
    DatasetConfigDTO datasetConfigDTO = null;

    final List<Map<String, Object>> listOfDetectionMaps = new ArrayList<>();
    for (Map.Entry<String, AlertComponentApi> e : api.getDetections().entrySet()) {
      final String name = e.getKey();
      final AlertComponentApi component = e.getValue();

      final String key = metricAttributesMap.loadMetricCache(
          component.getMetric().getName(),
          component.getMetric().getDataset().getName(),
          api.getCron());

      final DetectionMetricProperties detectionMetricProperties = metricAttributesMap
          .getDetectionMetricProperties(key);

      metricConfigDTO = detectionMetricProperties.getMetricConfigDTO();
      datasetConfigDTO = detectionMetricProperties.getDatasetConfigDTO();
      listOfDetectionMaps.add(ruleMap(name, component));
    }

    return detectionTranslatorBuilder
        .buildMetricAlertExecutionPlan(
            requireNonNull(metricConfigDTO),
            requireNonNull(datasetConfigDTO),
            api.getName(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            false,
            toRuleYamls(listOfDetectionMaps),
            Collections.emptyList()
        );
  }

  private List<Map<String, Object>> toRuleYamls(
      final List<Map<String, Object>> listOfDetectionMaps) {
    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("detection", listOfDetectionMaps);

    return Collections.singletonList(map);
  }

  private Map<String, Object> ruleMap(
      final String name,
      final AlertComponentApi component) {
    final Map<String, Object> ruleMap = new LinkedHashMap<>();
    ruleMap.put("name", name);
    ruleMap.put("type", component.getType());
    ruleMap.put("params", component.getParams());
    return ruleMap;
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
}
