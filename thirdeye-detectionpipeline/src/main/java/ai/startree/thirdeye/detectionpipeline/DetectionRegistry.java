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

  @Inject
  public DetectionRegistry() {

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

  public AnomalyDetector<AbstractSpec> buildDetector(
      String factoryName,
      AnomalyDetectorFactoryContext context) {
    checkArgument(anomalyDetectorFactoryMap.containsKey(factoryName),
        String.format("Detector type not registered: %s. Available detectors: %s",
            factoryName,
            anomalyDetectorFactoryMap.keySet()));
    return anomalyDetectorFactoryMap.get(factoryName).build(context);
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
}
