package org.apache.pinot.thirdeye.datasource.pinotsql;

import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.pinot.client.PinotDriver;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
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
