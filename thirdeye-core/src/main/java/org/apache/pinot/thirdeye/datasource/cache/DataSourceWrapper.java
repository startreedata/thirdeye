package org.apache.pinot.thirdeye.datasource.cache;

import java.sql.Timestamp;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;

public class DataSourceWrapper {
  private final ThirdEyeDataSource dataSource;
  private final Timestamp updateTime;

  public DataSourceWrapper(final ThirdEyeDataSource dataSource, final Timestamp updateTime) {
    this.dataSource = dataSource;
    this.updateTime = updateTime;
  }

  public ThirdEyeDataSource getDataSource() {
    return dataSource;
  }

  public Timestamp getUpdateTime() {
    return updateTime;
  }
}