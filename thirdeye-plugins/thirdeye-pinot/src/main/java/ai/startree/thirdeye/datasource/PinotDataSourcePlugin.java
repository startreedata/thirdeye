package ai.startree.thirdeye.datasource;

import ai.startree.thirdeye.datasource.pinot.PinotThirdEyeDataSourceFactory;
import ai.startree.thirdeye.datasource.pinotsql.PinotSqlDataSourceFactory;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import com.google.common.collect.ImmutableList;

public class PinotDataSourcePlugin implements Plugin {
  @Override
  public Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return ImmutableList.of(
        new PinotSqlDataSourceFactory(),
        new PinotThirdEyeDataSourceFactory()
    );
  }
}
