/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datalayer.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import java.io.File;
import javax.validation.Validation;

public class PersistenceConfig extends Configuration {

  /**
   * Persistence specific file will be in
   * <configRootDir>/persistence.yml
   */
  private DatabaseConfiguration databaseConfiguration;

  public static PersistenceConfig readPersistenceConfig(File configFile) {
    YamlConfigurationFactory<PersistenceConfig> factory = new YamlConfigurationFactory<>(
        PersistenceConfig.class,
        Validation.buildDefaultValidatorFactory().getValidator(),
        Jackson.newObjectMapper(),
        "");
    try {
      return factory.build(configFile);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @JsonProperty
  public DatabaseConfiguration getDatabaseConfiguration() {
    return databaseConfiguration;
  }

  public PersistenceConfig setDatabaseConfiguration(
      final DatabaseConfiguration databaseConfiguration) {
    this.databaseConfiguration = databaseConfiguration;
    return this;
  }
}
