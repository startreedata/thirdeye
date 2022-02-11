/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.healthcheck;

import ai.startree.thirdeye.datalayer.DatabaseAdministrator;
import com.codahale.metrics.health.HealthCheck;
import javax.inject.Inject;
import javax.inject.Singleton;

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
