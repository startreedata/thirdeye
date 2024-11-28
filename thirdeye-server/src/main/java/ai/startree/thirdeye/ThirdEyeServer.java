/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.spi.Constants.ENV_THIRDEYE_PLUGINS_DIR;
import static ai.startree.thirdeye.spi.Constants.SYS_PROP_THIRDEYE_PLUGINS_DIR;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.AuthDisabledRequestFilter;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.config.BackendSentryConfiguration;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.DataSourceBuilder;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.healthcheck.DatabaseHealthCheck;
import ai.startree.thirdeye.json.ThirdEyeJsonProcessingExceptionMapper;
import ai.startree.thirdeye.resources.root.RootResource;
import ai.startree.thirdeye.scheduler.SchedulerService;
import ai.startree.thirdeye.scheduler.events.MockEventsLoader;
import ai.startree.thirdeye.service.ResourcesBootstrapService;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import ai.startree.thirdeye.worker.task.TaskDriver;
import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.spi.DefaultBindingScopingVisitor;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.Managed;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jersey.server.DefaultJerseyTagsProvider;
import io.micrometer.core.instrument.binder.jersey.server.MetricsApplicationEventListener;
import io.micrometer.core.instrument.binder.jetty.JettyConnectionMetrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.sentry.Hint;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.logback.SentryAppender;
import java.util.EnumSet;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.ws.rs.WebApplicationException;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeServer extends Application<ThirdEyeServerConfiguration> {

  private static Logger log = LoggerFactory.getLogger(ThirdEyeServer.class);
  private static final String SENTRY_MAIN_THREAD_HINT_KEY = "IS_MAIN_THREAD_ERROR";

  private Injector injector;
  private TaskDriver taskDriver = null;
  private SchedulerService schedulerService = null;

  /**
   * Use {@link ThirdEyeServerDebug} class for debugging purposes.
   * The integration-tests/tools module will load all the thirdeye jars including datasources
   * making it easier to debug.
   */
  public static void main(final String[] args) throws Exception {
    ServerUtils.logJvmSettings();

    new ThirdEyeServer().run(args);
  }

  @Override
  public void initialize(final Bootstrap<ThirdEyeServerConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
            new EnvironmentVariableSubstitutor()));
    bootstrap.addBundle(new SwaggerBundle<>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          final ThirdEyeServerConfiguration configuration) {
        return configuration.getSwaggerBundleConfiguration();
      }
    });
    bootstrap.getObjectMapper()
        .registerModule(new JodaModule())
        .registerModule(Constants.TEMPLATABLE);
  }

  @Override
  protected void onFatalError(final Throwable t) {
    // dropwizard catches an exception that makes the main thread stop - so it is not caught by sentry - catch it in sentry manually here
    final Hint hint = new Hint();
    hint.set(SENTRY_MAIN_THREAD_HINT_KEY, new Object());
    Sentry.captureException(t, hint);
    super.onFatalError(t);
  }

  @Override
  public void run(final ThirdEyeServerConfiguration configuration, final Environment env) {
    initSentry(configuration.getSentryConfiguration(), env.jersey());
    initMetrics(configuration, env);

    final DataSource dataSource = new DataSourceBuilder()
        .build(configuration.getDatabaseConfiguration());

    injector = Guice.createInjector(new ThirdEyeServerModule(configuration, dataSource));

    // Load plugins
    optional(thirdEyePluginDirOverride())
        .ifPresent(pluginsPath -> injector
            .getInstance(PluginLoaderConfiguration.class)
            .setPluginsPath(pluginsPath));

    loadPlugins();

    registerResources(env.jersey());
    env.jersey().register(new ThirdEyeJsonProcessingExceptionMapper());

    // Persistence layer connectivity health check registry
    env.healthChecks().register("database", injector.getInstance(DatabaseHealthCheck.class));

    registerAuthFilter(injector, env.jersey());

    // Enable CORS. Opens up the API server to respond to requests from all external domains.
    addCorsFilter(env);

    // Load mock events if enabled.
    injector.getInstance(MockEventsLoader.class).run();
    env.lifecycle().manage(lifecycleManager(configuration));
  }

  private void initMetrics(final ThirdEyeServerConfiguration configuration, final Environment env) {
    final String environmentUrl = optional(configuration.getUiConfiguration().getExternalUrl())
        .map(s -> s.replaceAll("https?://", ""))
        .orElse("unknown");
    // Expose dropwizard metrics in prometheus compatible format
    if (configuration.getPrometheusConfiguration().isEnabled()) {
      final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(
          PrometheusConfig.DEFAULT);
      Metrics.globalRegistry.add(registry);
      Metrics.globalRegistry.config().commonTags("environment_url", environmentUrl);
      // add jersey instrumentation
      env.jersey()
          .getResourceConfig()
          .register(new MetricsApplicationEventListener(Metrics.globalRegistry,
              new DefaultJerseyTagsProvider(), "http.server.requests", true));
      // add jetty instrumentation
      env.lifecycle().addServerLifecycleListener(server -> {
        for (final Connector c : server.getConnectors()) {
          c.addBean(new JettyConnectionMetrics(Metrics.globalRegistry, c));
        }
      });
      // add jvm metrics
      new ClassLoaderMetrics().bindTo(registry);
      new JvmMemoryMetrics().bindTo(registry);
      // should not be closed here - but should be closed when the app stops. In TE case it's fine not to close it, app stop corresponds to JVM stop.
      new JvmGcMetrics().bindTo(registry);
      new ProcessorMetrics().bindTo(registry);
      new JvmThreadMetrics().bindTo(registry);

      env.admin()
          .addServlet("prometheus", new MicrometerPrometheusServlet(registry))
          .addMapping("/prometheus");
    }
    Gauge.builder("thirdeye_version_info", () -> 1)
        .tag("semver", packageSemver())
        .tag("git_hash", packageGitHash())
        .register(Metrics.globalRegistry);
  }

  protected void registerResources(final JerseyEnvironment jersey) {
    jersey.register(injector.getInstance(RootResource.class));
  }

  protected void loadPlugins() {
    injector.getInstance(PluginLoader.class).loadPlugins();
  }

  private Managed lifecycleManager(final ThirdEyeServerConfiguration config) {
    return new Managed() {
      @Override
      public void start() throws Exception {
        if (config.getSchedulerConfiguration().isEnabled()) {
          schedulerService = injector.getInstance(SchedulerService.class);

          // bootstrap resources before starting the scheduler
          // bootstrapping runs on the main thread. If it fails, the scheduler will not start.
          injector.getInstance(ResourcesBootstrapService.class)
              .bootstrap(AuthorizationManager.getInternalValidPrincipal());

          registerDatabaseMetricsOfAllDaos(injector);

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

  private void registerDatabaseMetricsOfAllDaos(final Injector injector) {
    for(final Map.Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet()) {
      final Binding<?> binding = entry.getValue();
      if (AbstractManager.class.isAssignableFrom(
          entry.getKey().getTypeLiteral().getRawType())) {
        binding.acceptScopingVisitor(new DefaultBindingScopingVisitor<Void>() {
          @Override public Void visitEagerSingleton() {
            final AbstractManager instance = (AbstractManager) (binding.getProvider().get());
            instance.registerDatabaseMetrics();
            return null;
          }
        });
      }
    }
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
      jersey.register(new AuthValueFactoryProvider.Binder<>(ThirdEyeServerPrincipal.class));
    } catch (final Exception e) {
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

  private void initSentry(final BackendSentryConfiguration config, final JerseyEnvironment jersey) {
    if (config.getDsn() != null && !config.getDsn().isBlank()) {
      // start sentry - see https://docs.sentry.io/platforms/java/usage/
      Sentry.init(options -> {
        options.setDsn(config.getDsn());
        // by default sentry catches uncaught exception, so they are not shown in stdout. Force print them in stdout 
        options.setBeforeSend((sentryEvent, hint) -> {
          // if the exception is not on the main thread, print it. If it's on the main thread dropwizard prints it already
          if (hint.get(SENTRY_MAIN_THREAD_HINT_KEY) == null) {
            optional(sentryEvent.getThrowable()).ifPresent(Throwable::printStackTrace);
          }
          optional(sentryEvent.getThrowable())
              .filter(t -> t instanceof WebApplicationException)
              .map(t -> ((WebApplicationException) t).getResponse())
              .map(r -> String.valueOf(r.getStatus()))
              .ifPresent(status_code -> sentryEvent.setTag("http_status_code", status_code));
          return sentryEvent;
        });
        options.setRelease(packageSemver());
        options.setEnvironment(config.getEnvironment());
        config.getTags().forEach(options::setTag);
        // Enable Sentry SDK logs for error level - to know if sentry has errors
        options.setDebug(true);
        options.setDiagnosticLevel(SentryLevel.ERROR);
      });
      // intercept exceptions caught by dropwizard/jersey
      jersey.register(new ExceptionSentryLogger());

      // enable sentry log collect - see https://docs.sentry.io/platforms/java/guides/logback/
      final SentryAppender appender = new SentryAppender();
      appender.setMinimumEventLevel(Level.ERROR);
      appender.setMinimumBreadcrumbLevel(Level.DEBUG);
      appender.start();
      final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
          Logger.ROOT_LOGGER_NAME);
      rootLogger.addAppender(appender);
      // re-instantiate the logger of this class now that the Sentry appender has been injected
      log = LoggerFactory.getLogger(ThirdEyeServer.class);
      log.info("Sentry.io collect is enabled.");
    } else {
      log.info("Sentry.io collect is not enabled.");
    }
  }

  private String packageSemver() {
    // assuming format is "x.y.z-2c2commitHash"
    final String fullPackageVersion = this.getClass().getPackage().getImplementationVersion();
    if (fullPackageVersion == null) {
      return "unknown";
    }
    final int indexOfCommitHashStart = fullPackageVersion.indexOf("-");
    if (indexOfCommitHashStart > 0) {
      return fullPackageVersion.substring(0, indexOfCommitHashStart);
    }
    return fullPackageVersion;
  }

  private String packageGitHash() {
    // assuming format is "x.y.z-2c2commitHash"
    final String fullPackageVersion = this.getClass().getPackage().getImplementationVersion();
    if (fullPackageVersion == null) {
      return "unknown";
    }
    final int indexOfCommitHashStart = fullPackageVersion.indexOf("-");
    if (indexOfCommitHashStart > 0) {
      return fullPackageVersion.substring(indexOfCommitHashStart + 1);
    }
    return fullPackageVersion;
  }
}
