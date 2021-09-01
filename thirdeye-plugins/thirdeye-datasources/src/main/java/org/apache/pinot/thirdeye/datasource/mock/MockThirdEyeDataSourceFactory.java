package org.apache.pinot.thirdeye.datasource.mock;

import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;

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
