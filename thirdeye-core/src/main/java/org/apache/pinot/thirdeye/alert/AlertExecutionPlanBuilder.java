package org.apache.pinot.thirdeye.alert;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import static org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionConfigPropertiesBuilder.PROP_DETECTION;
import static org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionConfigPropertiesBuilder.PROP_FILTER;
import static org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionConfigPropertiesBuilder.PROP_LABELER;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertNodeApi;
import org.apache.pinot.thirdeye.api.MetricApi;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datalayer.pojo.AlertNodeType;
import org.apache.pinot.thirdeye.detection.ConfigUtils;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.wrapper.AnomalyFilterWrapper;
import org.apache.pinot.thirdeye.detection.wrapper.AnomalyLabelerWrapper;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricAttributeHolder;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricProperties;
import org.apache.pinot.thirdeye.detection.yaml.translator.builder.DetectionPropertiesBuilder;
import org.apache.pinot.thirdeye.rootcause.impl.MetricEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertExecutionPlanBuilder {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertExecutionPlanBuilder.class);

  private final DataProvider dataProvider;
  private final DetectionMetricAttributeHolder metricAttributesMap;
  private Map<String, Object> properties = Collections.emptyMap();

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
    final Map<String, AlertNodeApi> nodes = api.getNodes();
    if (nodes == null) {
      return this;
    }
    populateNodeNames(nodes);

    final DetectionPropertiesBuilder detectionTranslatorBuilder =
        new DetectionPropertiesBuilder(metricAttributesMap, dataProvider);

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

    final List<Map<String, Object>> nestedPipelines = new ArrayList<>();

    final Queue<AlertNodeApi> q = new ArrayDeque<>();
    q.offer(findRoot(nodes));

    final Map<String, Collection<String>> dimensionFiltersMap = Collections.emptyMap();
    final Map<String, Object> mergerProperties = Collections.emptyMap();
    final String metricUrn = MetricEntity
        .fromMetric(dimensionFiltersMap, metricConfigDTO.getId())
        .getUrn();

    while (q.size() != 0) {
      final AlertNodeApi node = q.poll();
      if (node == null) {
        break;
      }

      if (node.getType() == AlertNodeType.DETECTION) {

        final List<Map<String, Object>> detectionList = Collections
            .singletonList(ruleMap(node));
        List<Map<String, Object>> detectionProperties = detectionTranslatorBuilder
            .buildListOfMergeWrapperProperties(
                api.getName(),
                metricUrn,
                detectionList,
                mergerProperties,
                datasetConfigDTO.bucketTimeGranularity());
        nestedPipelines.addAll(detectionProperties);
      }

      optional(node.getDependsOn())
          .ifPresent(l -> l.stream()
              .map(nodes::get)
              .forEach(q::offer))
      ;
    }
    final String alertName = api.getName();

    // TODO suvodeep Add Dimension Exploration and Labeler code.

    // Wrap with dimension exploration properties
    properties = detectionTranslatorBuilder
        .buildMetricAlertExecutionPlan(
            datasetConfigDTO,
            alertName,
            mergerProperties,
            dimensionFiltersMap,
            Collections.emptyMap(),
            false,
            Collections.emptyList(),
            metricUrn,
            nestedPipelines);

    return this;
  }

  @SuppressWarnings("unused")
  private List<Map<String, Object>> processRules(
      final DatasetConfigDTO datasetConfigDTO,
      final String alertName,
      final Map<String, Object> mergerProperties,
      final List<Map<String, Object>> ruleYamls,
      final String metricUrn,
      final DetectionPropertiesBuilder detectionTranslatorBuilder) {
    List<Map<String, Object>> nestedPipelines = new ArrayList<>();
    for (Map<String, Object> ruleYaml : ruleYamls) {
      List<Map<String, Object>> detectionYamls = ConfigUtils.getList(ruleYaml.get(PROP_DETECTION));
      List<Map<String, Object>> detectionProperties = detectionTranslatorBuilder
          .buildListOfMergeWrapperProperties(
              alertName, metricUrn, detectionYamls, mergerProperties,
              datasetConfigDTO.bucketTimeGranularity());

      List<Map<String, Object>> filterYamls = ConfigUtils.getList(ruleYaml.get(PROP_FILTER));
      List<Map<String, Object>> labelerYamls = ConfigUtils.getList(ruleYaml.get(PROP_LABELER));
      if (filterYamls.isEmpty() && labelerYamls.isEmpty()) {
        // output detection properties if neither filter and labeler is configured
        nestedPipelines.addAll(detectionProperties);
      } else {
        // wrap detection properties around with filter properties if a filter is configured
        List<Map<String, Object>> filterNestedProperties = detectionProperties;
        for (Map<String, Object> filterProperties : filterYamls) {
          filterNestedProperties = detectionTranslatorBuilder
              .buildFilterWrapperProperties(metricUrn,
                  AnomalyFilterWrapper.class.getName(), filterProperties,
                  filterNestedProperties);
        }
        if (labelerYamls.isEmpty()) {
          // output filter properties if no labeler is configured
          nestedPipelines.addAll(filterNestedProperties);
        } else {
          // wrap filter properties around with labeler properties if a labeler is configured
          nestedPipelines.add(
              detectionTranslatorBuilder
                  .buildLabelerWrapperProperties(metricUrn, AnomalyLabelerWrapper.class.getName(),
                      labelerYamls.get(0),
                      filterNestedProperties));
        }
      }
    }
    return nestedPipelines;
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

  private AlertNodeApi findRoot(final Map<String, AlertNodeApi> nodes) {
    final Set<String> allNodeNames = new HashSet<>(nodes.keySet());
    final Set<String> nonRootNodes = nodes
        .values()
        .stream()
        .map(AlertNodeApi::getDependsOn)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .collect(Collectors.toSet());

    allNodeNames.removeAll(nonRootNodes);
    ensure(allNodeNames.size() == 1, "Found more than 1 root!");

    final String rootNodeName = allNodeNames.iterator().next();
    return nodes.get(rootNodeName);
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

  private List<Map<String, Object>> toRuleYamls(
      final List<Map<String, Object>> listOfDetectionMaps) {
    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("detection", listOfDetectionMaps);

    return Collections.singletonList(map);
  }

  private Map<String, Object> ruleMap(
      final AlertNodeApi node) {
    final Map<String, Object> ruleMap = new LinkedHashMap<>();
    ruleMap.put("name", node.getName());
    ruleMap.put("type", node.getSubType());
    ruleMap.put("params", node.getParams());
    return ruleMap;
  }
}
