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
package ai.startree.thirdeye.detectionpipeline;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactory;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorFactoryContext;
import ai.startree.thirdeye.spi.detection.Enumerator;
import ai.startree.thirdeye.spi.detection.EnumeratorFactory;
import ai.startree.thirdeye.spi.detection.EventTrigger;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import ai.startree.thirdeye.spi.detection.EventTriggerFactoryContext;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The detection registry.
 */
@Singleton
public class DetectionRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionRegistry.class);

  private final Map<String, AnomalyDetectorFactory> anomalyDetectorFactoryMap = new HashMap<>();
  private final Map<String, EventTriggerFactory> triggerFactoryMap = new HashMap<>();
  private final Map<String, EnumeratorFactory> enumeratorFactoryMap = new HashMap<>();

  @Inject
  public DetectionRegistry() {

  }

  private static <T> void validate(final String name, final Map<String, T> m, String type) {
    checkArgument(m.containsKey(name), "%s type not registered: %s. Available: %s",
        type,
        name,
        m.keySet());
  }

  public void addAnomalyDetectorFactory(final AnomalyDetectorFactory f) {
    checkState(!anomalyDetectorFactoryMap.containsKey(f.name()),
        "Duplicate AnomalyDetectorFactory: " + f.name());

    anomalyDetectorFactoryMap.put(f.name(), f);
  }

  public void addEventTriggerFactory(final EventTriggerFactory f) {
    checkState(!triggerFactoryMap.containsKey(f.name()),
        "Duplicate EventTriggerFactory: " + f.name());

    triggerFactoryMap.put(f.name(), f);
  }

  public void addEnumeratorFactory(final EnumeratorFactory f) {
    checkState(!enumeratorFactoryMap.containsKey(f.name()),
        "Duplicate EnumeratorFactory: " + f.name());

    enumeratorFactoryMap.put(f.name(), f);
  }

  public AnomalyDetector<AbstractSpec> buildDetector(
      final String factoryName,
      final AnomalyDetectorFactoryContext context) {
    validate(factoryName, anomalyDetectorFactoryMap, "Detector");
    return anomalyDetectorFactoryMap.get(factoryName).build(context);
  }

  public EventTrigger<AbstractSpec> buildTrigger(
      final String factoryName,
      final EventTriggerFactoryContext context) {
    validate(factoryName, triggerFactoryMap, "Trigger");
    return triggerFactoryMap.get(factoryName).build(context);
  }

  public Enumerator buildEnumerator(final String factoryName) {
    validate(factoryName, enumeratorFactoryMap, "Enumerator");
    return enumeratorFactoryMap.get(factoryName).build();
  }
}
