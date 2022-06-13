/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

public interface BaseComponent<T extends AbstractSpec> {

  /**
   * This is the v2 interface and should be only used with the v2 pipeline
   * @param spec
   */
  void init(T spec);

  /**
   * Legacy interface to initialize the base component.
   *
   * @param spec
   * @param dataFetcher
   */
  @Deprecated
  default void init(T spec, InputDataFetcher dataFetcher) {
    throw new UnsupportedOperationException();
  }
}
