/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

public interface AnomalyDetectorFactory {

  String name();

  <T extends AbstractSpec>
  AnomalyDetector<T> build(
      AnomalyDetectorFactoryContext context);

  boolean isBaselineProvider();
}
