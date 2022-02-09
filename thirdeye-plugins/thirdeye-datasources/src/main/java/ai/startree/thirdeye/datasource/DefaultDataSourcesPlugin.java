package ai.startree.thirdeye.datasource;

import ai.startree.thirdeye.datasource.csv.CsvThirdEyeDataSourceFactory;
import ai.startree.thirdeye.datasource.mock.MockThirdEyeDataSourceFactory;
import ai.startree.thirdeye.datasource.sql.SqlThirdEyeDataSourceFactory;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import com.google.common.collect.ImmutableList;

public class DefaultDataSourcesPlugin implements Plugin {

  @Override
  public Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return ImmutableList.of(
        new SqlThirdEyeDataSourceFactory(),
        new CsvThirdEyeDataSourceFactory(),
        new MockThirdEyeDataSourceFactory()
    );
  }
}
