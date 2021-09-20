package org.apache.pinot.thirdeye.datasource.pinot;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;

public class PinotLegacyDataSourcePlugin implements Plugin {
  @Override
  public Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return ImmutableList.of(
        new PinotThirdEyeDataSourceFactory()
    );
  }
}
