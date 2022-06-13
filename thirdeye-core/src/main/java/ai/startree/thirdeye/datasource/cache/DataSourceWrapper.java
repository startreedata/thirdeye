/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

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
