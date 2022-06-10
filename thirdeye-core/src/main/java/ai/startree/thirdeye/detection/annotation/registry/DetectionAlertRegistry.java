/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detection.annotation.registry;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilter;
import ai.startree.thirdeye.detection.alert.suppress.DetectionAlertSuppressor;
import ai.startree.thirdeye.spi.detection.annotation.AlertFilter;
import ai.startree.thirdeye.spi.detection.annotation.AlertSuppressor;
import com.google.common.base.Preconditions;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The detection alert registry.
 */
public class DetectionAlertRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionAlertRegistry.class);

  // Alert Scheme type to Alert Scheme class name
  private static final Map<String, String> ALERT_SCHEME_MAP = new HashMap<>();

  // Alert Suppressor type to Alert Suppressor class name
  private static final Map<String, String> ALERT_SUPPRESSOR_MAP = new HashMap<>();

  // Alert Filter Type Map
  private static final Map<String, String> ALERT_FILTER_MAP = new HashMap<>();

  private static DetectionAlertRegistry INSTANCE;

  private DetectionAlertRegistry() {
    init();
  }

  public static DetectionAlertRegistry getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new DetectionAlertRegistry();
    }

    return INSTANCE;
  }

  /**
   * Read all the alert schemes and suppressors and initialize the registry.
   */
  private static void init() {
    try (ScanResult scanResult = new ClassGraph().enableAnnotationInfo().enableClassInfo().scan()) {
      // register alert filters
      ClassInfoList alertFilterClassClasses = scanResult
          .getSubclasses(DetectionAlertFilter.class.getName());
      for (ClassInfo classInfo : alertFilterClassClasses) {
        for (AnnotationInfo annotationInfo : classInfo.getAnnotationInfo()) {
          if (annotationInfo.getName().equals(AlertFilter.class.getName())) {
            Annotation annotation = annotationInfo.loadClassAndInstantiate();
            ALERT_FILTER_MAP.put(((AlertFilter) annotation).type(), classInfo.getName());
            LOG.info("Registered Alter Filter {}", classInfo.getName());
          }
        }
      }

      // register alert suppressors
      ClassInfoList alertSuppressorClasses = scanResult
          .getSubclasses(DetectionAlertSuppressor.class.getName());
      for (ClassInfo classInfo : alertSuppressorClasses) {
        for (AnnotationInfo annotationInfo : classInfo.getAnnotationInfo()) {
          if (annotationInfo.getName().equals(AlertSuppressor.class.getName())) {
            Annotation annotation = annotationInfo.loadClassAndInstantiate();
            ALERT_SUPPRESSOR_MAP.put(((AlertSuppressor) annotation).type(), classInfo.getName());
            LOG.info("Registered Alter AlertSuppressor {}", classInfo.getName());
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("initialize detection registry error", e);
    }
  }

  public void registerAlertFilter(String type, String className) {
    ALERT_FILTER_MAP.put(type, className);
  }

  public void registerAlertScheme(String type, String className) {
    ALERT_SCHEME_MAP.put(type, className);
  }

  public void registerAlertSuppressor(String type, String className) {
    ALERT_SUPPRESSOR_MAP.put(type, className);
  }

  /**
   * Look up the class name for a given alert filter
   *
   * @param type the type used in the YAML configs
   */
  public String lookupAlertFilters(String type) {
    Preconditions.checkArgument(ALERT_FILTER_MAP.containsKey(type.toUpperCase()),
        type + " not found in registry");
    return ALERT_FILTER_MAP.get(type.toUpperCase());
  }

  /**
   * Look up the {@link #ALERT_SCHEME_MAP} for the Alert scheme class name from the type
   */
  public String lookupAlertSchemes(String schemeType) {
    Preconditions.checkArgument(ALERT_SCHEME_MAP.containsKey(schemeType.toUpperCase()),
        schemeType + " not found in registry");
    return ALERT_SCHEME_MAP.get(schemeType.toUpperCase());
  }

  /**
   * Look up the {@link #ALERT_SUPPRESSOR_MAP} for the Alert suppressor class name from the type
   */
  public String lookupAlertSuppressors(String suppressorType) {
    Preconditions.checkArgument(ALERT_SUPPRESSOR_MAP.containsKey(suppressorType.toUpperCase()),
        suppressorType + " not found in registry");
    return ALERT_SUPPRESSOR_MAP.get(suppressorType.toUpperCase());
  }
}
