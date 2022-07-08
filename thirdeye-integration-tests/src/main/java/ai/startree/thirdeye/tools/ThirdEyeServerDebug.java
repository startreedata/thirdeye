/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.tools;

import static ai.startree.thirdeye.AppUtils.logJvmSettings;

import ai.startree.thirdeye.ThirdEyeServer;
import ai.startree.thirdeye.bootstrap.BootstrapResourcesRegistry;
import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.detection.annotation.registry.DetectionRegistry;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.plugins.bootstrap.opencore.OpenCoreBoostrapResourcesProviderPlugin;
import ai.startree.thirdeye.plugins.datasource.DefaultDataSourcesPlugin;
import ai.startree.thirdeye.plugins.datasource.PinotDataSourcePlugin;
import ai.startree.thirdeye.plugins.detection.components.DetectionComponentsPlugin;
import ai.startree.thirdeye.plugins.detectors.DetectorsPlugin;
import ai.startree.thirdeye.plugins.notification.email.EmailSendgridNotificationServiceFactory;
import ai.startree.thirdeye.plugins.notification.email.EmailSmtpNotificationServiceFactory;
import ai.startree.thirdeye.plugins.rca.contributors.simple.SimpleContributorsFinderPlugin;
import ai.startree.thirdeye.rootcause.ContributorsFinderRunner;
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
    loadContributorsFinderFactories(injector.getInstance(ContributorsFinderRunner.class));
    loadBootstrapResourcesProviderFactories(injector.getInstance(BootstrapResourcesRegistry.class));
  }

  private static void loadBootstrapResourcesProviderFactories(
      final BootstrapResourcesRegistry bootstrapResourcesRegistry) {
    Stream.of(
            new OpenCoreBoostrapResourcesProviderPlugin()
        )
        .forEach(plugin -> plugin.getBootstrapResourcesProviderFactories()
            .forEach(bootstrapResourcesRegistry::addBootstrapResourcesProviderFactory));
  }

  private static void loadContributorsFinderFactories(
      final ContributorsFinderRunner contributorsFinderRunner) {
    Stream
        .of(new SimpleContributorsFinderPlugin())
        .forEach(plugin -> plugin.getContributorsFinderFactories()
            .forEach(contributorsFinderRunner::addContributorsFinderFactory));
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
        .forEach(detectionRegistry::addAnomalyDetectorFactory);

    detectionComponentsPlugin
        .getEventTriggerFactories()
        .forEach(detectionRegistry::addEventTriggerFactory);

    new DetectorsPlugin()
        .getAnomalyDetectorFactories()
        .forEach(detectionRegistry::addAnomalyDetectorFactory);
  }

  static void loadNotificationServiceFactories(final NotificationServiceRegistry instance) {
    Stream.of(
        new EmailSmtpNotificationServiceFactory(),
        new EmailSendgridNotificationServiceFactory()
    ).forEach(instance::addNotificationServiceFactory);
  }
}
