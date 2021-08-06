package org.apache.pinot.thirdeye.datasource.pinot;

import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.pinot.client.PinotDriver;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotThirdEyeDataSourceFactory implements ThirdEyeDataSourceFactory {
  private static final Logger LOG = LoggerFactory.getLogger(PinotThirdEyeDataSourceFactory.class);

  @Override
  public String name() {
    return "pinot";
  }

  @Override
  public ThirdEyeDataSource build(final ThirdEyeDataSourceContext context) {
    try {
      DriverManager.registerDriver(new PinotDriver());
    } catch (SQLException e) {
      LOG.error("Pinot datasource driver registry failed!");
    }
    final ThirdEyeDataSource ds = new PinotThirdEyeDataSource();
    ds.init(context);

    return ds;
  }
}
