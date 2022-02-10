package ai.startree.thirdeye.spi.datasource;

public interface ThirdEyeDataSourceFactory {

  String name();

  ThirdEyeDataSource build(ThirdEyeDataSourceContext context);
}
