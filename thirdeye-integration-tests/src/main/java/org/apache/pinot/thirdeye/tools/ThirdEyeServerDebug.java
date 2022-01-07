package org.apache.pinot.thirdeye.tools;

import static org.apache.pinot.thirdeye.AppUtils.logJvmSettings;

import com.google.inject.Injector;
import java.util.stream.Stream;
import org.apache.pinot.thirdeye.ThirdEyeServer;
import org.apache.pinot.thirdeye.datasource.DataSourcesLoader;
import org.apache.pinot.thirdeye.datasource.DefaultDataSourcesPlugin;
import org.apache.pinot.thirdeye.datasource.PinotDataSourcePlugin;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.detection.components.DetectionComponentsPlugin;
import org.apache.pinot.thirdeye.notification.NotificationServiceRegistry;
import org.apache.pinot.thirdeye.notification.email.EmailNotificationServiceFactory;
import org.apache.pinot.thirdeye.notification.webhook.WebhookNotificationServiceFactory;
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
        .getAnomalyDetectorV2Factories()
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
