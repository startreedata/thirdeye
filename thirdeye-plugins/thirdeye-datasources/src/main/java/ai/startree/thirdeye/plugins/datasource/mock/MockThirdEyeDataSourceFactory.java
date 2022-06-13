/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource.mock;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;

public class MockThirdEyeDataSourceFactory implements ThirdEyeDataSourceFactory {

  @Override
  public String name() {
    return "mock";
  }

  @Override
  public ThirdEyeDataSource build(final ThirdEyeDataSourceContext context) {
    final ThirdEyeDataSource ds = new MockThirdEyeDataSource();
    ds.init(context);

    return ds;
  }
}
