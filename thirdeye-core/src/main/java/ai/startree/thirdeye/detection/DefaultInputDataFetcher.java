/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.detection.DataProvider;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.model.InputData;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;

/**
 * Input data fetcher.
 * For components to fetch the input data it need.
 */
public class DefaultInputDataFetcher implements InputDataFetcher {

  private final DataProvider provider;
  private final long configId;

  public DefaultInputDataFetcher(DataProvider provider, long configId) {
    this.provider = provider;
    this.configId = configId;
  }

  /**
   * Fetch data for input data spec
   */
  @Deprecated
  // todo cyril remove once detector ensembling is implemented - kept to keep the filter code
  public InputData fetchData(InputDataSpec inputDataSpec) {
    throw new UnsupportedOperationException("fetchData not supported anymore");
  }
}
