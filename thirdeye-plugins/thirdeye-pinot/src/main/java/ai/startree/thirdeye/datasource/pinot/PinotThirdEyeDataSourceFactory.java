package ai.startree.thirdeye.datasource.pinot;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;

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
