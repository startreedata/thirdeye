package org.apache.pinot.thirdeye.datasource;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.datasource.pinot.PinotThirdEyeDataSourceFactory;
import org.apache.pinot.thirdeye.datasource.pinotsql.PinotSqlDataSourceFactory;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;

public class PinotDataSourcePlugin implements Plugin {
  @Override
  public Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return ImmutableList.of(
        new PinotSqlDataSourceFactory(),
        new PinotThirdEyeDataSourceFactory()
    );
  }
}
