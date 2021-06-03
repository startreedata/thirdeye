package org.apache.pinot.thirdeye;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.apache.pinot.thirdeye.anomaly.events.MockEventsLoader;
import org.apache.pinot.thirdeye.datalayer.DataSourceBuilder;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.detection.cache.CacheConfig;
import org.apache.pinot.thirdeye.resources.RootResource;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeCoordinator extends Application<ThirdEyeCoordinatorConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeCoordinator.class);

  /**
   * Use {@link ThirdEyeCoordinatorDebug} class for debugging purposes.
   * The integration-tests/tools module will load all the thirdeye jars including datasources
   * making it easier to debug.
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    log.info(String.format("JVM arguments: %s", runtimeMxBean.getInputArguments()));

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

    final Injector injector = Guice.createInjector(new ThirdEyeCoordinatorModule(
        configuration,
        dataSource,
        env.metrics()));

    // TODO remove hack and CacheConfig singleton
    CacheConfig.setINSTANCE(injector.getInstance(CacheConfig.class));

    // Initialize ThirdEyeCacheRegistry
    injector
        .getInstance(ThirdEyeCacheRegistry.class)
        .initializeCaches();

    env.jersey().register(injector.getInstance(RootResource.class));

    // Enable CORS. Opens up the API server to respond to requests from all external domains.
    addCorsFilter(env);

    // Load mock events if enabled.
    injector.getInstance(MockEventsLoader.class).run();
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
}
