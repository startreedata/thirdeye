package org.apache.pinot.thirdeye.spi.datasource;

public interface ThirdEyeDataSourceFactory {

  String name();

  ThirdEyeDataSource build(ThirdEyeDataSourceContext context);
}
