/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.detection.model.InputData;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;

/**
 * Input data fetcher interface.
 * For components to fetch the input data it need.
 *
 * Deprecated in favor of {@link DataFetcher}
 * This is used by the v2 pipeline
 */
@Deprecated
public interface InputDataFetcher {

  /**
   * fetch data for input data spec
   */
  @Deprecated
  InputData fetchData(InputDataSpec inputDataSpec);
}
