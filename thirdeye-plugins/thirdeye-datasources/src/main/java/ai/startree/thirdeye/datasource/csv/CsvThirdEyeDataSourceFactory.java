/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.csv;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;

public class CsvThirdEyeDataSourceFactory implements ThirdEyeDataSourceFactory {

  @Override
  public String name() {
    return "csv";
  }

  @Override
  public ThirdEyeDataSource build(final ThirdEyeDataSourceContext context) {
    final ThirdEyeDataSource ds = new CSVThirdEyeDataSource();
    ds.init(context);

    return ds;
  }
}
