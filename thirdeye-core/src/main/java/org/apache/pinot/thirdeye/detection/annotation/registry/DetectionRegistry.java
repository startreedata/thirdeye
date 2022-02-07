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

package org.apache.pinot.thirdeye.detection.annotation.registry;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactoryV2Context;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Factory;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;
import org.apache.pinot.thirdeye.spi.detection.EventTrigger;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactory;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactoryContext;
import org.apache.pinot.thirdeye.spi.detection.annotation.Components;
import org.apache.pinot.thirdeye.spi.detection.annotation.Tune;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The detection registry.
 *
 * TODO spyne Guicify class instead of using 'static' except annotation scan
 */
@Singleton
public class DetectionRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionRegistry.class);

  // component type to component class name and annotation
  // Deprecated in favor of new plugin implementation
  @Deprecated
  private static final Map<String, Map> REGISTRY_MAP = new HashMap<>();

  // component class name to tuner annotation
  // Deprecated in favor of new plugin implementation
  @Deprecated
  private static final Map<String, Tune> TUNE_MAP = new HashMap<>();

  // yaml pipeline type to yaml converter class name
  private static final Map<String, String> YAML_MAP = new HashMap<>();

  private static final String KEY_CLASS_NAME = "className";
  private static final String KEY_ANNOTATION = "annotation";
  private static final String KEY_IS_BASELINE_PROVIDER = "isBaselineProvider";
  private static final Map<String, AnomalyDetectorV2Factory> anomalyDetectorV2FactoryMap = new HashMap<>();
  private static final Map<String, EventTriggerFactory> triggerFactoryMap = new HashMap<>();

  static {
    init();
  }

  @Inject
  public DetectionRegistry() {

  }

  /**
   * Read all the components, tune, and yaml annotations and initialize the registry.
   */
  private static void init() {
    try (ScanResult scanResult = new ClassGraph().enableAnnotationInfo().enableClassInfo().scan()) {
      // register components
      ClassInfoList classes = scanResult.getClassesImplementing(BaseComponent.class.getName());
      for (ClassInfo classInfo : classes) {
        String className = classInfo.getName();
        for (AnnotationInfo annotationInfo : classInfo.getAnnotationInfo()) {
          Annotation annotation = annotationInfo.loadClassAndInstantiate();
          if (annotation instanceof Components) {
            Components componentsAnnotation = (Components) annotation;
            REGISTRY_MAP.put(componentsAnnotation.type(),
                ImmutableMap.of(KEY_CLASS_NAME,
                    className,
                    KEY_ANNOTATION,
                    componentsAnnotation,
                    KEY_IS_BASELINE_PROVIDER,
                    BaselineProvider.isBaselineProvider(Class.forName(className))));
            LOG.info("Registered component {} - {}", componentsAnnotation.type(), className);
          }
          if (annotation instanceof Tune) {
            Tune tunableAnnotation = (Tune) annotation;
            TUNE_MAP.put(className, tunableAnnotation);
            LOG.info("Registered tuner {} - {}", className, tunableAnnotation.tunable());
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("initialize detection registry error", e);
    }
  }

  public static void registerComponent(String className, String type) {
    try {
      Class<? extends BaseComponent> clazz = (Class<? extends BaseComponent>) Class
          .forName(className);
      REGISTRY_MAP.put(type, ImmutableMap
          .of(KEY_CLASS_NAME,
              className,
              KEY_IS_BASELINE_PROVIDER,
              BaselineProvider.isBaselineProvider(clazz)));
      LOG.info("Registered component {} {}", type, className);
    } catch (Exception e) {
      LOG.warn("Encountered exception when registering component {}", className, e);
    }
  }

  public static void registerTunableComponent(String className, String tunable, String type) {
    try {
      Class<? extends BaseComponent> clazz = (Class<? extends BaseComponent>) Class
          .forName(className);
      REGISTRY_MAP.put(type, ImmutableMap
          .of(KEY_CLASS_NAME,
              className,
              KEY_IS_BASELINE_PROVIDER,
              BaselineProvider.isBaselineProvider(clazz)));
      Tune tune = new Tune() {
        @Override
        public String tunable() {
          return tunable;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
          return Tune.class;
        }
      };
      TUNE_MAP.put(className, tune);
      LOG.info("Registered tunable component {} {}", type, className);
    } catch (Exception e) {
      LOG.warn("Encountered exception when registering component {}", className, e);
    }
  }

  public void addAnomalyDetectorV2Factory(final AnomalyDetectorV2Factory f) {
    anomalyDetectorV2FactoryMap.put(f.name(), f);
  }

  public void addEventTriggerFactory(final EventTriggerFactory f) {
    triggerFactoryMap.put(f.name(), f);
  }

  public AnomalyDetectorV2<AbstractSpec> buildDetectorV2(
      String factoryName,
      AnomalyDetectorFactoryV2Context context) {
    checkArgument(anomalyDetectorV2FactoryMap.containsKey(factoryName),
        String.format("Detector type not registered: %s. Available detectors: %s",
            factoryName,
            anomalyDetectorV2FactoryMap.keySet()));
    return anomalyDetectorV2FactoryMap.get(factoryName).build(context);
  }

  public EventTrigger<AbstractSpec> buildTrigger(
      String factoryName,
      EventTriggerFactoryContext context) {
    checkArgument(triggerFactoryMap.containsKey(factoryName),
        String.format("Trigger type not registered: %s. Available triggers: %s",
            factoryName,
            triggerFactoryMap.keySet()));
    return triggerFactoryMap.get(factoryName).build(context);
  }

  /**
   * Look up the class name for a given component
   *
   * @param type the type used in the YAML configs
   * @return component class name
   */
  public String lookup(String type) {
    validate(type);
    return MapUtils.getString(REGISTRY_MAP.get(type), KEY_CLASS_NAME);
  }

  /**
   * is type contained in REGISTRY_MAP.
   * REGISTRY_MAP is built from annotated classes and doesn't contain any plugins
   */
  public boolean isAnnotatedType(String type) {
    validate(type);
    return REGISTRY_MAP.containsKey(type);
  }

  private void validate(final String type) {
    requireNonNull(type, "type is null");
    checkArgument(REGISTRY_MAP.containsKey(type), type + " not found in registry");
  }

  /**
   * Look up the tunable class name for a component class name
   *
   * @return tunable class name
   */
  public String lookupTunable(String type) {
    checkArgument(TUNE_MAP.containsKey(type), type + " not found in registry");
    return this.lookup(TUNE_MAP.get(type).tunable());
  }

  public boolean isTunable(String className) {
    return TUNE_MAP.containsKey(className);
  }

  public boolean isBaselineProvider(String type) {
    validate(type);
    return MapUtils.getBooleanValue(REGISTRY_MAP.get(type), KEY_IS_BASELINE_PROVIDER);
  }

  /**
   * Return all component implementation annotations
   *
   * @return List of component annotation
   */
  public List<Components> getAllAnnotation() {
    List<Components> annotations = new ArrayList<>();
    for (Map.Entry<String, Map> entry : REGISTRY_MAP.entrySet()) {
      Map infoMap = entry.getValue();
      if (infoMap.containsKey(KEY_ANNOTATION)) {
        annotations.add((Components) infoMap.get(KEY_ANNOTATION));
      }
    }
    return annotations;
  }

  public String printAnnotations() {
    return String.join(", ", YAML_MAP.keySet());
  }
}
