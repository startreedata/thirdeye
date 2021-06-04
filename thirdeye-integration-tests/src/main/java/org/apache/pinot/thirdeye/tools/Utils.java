package org.apache.pinot.thirdeye.tools;

import com.google.inject.Injector;
import org.apache.pinot.thirdeye.datasource.DataSourcesLoader;
import org.apache.pinot.thirdeye.datasource.DefaultDataSourcesPlugin;

public class Utils {

  /**
   * NOTE!
   * Default Data sources module is packaged as a plugin and therefore not available in the
   * application. This module has data sources added into it for dev purposes. Any changes WILL
   * require eventual testing with the final distribution.
   *
   * @param injector The coordinator injector.
   */
  static void loadDefaultDataSources(final Injector injector) {
    // Grab the instance from the coordinator
    final DataSourcesLoader dataSourcesLoader = injector.getInstance(DataSourcesLoader.class);

    // Load the default data sources.
    // If there are duplicate additions, this will throw an error.
    new DefaultDataSourcesPlugin()
        .getDataSourceFactories()
        .forEach(dataSourcesLoader::addThirdEyeDataSourceFactory);
  }
}
