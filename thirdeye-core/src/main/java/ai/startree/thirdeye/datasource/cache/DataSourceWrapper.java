package ai.startree.thirdeye.datasource.cache;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import java.sql.Timestamp;

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
