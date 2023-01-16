/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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
