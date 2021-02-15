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

package org.apache.pinot.thirdeye.worker;

import static org.apache.pinot.thirdeye.Constants.CTX_INJECTOR;
import static org.apache.pinot.thirdeye.datalayer.util.PersistenceConfig.readPersistenceConfig;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.common.ThirdEyeSwaggerBundle;
import org.apache.pinot.thirdeye.common.restclient.ThirdEyeRestClientConfiguration;
import org.apache.pinot.thirdeye.common.time.TimeGranularity;
import org.apache.pinot.thirdeye.common.utils.SessionUtils;
import org.apache.pinot.thirdeye.datalayer.DataSourceBuilder;
import org.apache.pinot.thirdeye.datalayer.bao.SessionManager;
import org.apache.pinot.thirdeye.datalayer.dto.SessionDTO;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.apache.pinot.thirdeye.datalayer.util.PersistenceConfig;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.scheduler.DetectionCronScheduler;
import org.apache.pinot.thirdeye.scheduler.SchedulerService;
import org.apache.pinot.thirdeye.scheduler.SubscriptionCronScheduler;
import org.apache.pinot.thirdeye.task.TaskDriver;
import org.apache.pinot.thirdeye.tracking.RequestStatisticsLogger;
import org.apache.pinot.thirdeye.util.DeprecatedInjectorUtil;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeWorker extends Application<ThirdEyeWorkerConfiguration> {

  protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private TaskDriver taskDriver = null;
  private SchedulerService schedulerService;
  private RequestStatisticsLogger requestStatisticsLogger = null;
  private Injector injector;

  public static void main(final String[] args) throws Exception {
    List<String> argList = new ArrayList<>(Arrays.asList(args));
    if (argList.isEmpty()) {
      argList.add("./config");
    }

    if (argList.size() <= 1) {
      argList.add(0, "server");
    }

    int lastIndex = argList.size() - 1;
    String thirdEyeConfigDir = argList.get(lastIndex);
    System.setProperty("dw.rootDir", thirdEyeConfigDir);
    String detectorApplicationConfigFile = thirdEyeConfigDir + "/" + "detector.yml";
    argList.set(lastIndex, detectorApplicationConfigFile); // replace config dir with the
    // actual config file
    new ThirdEyeWorker().run(argList.toArray(new String[argList.size()]));
  }

  @Override
  public void initialize(final Bootstrap<ThirdEyeWorkerConfiguration> bootstrap) {
    bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    bootstrap.addBundle(new ThirdEyeSwaggerBundle());
  }

  @Override
  public void run(final ThirdEyeWorkerConfiguration config, final Environment env) {
    LOG.info("Starting ThirdEye Worker : Scheduler {} Worker {}", config.isScheduler(),
        config.isWorker());
    final DatabaseConfiguration dbConfig = getDatabaseConfiguration();
    final DataSource dataSource = new DataSourceBuilder().build(dbConfig);

    injector = Guice.createInjector(new ThirdEyeWorkerModule(dataSource, config));
    DeprecatedInjectorUtil.setInjector(injector);

    injector.getInstance(ThirdEyeCacheRegistry.class).initializeCaches(config);
    schedulerService = injector.getInstance(SchedulerService.class);

    injector.getInstance(DetectionCronScheduler.class)
        .addToContext(CTX_INJECTOR, injector);

    injector.getInstance(SubscriptionCronScheduler.class)
        .addToContext(CTX_INJECTOR, injector);

    env.lifecycle().manage(lifecycleManager(config));
  }

  private Managed lifecycleManager(ThirdEyeWorkerConfiguration config) {
    return new Managed() {
      @Override
      public void start() throws Exception {

        requestStatisticsLogger = new RequestStatisticsLogger(
            new TimeGranularity(1, TimeUnit.DAYS));
        requestStatisticsLogger.start();

        if (config.isWorker()) {
          taskDriver = injector.getInstance(TaskDriver.class);
          taskDriver.start();
        }

        schedulerService.start();

        if (config.getTeRestConfig() != null) {
          ThirdEyeRestClientConfiguration restClientConfig = config.getTeRestConfig();
          updateAdminSession(restClientConfig.getAdminUser(), restClientConfig.getSessionKey());
        }
      }

      @Override
      public void stop() throws Exception {
        if (requestStatisticsLogger != null) {
          requestStatisticsLogger.shutdown();
        }
        if (taskDriver != null) {
          taskDriver.shutdown();
        }
        schedulerService.stop();
      }
    };
  }

  private void updateAdminSession(String adminUser, String sessionKey) {
    final SessionManager sessionManager = injector.getInstance(SessionManager.class);
    final SessionDTO savedSession = sessionManager.findBySessionKey(sessionKey);
    final long expiryMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365);
    if (savedSession == null) {
      SessionDTO sessionDTO = SessionUtils.buildServiceAccount(adminUser, sessionKey, expiryMillis);
      sessionManager.save(sessionDTO);
    } else {
      savedSession.setExpirationTime(expiryMillis);
      sessionManager.update(savedSession);
    }
  }

  public DatabaseConfiguration getDatabaseConfiguration() {
    final String persistenceConfig = System.getProperty("dw.rootDir") + "/persistence.yml";
    LOG.info("Loading persistence config from [{}]", persistenceConfig);

    final PersistenceConfig configuration = readPersistenceConfig(new File(persistenceConfig));
    return configuration.getDatabaseConfiguration();
  }
}
