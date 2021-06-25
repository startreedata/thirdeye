package org.apache.pinot.thirdeye;

import static org.apache.pinot.thirdeye.spi.Constants.CTX_INJECTOR;
import static org.apache.pinot.thirdeye.spi.Constants.ENV_THIRDEYE_PLUGINS_DIR;
import static org.apache.pinot.thirdeye.spi.Constants.SYS_PROP_THIRDEYE_PLUGINS_DIR;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.datalayer.DataSourceBuilder;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.detection.anomaly.events.MockEventsLoader;
import org.apache.pinot.thirdeye.detection.cache.CacheConfig;
import org.apache.pinot.thirdeye.resources.RootResource;
import org.apache.pinot.thirdeye.scheduler.DetectionCronScheduler;
import org.apache.pinot.thirdeye.scheduler.SchedulerService;
import org.apache.pinot.thirdeye.scheduler.SubscriptionCronScheduler;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.task.TaskDriver;
import org.apache.pinot.thirdeye.tracking.RequestStatisticsLogger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeCoordinator extends Application<ThirdEyeCoordinatorConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeCoordinator.class);

  private Injector injector;
  private RequestStatisticsLogger requestStatisticsLogger = null;
  private TaskDriver taskDriver = null;
  private SchedulerService schedulerService = null;

  /**
   * Use {@link ThirdEyeCoordinatorDebug} class for debugging purposes.
   * The integration-tests/tools module will load all the thirdeye jars including datasources
   * making it easier to debug.
   */
  public static void main(String[] args) throws Exception {
    AppUtils.logJvmSettings();

    new ThirdEyeCoordinator().run(args);
  }

  @Override
  public void initialize(final Bootstrap<ThirdEyeCoordinatorConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
            new EnvironmentVariableSubstitutor()));
    bootstrap.addBundle(new SwaggerBundle<ThirdEyeCoordinatorConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          ThirdEyeCoordinatorConfiguration configuration) {
        return configuration.getSwaggerBundleConfiguration();
      }
    });
  }

  @Override
  public void run(final ThirdEyeCoordinatorConfiguration configuration, final Environment env) {

    final DataSource dataSource = new DataSourceBuilder()
        .build(configuration.getDatabaseConfiguration());

    injector = Guice.createInjector(new ThirdEyeCoordinatorModule(
        configuration,
        dataSource,
        env.metrics()));

    // TODO remove hack and CacheConfig singleton
    CacheConfig.setINSTANCE(injector.getInstance(CacheConfig.class));

    // Load plugins
    optional(thirdEyePluginDirOverride())
        .ifPresent(pluginsPath -> injector
            .getInstance(PluginLoaderConfiguration.class)
            .setPluginsPath(pluginsPath));

    injector.getInstance(PluginLoader.class).loadPlugins();

    // Initialize ThirdEyeCacheRegistry
    injector
        .getInstance(ThirdEyeCacheRegistry.class)
        .initializeCaches();

    env.jersey().register(injector.getInstance(RootResource.class));

    // Enable CORS. Opens up the API server to respond to requests from all external domains.
    addCorsFilter(env);

    // Load mock events if enabled.
    injector.getInstance(MockEventsLoader.class).run();
    env.lifecycle().manage(lifecycleManager(configuration));
  }

  private Managed lifecycleManager(ThirdEyeCoordinatorConfiguration config) {
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

        requestStatisticsLogger = new RequestStatisticsLogger(
            new TimeGranularity(1, TimeUnit.DAYS));
        requestStatisticsLogger.start();

        if (config.getTaskDriverConfiguration().isEnabled()) {
          taskDriver = injector.getInstance(TaskDriver.class);
          taskDriver.start();
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
        if (schedulerService != null) {
          schedulerService.stop();
        }
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

  /**
   * Maintained for enabling debug tools.
   *
   * @return injector instance used by the coordinator.
   */
  public Injector getInjector() {
    return injector;
  }
}
