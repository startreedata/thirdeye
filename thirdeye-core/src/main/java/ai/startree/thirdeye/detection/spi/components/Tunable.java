/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.spi.components;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.BaseComponent;
import java.util.Map;
import org.joda.time.Interval;

/**
 * The tunable. For tuning specs of each component. Will be initialize with user's input yaml for
 * this
 * component.
 */
public interface Tunable<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Returns the new spec for the component it's tuning for a given metric urn.
   *
   * @param currentSpec current spec for the component. empty if not exist.
   * @param tuningWindow the tuning window
   * @param metricUrn the metric urn to tune. When detection runs,the tuned spec will be used
   *     for detection
   *     to run on this metric urn.
   * @return the specs for the component it's tuning. Will be used to initialize the tuned component
   *     when detection runs.
   */
  Map<String, Object> tune(Map<String, Object> currentSpec, Interval tuningWindow,
      String metricUrn);
}
