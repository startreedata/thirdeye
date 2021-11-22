package org.apache.pinot.thirdeye.datasource.pinot;

import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;

public class PinotThirdEyeDataSourceFactory implements ThirdEyeDataSourceFactory {

  @Override
  public String name() {
    return "pinot";
  }

  @Override
  public ThirdEyeDataSource build(final ThirdEyeDataSourceContext context) {
    final ThirdEyeDataSource ds = new PinotThirdEyeDataSource(new PinotSqlExpressionBuilder(),
        new PinotSqlLanguage());
    ds.init(context);

    return ds;
  }
}
