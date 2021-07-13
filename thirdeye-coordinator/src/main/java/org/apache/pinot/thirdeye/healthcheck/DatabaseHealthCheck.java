package org.apache.pinot.thirdeye.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.DatabaseAdministrator;

@Singleton
public class DatabaseHealthCheck extends HealthCheck {

  private final DatabaseAdministrator databaseAdministrator;

  @Inject
  public DatabaseHealthCheck(final DatabaseAdministrator databaseAdministrator) {
    this.databaseAdministrator = databaseAdministrator;
  }

  @Override
  protected Result check() throws Exception {
    try{
      if(!databaseAdministrator.validate()){
        throw new Exception("Unexpected results of validation query.");
      }
    } catch(Exception e){
      return Result.unhealthy(e.getMessage());
    }
    return Result.healthy();
  }
}
