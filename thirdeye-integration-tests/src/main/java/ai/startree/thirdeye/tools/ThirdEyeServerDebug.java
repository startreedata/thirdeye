package ai.startree.thirdeye.tools;

import static ai.startree.thirdeye.AppUtils.logJvmSettings;

import ai.startree.thirdeye.ThirdEyeServer;
import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.datasource.DefaultDataSourcesPlugin;
import ai.startree.thirdeye.datasource.PinotDataSourcePlugin;
import ai.startree.thirdeye.detection.annotation.registry.DetectionRegistry;
import ai.startree.thirdeye.detection.components.DetectionComponentsPlugin;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.notification.email.EmailNotificationServiceFactory;
import ai.startree.thirdeye.notification.webhook.WebhookNotificationServiceFactory;
import com.google.inject.Injector;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeServerDebug {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeServerDebug.class);

  public static void main(String[] args) throws Exception {
    logJvmSettings();

    final ThirdEyeServer thirdEyeServer = new ThirdEyeServer();
    thirdEyeServer.run(args);

    final Injector injector = thirdEyeServer.getInjector();

    loadDefaultDataSources(injector.getInstance(DataSourcesLoader.class));
    loadDetectors(injector.getInstance(DetectionRegistry.class));
    loadNotificationServiceFactories(injector.getInstance(NotificationServiceRegistry.class));
  }

  /**
   * NOTE!
   * Default Data sources module is packaged as a plugin and therefore not available in the
   * application. This module has data sources added into it for dev purposes. Any changes WILL
   * require eventual testing with the final distribution.
   */
  static void loadDefaultDataSources(final DataSourcesLoader dataSourcesLoader) {
    // Load the default data sources.
    // If there are duplicate additions, this will throw an error.
    Stream.of(
            new DefaultDataSourcesPlugin(),
            new PinotDataSourcePlugin())
        .forEach(plugin -> plugin
            .getDataSourceFactories()
            .forEach(dataSourcesLoader::addThirdEyeDataSourceFactory));
  }

  static void loadDetectors(final DetectionRegistry detectionRegistry) {
    // Grab the instance from the coordinator

    // Load the default data sources.
    // If there are duplicate additions, this will throw an error.
    final DetectionComponentsPlugin detectionComponentsPlugin = new DetectionComponentsPlugin();

    detectionComponentsPlugin
        .getAnomalyDetectorFactories()
        .forEach(detectionRegistry::addAnomalyDetectorV2Factory);

    detectionComponentsPlugin
        .getEventTriggerFactories()
        .forEach(detectionRegistry::addEventTriggerFactory);
  }

  static void loadNotificationServiceFactories(final NotificationServiceRegistry instance) {
    instance.addNotificationServiceFactory(new WebhookNotificationServiceFactory());
    instance.addNotificationServiceFactory(new EmailNotificationServiceFactory());
  }
}
