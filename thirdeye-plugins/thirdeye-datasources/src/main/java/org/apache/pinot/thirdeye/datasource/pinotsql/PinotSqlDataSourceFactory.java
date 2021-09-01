package org.apache.pinot.thirdeye.datasource.pinotsql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.client.PinotDriver;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotSqlDataSourceFactory implements ThirdEyeDataSourceFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PinotSqlDataSourceFactory.class);
  private final Map<String, ThirdEyeDataSource> urlToDatasource = new HashMap<>();

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
    final String dsUrl = String.format("%s:%s",
        context.getDataSourceDTO().getProperties().get("controllerHost"),
        context.getDataSourceDTO().getProperties().get("controllerPort"));
    final ThirdEyeDataSource ds;
    synchronized (urlToDatasource) {
      if (!urlToDatasource.containsKey(dsUrl)) {
        LOG.info("Datasource for {} created", dsUrl);
        ds = new PinotSqlThirdEyeDataSource();
        ds.init(context);
        urlToDatasource.put(dsUrl, ds);
      }
    }
    return urlToDatasource.get(dsUrl);
  }
}
