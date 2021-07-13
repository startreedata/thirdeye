package org.apache.pinot.thirdeye.datalayer.util;

import com.codahale.metrics.health.HealthCheck;
import java.sql.Connection;
import java.sql.Statement;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.tomcat.jdbc.pool.DataSource;

@Singleton
public class DatabaseHealthCheck extends HealthCheck {

  private final DataSource dataSource;

  @Inject
  public DatabaseHealthCheck(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  protected Result check() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      final Statement statement = connection.createStatement();
      if (!statement.executeQuery(dataSource.getValidationQuery()).next()) {
        throw new Exception("Unexpected results of validation query.");
      }
    } catch (Exception e) {
      return Result.unhealthy(e.getMessage());
    }
    return Result.healthy();
  }
}
