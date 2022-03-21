package org.apache.pinot.thirdeye.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.pinot.thirdeye.datalayer.DatabaseAdministrator;

@Singleton
public class DatabaseHealthCheck extends HealthCheck {

  private final DatabaseAdministrator databaseAdministrator;

  @Inject
  public DatabaseHealthCheck(final DatabaseAdministrator databaseAdministrator) {
    this.databaseAdministrator = databaseAdministrator;
  }

  @Override
  protected Result check() throws Exception {
    if (databaseAdministrator.validate()) {
      return Result.healthy();
    }
    return Result.unhealthy("Database health check failed.");
  }
}
