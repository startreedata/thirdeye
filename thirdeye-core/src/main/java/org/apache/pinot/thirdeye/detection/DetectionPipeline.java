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

package org.apache.pinot.thirdeye.detection;

import static org.apache.pinot.thirdeye.detection.yaml.translator.SubscriptionConfigTranslator.PROP_TYPE;
import static org.apache.pinot.thirdeye.spi.detection.DetectionUtils.getSpecClassName;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetector;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactoryContext;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.InputDataFetcher;
import org.apache.pinot.thirdeye.spi.detection.dimension.DimensionMap;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DetectionPipeline forms the root of the detection class hierarchy. It represents a wireframe
 * for implementing (intermittently stateful) executable pipelines on top of it.
 */
@Deprecated
public abstract class DetectionPipeline {

  private static final String PROP_CLASS_NAME = "className";
  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipeline.class);

  protected final DataProvider provider;
  protected final AlertDTO config;
  protected final long startTime;
  protected final long endTime;

  // TODO spyne refactor this to use Guice singleton
  private final DetectionRegistry detectionRegistry = new DetectionRegistry();

  private DetectionPipelineFactory mockDetectionPipelineFactory;

  protected DetectionPipeline(DataProvider provider, AlertDTO config, long startTime,
      long endTime) {
    this.provider = provider;
    this.config = config;
    this.startTime = startTime;
    this.endTime = endTime;
    this.initComponents();
  }

  /**
   * Only used for testing. To be refactored. Please do not use.
   */
  @Deprecated
  public DetectionPipeline setMockDetectionPipelineFactory(
      final DetectionPipelineFactory mockDetectionPipelineFactory) {
    this.mockDetectionPipelineFactory = mockDetectionPipelineFactory;
    return this;
  }

  /**
   * Returns a detection result for the time range between {@code startTime} and {@code endTime}.
   *
   * @return detection result
   */
  public abstract DetectionPipelineResultV1 run() throws Exception;

  /**
   * Initialize all components in the pipeline
   */
  private void initComponents() {
    InputDataFetcher dataFetcher = new DefaultInputDataFetcher(this.provider, this.config.getId());
    Map<String, BaseComponent> instancesMap = config.getComponents();
    Map<String, Object> componentSpecs = config.getComponentSpecs();
    if (componentSpecs != null) {
      for (String componentKey : componentSpecs.keySet()) {
        Map<String, Object> componentSpec = ConfigUtils.getMap(componentSpecs.get(componentKey));
        if (!instancesMap.containsKey(componentKey)) {
          instancesMap.put(componentKey, createComponent(componentSpec, dataFetcher));
        }
      }

      for (String componentKey : componentSpecs.keySet()) {
        Map<String, Object> componentSpec = ConfigUtils.getMap(componentSpecs.get(componentKey));
        for (Map.Entry<String, Object> entry : componentSpec.entrySet()) {
          if (entry.getValue() != null && DetectionUtils
              .isReferenceName(entry.getValue().toString())) {
            componentSpec.put(entry.getKey(),
                instancesMap.get(DetectionUtils.getComponentKey(entry.getValue().toString())));
          }
        }
        // Initialize the components
        if (!componentSpec.containsKey(PROP_TYPE)
            || detectionRegistry.isAnnotatedType(componentSpec.get(PROP_TYPE).toString())) {
          instancesMap.get(componentKey).init(getComponentSpec(componentSpec), dataFetcher);
        }
      }
    }
    config.setComponents(instancesMap);
  }

  private BaseComponent createComponent(Map<String, Object> componentSpec,
      final InputDataFetcher dataFetcher) {
    final AnomalyDetector<AbstractSpec> detector = optional(componentSpec.get(PROP_TYPE))
        .map(Object::toString)
        .map(type -> createDetector(componentSpec, dataFetcher, type))
        .orElse(null);
    if (detector != null) {
      return detector;
    }

    final String className = MapUtils.getString(componentSpec, PROP_CLASS_NAME);
    try {
      Class<BaseComponent> clazz = (Class<BaseComponent>) Class.forName(className);
      return clazz.newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to create component for " + className,
          e.getCause());
    }
  }

  private AnomalyDetector<AbstractSpec> createDetector(
      final Map<String, Object> componentSpec, final InputDataFetcher dataFetcher,
      final String type) {
    final AnomalyDetector<AbstractSpec> detector = detectionRegistry.buildDetector(type,
        new AnomalyDetectorFactoryContext()
            .setInputDataFetcher(dataFetcher)
            .setProperties(componentSpec)
    );
    return detector;
  }

  private AbstractSpec getComponentSpec(Map<String, Object> componentSpec) {
    String className = MapUtils.getString(componentSpec, PROP_CLASS_NAME);
    try {
      Class clazz = Class.forName(className);
      Class<AbstractSpec> specClazz = (Class<AbstractSpec>) Class.forName(getSpecClassName(clazz));
      return AbstractSpec.fromProperties(componentSpec, specClazz);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to get component spec for " + className, e);
    }
  }

  /**
   * Helper for creating an anomaly for a given metric slice. Injects properties such as
   * metric name, filter dimensions, etc.
   *
   * @param slice metric slice
   * @return anomaly template
   */
  protected final MergedAnomalyResultDTO makeAnomaly(MetricSlice slice) {
    Map<Long, MetricConfigDTO> metrics = this.provider
        .fetchMetrics(Collections.singleton(slice.getMetricId()));
    if (!metrics.containsKey(slice.getMetricId())) {
      throw new IllegalArgumentException(
          String.format("Could not resolve metric id %d", slice.getMetricId()));
    }

    MetricConfigDTO metric = metrics.get(slice.getMetricId());

    return makeAnomaly(slice, metric);
  }

  /**
   * Helper for creating an anomaly for a given metric slice. Injects properties such as
   * metric name, filter dimensions, etc.
   *
   * @param slice metric slice
   * @param metric metric config dto related to slice
   * @return anomaly template
   */
  protected final MergedAnomalyResultDTO makeAnomaly(MetricSlice slice, MetricConfigDTO metric) {
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(slice.getStart());
    anomaly.setEndTime(slice.getEnd());
    anomaly.setMetric(metric.getName());
    anomaly.setCollection(metric.getDataset());
    anomaly.setMetricUrn(MetricEntity.fromSlice(slice, 1.0).getUrn());
    anomaly.setDimensions(toFilterMap(slice.getFilters()));
    anomaly.setDetectionConfigId(this.config.getId());
    anomaly.setChildren(new HashSet<MergedAnomalyResultDTO>());

    return anomaly;
  }

  /**
   * Helper to initialize and run the next level wrapper
   *
   * @param nestedProps nested properties
   * @return intermediate result of a detection pipeline
   */
  protected DetectionPipelineResultV1 runNested(
      Map<String, Object> nestedProps, final long startTime, final long endTime) throws Exception {
    Preconditions.checkArgument(nestedProps.containsKey(PROP_CLASS_NAME),
        "Nested missing " + PROP_CLASS_NAME);
    Map<String, Object> properties = new HashMap<>(nestedProps);
    AlertDTO nestedConfig = new AlertDTO();
    nestedConfig.setId(this.config.getId());
    nestedConfig.setName(this.config.getName());
    nestedConfig.setDescription(this.config.getDescription());
    nestedConfig.setComponents(this.config.getComponents());
    nestedConfig.setProperties(properties);

    final DetectionPipelineFactory detectionPipelineFactory = optional(mockDetectionPipelineFactory)
        .orElse(new DetectionPipelineFactory(provider));

    final DetectionPipeline pipeline = detectionPipelineFactory.get(
        new DetectionPipelineContext()
            .setAlert(nestedConfig)
            .setStart(startTime)
            .setEnd(endTime)
    );
    return pipeline.run();
  }

  // TODO anomaly should support multimap
  private DimensionMap toFilterMap(Multimap<String, String> filters) {
    DimensionMap map = new DimensionMap();
    for (Map.Entry<String, String> entry : filters.entries()) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  public DataProvider getProvider() {
    return provider;
  }

  public AlertDTO getConfig() {
    return config;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }
}
