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

package org.apache.pinot.thirdeye.util;

import static java.util.Objects.requireNonNull;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import org.apache.pinot.thirdeye.datalayer.DataSourceBuilder;
import org.apache.pinot.thirdeye.datalayer.ThirdEyePersistenceModule;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.apache.pinot.thirdeye.datalayer.util.PersistenceConfig;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is deprecated and will be subsequently removed. It hasn't been marked @Deprecated
 * because the alternative is currently a WIP.
 *
 * The goal is to use Guice entirely for service construction. It currently is used both in the
 * coordinator and worker modules. Eventually all the dependencies expected to be injected rather
 * than referenced using DAO_REGISTRY.getInstance()
 */
public abstract class DeprecatedInjectorUtil {

  private static final Logger LOG = LoggerFactory.getLogger(DeprecatedInjectorUtil.class);

  private static Injector injector;

  public static void init(File localConfigFile) {
    final PersistenceConfig configuration = PersistenceConfig
        .readPersistenceConfig(localConfigFile);
    final DatabaseConfiguration dbConfig = configuration.getDatabaseConfiguration();

    init(new DataSourceBuilder().build(dbConfig));
  }

  public static void init(DataSource dataSource) {
    setInjector(Guice.createInjector(new ThirdEyePersistenceModule(dataSource)));
  }

  public static synchronized void setInjector(final Injector injector) {
    if (DeprecatedInjectorUtil.injector != null) {
      LOG.error("OVERWRITING previous injector!!!");
    }
    DeprecatedInjectorUtil.injector = injector;
  }

  public static <T> T getInstance(Class<T> c) {
    return requireNonNull(injector, "Injector not initialized").getInstance(c);
  }
}
