package org.apache.pinot.thirdeye.tools;

import static org.apache.pinot.thirdeye.AppUtils.logJvmSettings;

import com.google.inject.Injector;
import org.apache.pinot.thirdeye.ThirdEyeCoordinator;
import org.apache.pinot.thirdeye.datasource.DataSourcesLoader;
import org.apache.pinot.thirdeye.datasource.DefaultDataSourcesPlugin;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.plugin.detection.DetectionComponentsPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeCoordinatorDebug {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeCoordinatorDebug.class);

  public static void main(String[] args) throws Exception {
    logJvmSettings();

    final ThirdEyeCoordinator thirdEyeCoordinator = new ThirdEyeCoordinator();
    thirdEyeCoordinator.run(args);

    final Injector injector = thirdEyeCoordinator.getInjector();

    loadDefaultDataSources(injector.getInstance(DataSourcesLoader.class));
    loadDetectors(injector.getInstance(DetectionRegistry.class));
  }

  /**
   * NOTE!
   * Default Data sources module is packaged as a plugin and therefore not available in the
   * application. This module has data sources added into it for dev purposes. Any changes WILL
   * require eventual testing with the final distribution.
   *
   * @param dataSourcesLoader
   */
  static void loadDefaultDataSources(final DataSourcesLoader dataSourcesLoader) {
    // Load the default data sources.
    // If there are duplicate additions, this will throw an error.
    new DefaultDataSourcesPlugin()
        .getDataSourceFactories()
        .forEach(dataSourcesLoader::addThirdEyeDataSourceFactory);
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
        .getAnomalyDetectorV2Factories()
        .forEach(detectionRegistry::addAnomalyDetectorV2Factory);

    detectionComponentsPlugin
        .getEventTriggerFactories()
        .forEach(detectionRegistry::addEventTriggerFactory);
  }
}
