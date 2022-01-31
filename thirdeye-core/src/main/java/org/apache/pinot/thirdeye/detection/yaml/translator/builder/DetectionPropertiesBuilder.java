/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection.yaml.translator.builder;

import static org.apache.pinot.thirdeye.spi.detection.ConfigUtils.getList;
import static org.apache.pinot.thirdeye.spi.detection.ConfigUtils.getMap;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.wrapper.AnomalyFilterWrapper;
import org.apache.pinot.thirdeye.detection.wrapper.AnomalyLabelerWrapper;
import org.apache.pinot.thirdeye.detection.yaml.translator.DetectionMetricAttributeHolder;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;

/**
 * This class is responsible for translating the detection properties
 */
@Deprecated
public class DetectionPropertiesBuilder extends DetectionConfigPropertiesBuilder {

  private static final Set<String> MOVING_WINDOW_DETECTOR_TYPES = ImmutableSet.of("ALGORITHM");

  public DetectionPropertiesBuilder(DetectionMetricAttributeHolder metricAttributesMap,
      DataProvider provider) {
    super(metricAttributesMap, provider);
  }

  /**
   * Constructs the detection properties mapping by translating the detection & filter yaml
   *
   * @param metricAlertConfigMap holds the parsed yaml for a single metric alert
   */
  @Override
  public Map<String, Object> buildMetricAlertProperties(Map<String, Object> metricAlertConfigMap) {
    final MetricConfigDTO metricConfigDTO = metricAttributesMap.fetchMetric(metricAlertConfigMap);
    final DatasetConfigDTO datasetConfigDTO = metricAttributesMap
        .fetchDataset(metricAlertConfigMap);

    final String alertName = MapUtils.getString(metricAlertConfigMap, PROP_NAME);
    final Map<String, Object> mergerProperties = getMap(metricAlertConfigMap.get(PROP_MERGER));
    final Map<String, Collection<String>> dimensionFiltersMap = getMap(
        metricAlertConfigMap.get(PROP_FILTERS));

    String metricUrn = MetricEntity.fromMetric(dimensionFiltersMap, metricConfigDTO.getId())
        .getUrn();

    // Translate all the rules
    List<Map<String, Object>> nestedPipelines = processRules(datasetConfigDTO,
        alertName, mergerProperties, getList(metricAlertConfigMap.get(PROP_RULES)), metricUrn);

    // Wrap with dimension exploration properties
    return buildMetricAlertExecutionPlan(datasetConfigDTO, alertName, mergerProperties,
        dimensionFiltersMap, getMap(metricAlertConfigMap.get(PROP_DIMENSION_EXPLORATION)),
        metricAlertConfigMap.containsKey(PROP_DIMENSION_EXPLORATION),
        getList(metricAlertConfigMap.get(PROP_GROUPER)),
        metricUrn, nestedPipelines);
  }

  public Map<String, Object> buildMetricAlertExecutionPlan(
      final DatasetConfigDTO datasetConfigDTO,
      final String alertName,
      final Map<String, Object> mergerProperties,
      final Map<String, Collection<String>> dimensionFiltersMap,
      final Map<String, Object> dimensionExploreYaml,
      final boolean containsDimensionExploration,
      final List<Map<String, Object>> grouperYamls,
      final String metricUrn,
      final List<Map<String, Object>> nestedPipelines) {
    Map<String, Object> dimensionWrapperProperties = buildDimensionWrapperProperties(
        dimensionFiltersMap,
        metricUrn,
        datasetConfigDTO.getDataset(),
        dimensionExploreYaml,
        containsDimensionExploration);
    Map<String, Object> properties = new HashMap<>();

    return properties;
  }

  public List<Map<String, Object>> processRules(
      final DatasetConfigDTO datasetConfigDTO,
      final String alertName,
      final Map<String, Object> mergerProperties,
      final List<Map<String, Object>> ruleYamls,
      final String metricUrn) {
    List<Map<String, Object>> nestedPipelines = new ArrayList<>();
    for (Map<String, Object> ruleYaml : ruleYamls) {
      List<Map<String, Object>> detectionYamls = ConfigUtils.getList(ruleYaml.get(PROP_DETECTION));
      List<Map<String, Object>> detectionProperties = buildListOfMergeWrapperProperties(
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
          filterNestedProperties = buildFilterWrapperPropertiesLegacy(metricUrn,
              AnomalyFilterWrapper.class.getName(), filterProperties,
              filterNestedProperties);
        }
        if (labelerYamls.isEmpty()) {
          // output filter properties if no labeler is configured
          nestedPipelines.addAll(filterNestedProperties);
        } else {
          // wrap filter properties around with labeler properties if a labeler is configured
          nestedPipelines.add(
              buildLabelerWrapperProperties(metricUrn, AnomalyLabelerWrapper.class.getName(),
                  labelerYamls.get(0),
                  filterNestedProperties));
        }
      }
    }
    return nestedPipelines;
  }

  @Override
  public Map<String, Object> buildCompositeAlertProperties(
      Map<String, Object> compositeAlertConfigMap) {
    // Recursively translate all the sub-alerts
    List<Map<String, Object>> subDetectionYamls = ConfigUtils
        .getList(compositeAlertConfigMap.get(PROP_ALERTS));
    List<Map<String, Object>> nestedPropertiesList = new ArrayList<>();
    for (Map<String, Object> subDetectionYaml : subDetectionYamls) {
      Map<String, Object> subProps;
      if (subDetectionYaml.containsKey(PROP_TYPE) && subDetectionYaml.get(PROP_TYPE)
          .equals(COMPOSITE_ALERT)) {
        subProps = buildCompositeAlertProperties(subDetectionYaml);
      } else {
        subProps = buildMetricAlertProperties(subDetectionYaml);
      }

      nestedPropertiesList.add(subProps);
    }

    return compositePropertyBuilderHelper(nestedPropertiesList, compositeAlertConfigMap);
  }

  public List<Map<String, Object>> buildListOfMergeWrapperProperties(String subEntityName,
      String metricUrn,
      List<Map<String, Object>> yamlConfigs, Map<String, Object> mergerProperties,
      TimeGranularity datasetTimegranularity) {
    List<Map<String, Object>> properties = new ArrayList<>();
    for (Map<String, Object> yamlConfig : yamlConfigs) {
      properties.add(
          buildMergeWrapperProperties(subEntityName, metricUrn, yamlConfig, mergerProperties,
              datasetTimegranularity));
    }
    return properties;
  }

  public Map<String, Object> buildMergeWrapperProperties(String subEntityName, String metricUrn,
      Map<String, Object> yamlConfig,
      Map<String, Object> mergerProperties,
      TimeGranularity datasetTimegranularity) {
    String detectorType = MapUtils.getString(yamlConfig, PROP_TYPE);
    String name = MapUtils.getString(yamlConfig, PROP_NAME);
    Map<String, Object> nestedProperties = new HashMap<>();
    nestedProperties.put(PROP_SUB_ENTITY_NAME, subEntityName);
    String detectorRefKey = makeComponentRefKey(detectorType, name);

    fillInDetectorWrapperProperties(nestedProperties, yamlConfig, detectorType,
        datasetTimegranularity);

    buildComponentSpec(metricUrn, yamlConfig, detectorRefKey);

    Map<String, Object> properties = new HashMap<>();
    properties.put(PROP_NESTED, Collections.singletonList(nestedProperties));
    properties.put(PROP_DETECTOR, detectorRefKey);

    // fill in baseline provider properties
    if (DETECTION_REGISTRY.isBaselineProvider(detectorType)) {
      // if the detector implements the baseline provider interface, use it to generate baseline
      properties.put(PROP_BASELINE_PROVIDER, detectorRefKey);
    } else {
      String baselineProviderKey = makeComponentRefKey(DEFAULT_BASELINE_PROVIDER_YAML_TYPE, name);
      buildComponentSpec(metricUrn, yamlConfig, baselineProviderKey);
      properties.put(PROP_BASELINE_PROVIDER, baselineProviderKey);
    }
    properties.putAll(mergerProperties);
    return properties;
  }

  // fill in window size and unit if detector requires this
  private static void fillInDetectorWrapperProperties(Map<String, Object> properties,
      Map<String, Object> yamlConfig, String detectorType, TimeGranularity datasetTimegranularity) {
    // set default bucketPeriod
    properties.put(PROP_BUCKET_PERIOD, datasetTimegranularity.toPeriod().toString());

    // override bucketPeriod now since it is needed by detection window
    if (yamlConfig.containsKey(PROP_BUCKET_PERIOD)) {
      properties.put(PROP_BUCKET_PERIOD, MapUtils.getString(yamlConfig, PROP_BUCKET_PERIOD));
    }

    // set default detection window
    setDefaultDetectionWindow(properties, detectorType);

    // override other properties from yaml
    if (yamlConfig.containsKey(PROP_WINDOW_SIZE)) {
      properties.put(PROP_MOVING_WINDOW_DETECTION, true);
      properties.put(PROP_WINDOW_SIZE, MapUtils.getString(yamlConfig, PROP_WINDOW_SIZE));
    }
    if (yamlConfig.containsKey(PROP_WINDOW_UNIT)) {
      properties.put(PROP_MOVING_WINDOW_DETECTION, true);
      properties.put(PROP_WINDOW_UNIT, MapUtils.getString(yamlConfig, PROP_WINDOW_UNIT));
    }
    if (yamlConfig.containsKey(PROP_WINDOW_DELAY)) {
      properties.put(PROP_WINDOW_DELAY, MapUtils.getString(yamlConfig, PROP_WINDOW_DELAY));
    }
    if (yamlConfig.containsKey(PROP_WINDOW_DELAY_UNIT)) {
      properties
          .put(PROP_WINDOW_DELAY_UNIT, MapUtils.getString(yamlConfig, PROP_WINDOW_DELAY_UNIT));
    }
    if (yamlConfig.containsKey(PROP_TIMEZONE)) {
      properties.put(PROP_TIMEZONE, MapUtils.getString(yamlConfig, PROP_TIMEZONE));
    }
    if (yamlConfig.containsKey(PROP_CACHE_PERIOD_LOOKBACK)) {
      properties.put(PROP_CACHE_PERIOD_LOOKBACK,
          MapUtils.getString(yamlConfig, PROP_CACHE_PERIOD_LOOKBACK));
    }
  }

  // Set the default detection window if it is not specified.
  // Here instead of using data granularity we use the detection period to set the default window size.
  private static void setDefaultDetectionWindow(Map<String, Object> properties,
      String detectorType) {
    if (MOVING_WINDOW_DETECTOR_TYPES.contains(detectorType)) {
      properties.put(PROP_MOVING_WINDOW_DETECTION, true);
      org.joda.time.Period detectionPeriod =
          org.joda.time.Period.parse(MapUtils.getString(properties, PROP_BUCKET_PERIOD));
      int days = detectionPeriod.toStandardDays().getDays();
      int hours = detectionPeriod.toStandardHours().getHours();
      int minutes = detectionPeriod.toStandardMinutes().getMinutes();
      if (days >= 1) {
        properties.put(PROP_WINDOW_SIZE, 1);
        properties.put(PROP_WINDOW_UNIT, TimeUnit.DAYS);
      } else if (hours >= 1) {
        properties.put(PROP_WINDOW_SIZE, 24);
        properties.put(PROP_WINDOW_UNIT, TimeUnit.HOURS);
      } else if (minutes >= 1) {
        properties.put(PROP_WINDOW_SIZE, 6);
        properties.put(PROP_WINDOW_UNIT, TimeUnit.HOURS);
        properties.put(PROP_FREQUENCY, new TimeGranularity(15, TimeUnit.MINUTES));
      } else {
        properties.put(PROP_WINDOW_SIZE, 6);
        properties.put(PROP_WINDOW_UNIT, TimeUnit.HOURS);
      }
    }
  }
}
