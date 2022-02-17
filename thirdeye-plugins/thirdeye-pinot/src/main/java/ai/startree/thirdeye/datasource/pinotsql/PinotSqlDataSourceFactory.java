/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinotsql;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.pinot.client.PinotDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotSqlDataSourceFactory implements ThirdEyeDataSourceFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PinotSqlDataSourceFactory.class);

  @Override
  public String name() {
    return "pinot-sql";
  }

  @Override
  public ThirdEyeDataSource build(final ThirdEyeDataSourceContext context) {
    try {
      DriverManager.registerDriver(new PinotDriver());
    } catch (SQLException e) {
      LOG.error("Pinot Sql datasource driver registry failed!");
    }
    final ThirdEyeDataSource ds = new PinotSqlThirdEyeDataSource();
    ds.init(context);
    return ds;
  }
}
