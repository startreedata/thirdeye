package org.apache.pinot.thirdeye.datasource;

import com.google.common.collect.ImmutableList;
import org.apache.pinot.thirdeye.datasource.csv.CsvThirdEyeDataSourceFactory;
import org.apache.pinot.thirdeye.datasource.mock.MockThirdEyeDataSourceFactory;
import org.apache.pinot.thirdeye.datasource.sql.SqlThirdEyeDataSourceFactory;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;

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
