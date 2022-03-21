package org.apache.pinot.thirdeye.alert;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionConfigPropertiesBuilder.PROP_FILTER;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.detection.wrapper.AnomalyFilterWrapper;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricAttributeHolder;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricProperties;
import org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionPropertiesBuilder;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertNodeApi;
import org.apache.pinot.thirdeye.spi.api.MetricApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertNodeType;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertExecutionPlanBuilder {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertExecutionPlanBuilder.class);

  private final DataProvider dataProvider;
  private final DetectionMetricAttributeHolder metricAttributesMap;

  private Map<String, Object> properties = Collections.emptyMap();
  private DetectionPropertiesBuilder detectionTranslatorBuilder;
  private String metricUrn;
  private TimeGranularity timeGranularity;
  private Map<String, AlertNodeApi> nodes;
  private AlertApi alertApi;

  public AlertExecutionPlanBuilder(
      final DataProvider dataProvider) {
    this.dataProvider = dataProvider;

    metricAttributesMap = new DetectionMetricAttributeHolder(dataProvider);
  }

  private static String metricApiKey(final MetricApi metricApi) {
    return String.format("%s/%s",
        requireNonNull(metricApi.getDataset()).getName(),
        requireNonNull(metricApi.getName()));
  }

  public AlertExecutionPlanBuilder process(final AlertApi api) {
    alertApi = api;
    nodes = api.getNodes();
    if (nodes == null) {
      return this;
    }
    populateNodeNames(nodes);

    detectionTranslatorBuilder = new DetectionPropertiesBuilder(metricAttributesMap, dataProvider);

    final MetricApi metric = findMetricApi(nodes);
    final String key = metricAttributesMap.loadMetricCache(
        metric.getName(),
        metric.getDataset().getName(),
        api.getCron());

    final DetectionMetricProperties detectionMetricProperties = metricAttributesMap
        .getDetectionMetricProperties(key);

    final MetricConfigDTO metricConfigDTO = requireNonNull(
        detectionMetricProperties.getMetricConfigDTO());
    final DatasetConfigDTO datasetConfigDTO = requireNonNull(
        detectionMetricProperties.getDatasetConfigDTO());
    timeGranularity = datasetConfigDTO.bucketTimeGranularity();

    final Map<String, Collection<String>> dimensionFiltersMap = ConfigUtils.getMap(api.getFilters());
    final Map<String, Object> mergerProperties = Collections.emptyMap();
    this.metricUrn = MetricEntity
        .fromMetric(dimensionFiltersMap, metricConfigDTO.getId())
        .getUrn();

    final List<Map<String, Object>> mapList = new ArrayList<>();
    final Set<String> rootNodeNames = findRoots(nodes);
    for (String name : rootNodeNames) {
      mapList.add(toProperties(nodes.get(name)));
    }

    // Wrap with dimension exploration properties
    properties = detectionTranslatorBuilder
        .buildMetricAlertExecutionPlan(
            datasetConfigDTO,
            api.getName(),
            mergerProperties,
            dimensionFiltersMap,
            Collections.emptyMap(),
            false,
            Collections.emptyList(),
            metricUrn,
            mapList);

    return this;
  }

  private Map<String, Object> toProperties(final AlertNodeApi node) {
    final List<Map<String, Object>> mapList = optional(node.getDependsOn())
        .orElse(Collections.emptyList())
        .stream()
        .map(name -> nodes.get(name))
        .map(this::toProperties)
        .collect(Collectors.toList());

    if (node.getType() == AlertNodeType.DETECTION) {
      return detectionTranslatorBuilder
          .buildMergeWrapperProperties(
              alertApi.getName(),
              metricUrn,
              toMap(node),
              emptyMap(),
              timeGranularity);
    } else if (node.getType() == AlertNodeType.FILTER) {

      return detectionTranslatorBuilder
          .buildFilterLabelerWrapperProperties(metricUrn,
              AnomalyFilterWrapper.class.getName(),
              toMap(node),
              mapList,
              PROP_FILTER);
    }
    ensure(false, "Unsupported node type!");
    // TODO suvodeep Add Dimension Exploration and Labeler code.
    return null;
  }

  public Map<String, Object> getComponentSpecs() {
    return metricAttributesMap.getAllComponents();
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  private void populateNodeNames(final Map<String, AlertNodeApi> nodes) {
    for (Map.Entry<String, AlertNodeApi> e : nodes.entrySet()) {
      e.getValue().setName(e.getKey());
    }
  }

  private Set<String> findRoots(final Map<String, AlertNodeApi> nodes) {
    final Set<String> allNodeNames = new HashSet<>(nodes.keySet());
    final Set<String> nonRootNodes = nodes
        .values()
        .stream()
        .map(AlertNodeApi::getDependsOn)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .collect(Collectors.toSet());

    allNodeNames.removeAll(nonRootNodes);
    return allNodeNames;
  }

  private MetricApi findMetricApi(final Map<String, AlertNodeApi> nodes) {
    Map<String, MetricApi> metricApiMap = new HashMap<>();
    nodes
        .values()
        .stream()
        .map(AlertNodeApi::getMetric)
        .filter(Objects::nonNull)
        .forEach(metricApi -> metricApiMap.put(metricApiKey(metricApi), metricApi));

    ensure(metricApiMap.size() == 1, "Only 1 metric supported at this time!");
    return metricApiMap.values().iterator().next();
  }

  private Map<String, Object> toMap(
      final AlertNodeApi node) {
    final Map<String, Object> ruleMap = new LinkedHashMap<>();
    ruleMap.put("name", node.getName());
    ruleMap.put("type", node.getSubType());
    ruleMap.put("params", node.getParams());
    return ruleMap;
  }
}
