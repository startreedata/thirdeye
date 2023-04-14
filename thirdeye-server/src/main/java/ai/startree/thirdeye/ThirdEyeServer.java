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
package ai.startree.thirdeye;

import static ai.startree.thirdeye.spi.Constants.CTX_INJECTOR;
import static ai.startree.thirdeye.spi.Constants.ENV_THIRDEYE_PLUGINS_DIR;
import static ai.startree.thirdeye.spi.Constants.SYS_PROP_THIRDEYE_PLUGINS_DIR;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.AuthDisabledRequestFilter;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.DataSourceBuilder;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.healthcheck.DatabaseHealthCheck;
import ai.startree.thirdeye.json.ThirdEyeJsonProcessingExceptionMapper;
import ai.startree.thirdeye.resources.RootResource;
import ai.startree.thirdeye.scheduler.DetectionCronScheduler;
import ai.startree.thirdeye.scheduler.SchedulerService;
import ai.startree.thirdeye.scheduler.SubscriptionCronScheduler;
import ai.startree.thirdeye.scheduler.events.MockEventsLoader;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import ai.startree.thirdeye.worker.task.TaskDriver;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeServer extends Application<ThirdEyeServerConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeServer.class);

  private Injector injector;
  private TaskDriver taskDriver = null;
  private SchedulerService schedulerService = null;

  /**
   * Use {@link ThirdEyeServerDebug} class for debugging purposes.
   * The integration-tests/tools module will load all the thirdeye jars including datasources
   * making it easier to debug.
   */
  public static void main(String[] args) throws Exception {
    ServerUtils.logJvmSettings();

    new ThirdEyeServer().run(args);
  }

  @Override
  public void initialize(final Bootstrap<ThirdEyeServerConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
            new EnvironmentVariableSubstitutor()));
    bootstrap.addBundle(new SwaggerBundle<ThirdEyeServerConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          ThirdEyeServerConfiguration configuration) {
        return configuration.getSwaggerBundleConfiguration();
      }
    });
    bootstrap.getObjectMapper().registerModule(ThirdEyeSerialization.TEMPLATABLE);
  }

  @Override
  public void run(final ThirdEyeServerConfiguration configuration, final Environment env) {

    final DataSource dataSource = new DataSourceBuilder()
        .build(configuration.getDatabaseConfiguration());

    injector = Guice.createInjector(new ThirdEyeServerModule(
        configuration,
        dataSource,
        env.metrics()));

    // Load plugins
    optional(thirdEyePluginDirOverride())
        .ifPresent(pluginsPath -> injector
            .getInstance(PluginLoaderConfiguration.class)
            .setPluginsPath(pluginsPath));

    loadPlugins();

    env.jersey().register(injector.getInstance(RootResource.class));
    env.jersey().register(new ThirdEyeJsonProcessingExceptionMapper());

    // Expose dropwizard metrics in prometheus compatible format
    if (configuration.getPrometheusConfiguration().isEnabled()) {
      CollectorRegistry collectorRegistry = new CollectorRegistry();
      collectorRegistry.register(new DropwizardExports(env.metrics()));
      env.admin()
          .addServlet("prometheus", new MetricsServlet(collectorRegistry))
          .addMapping("/prometheus");
    }

    // Persistence layer connectivity health check registry
    env.healthChecks().register("database", injector.getInstance(DatabaseHealthCheck.class));

    registerAuthFilter(injector, env.jersey());

    // Enable CORS. Opens up the API server to respond to requests from all external domains.
    addCorsFilter(env);

    // Load mock events if enabled.
    injector.getInstance(MockEventsLoader.class).run();
    env.lifecycle().manage(lifecycleManager(configuration));
  }

  protected void loadPlugins() {
    injector.getInstance(PluginLoader.class).loadPlugins();
  }

  private Managed lifecycleManager(ThirdEyeServerConfiguration config) {
    return new Managed() {
      @Override
      public void start() throws Exception {
        if (config.getSchedulerConfiguration().isEnabled()) {
          // Allow the jobs to use the injector
          injector.getInstance(DetectionCronScheduler.class)
              .addToContext(CTX_INJECTOR, injector);

          injector.getInstance(SubscriptionCronScheduler.class)
              .addToContext(CTX_INJECTOR, injector);

          schedulerService = injector.getInstance(SchedulerService.class);

          // Start the scheduler
          schedulerService.start();
        }

        if (config.getTaskDriverConfiguration().isEnabled()) {
          taskDriver = injector.getInstance(TaskDriver.class);
          taskDriver.start();
        }
      }

      @Override
      public void stop() throws Exception {
        if (taskDriver != null) {
          taskDriver.shutdown();
        }
        if (schedulerService != null) {
          schedulerService.stop();
        }

        /* Shutdown the Plan Executor threads */
        injector.getInstance(PlanExecutor.class).close();
      }
    };
  }

  /**
   * Prefer system property over env variable when overriding plugins Dir.
   *
   * @return overridden thirdEye plugins dir
   */
  private String thirdEyePluginDirOverride() {
    return optional(System.getProperty(SYS_PROP_THIRDEYE_PLUGINS_DIR))
        .orElse(System.getenv(ENV_THIRDEYE_PLUGINS_DIR));
  }

  void addCorsFilter(final Environment environment) {
    final FilterRegistration.Dynamic cors =
        environment.servlets().addFilter("CORS", CrossOriginFilter.class);

    // Configure CORS parameters
    cors.setInitParameter("allowedOrigins", "*");
    cors.setInitParameter("allowedHeaders",
        "Authorization,X-Requested-With,Content-Type,Accept,Origin,Accept-Version");
    cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

    // Add URL mapping
    cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
  }

  private void registerAuthFilter(final Injector injector, final JerseyEnvironment jersey) {
    try {
      if (!injector.getInstance(AuthConfiguration.class).isEnabled()) {
        jersey.register(injector.getInstance(AuthDisabledRequestFilter.class));
      }
      jersey.register(new AuthDynamicFeature(injector.getInstance(AuthFilter.class)));
      jersey.register(RolesAllowedDynamicFeature.class);
      jersey.register(new AuthValueFactoryProvider.Binder<>(ThirdEyePrincipal.class));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to configure Authentication filter", e);
    }
  }

  /**
   * Maintained for enabling debug tools.
   *
   * @return injector instance used by the coordinator.
   */
  public Injector getInjector() {
    return injector;
  }
}
