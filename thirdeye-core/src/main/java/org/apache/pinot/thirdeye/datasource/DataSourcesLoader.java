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

package org.apache.pinot.thirdeye.datasource;

import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains helper classes to load/transform datasources from file
 */
@Singleton
public class DataSourcesLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourcesLoader.class);

  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final DataSourcesConfiguration dataSourcesConfiguration;

  @Inject
  public DataSourcesLoader(
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final DataSourcesConfiguration dataSourcesConfiguration) {
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.dataSourcesConfiguration = dataSourcesConfiguration;
  }

  /**
   * Returns datasource name to datasource map
   */
  public Map<String, ThirdEyeDataSource> getDataSourceMap() {
    Map<String, ThirdEyeDataSource> dataSourceMap = new HashMap<>();
    if (!optional(dataSourcesConfiguration.getDataSourceConfigs()).filter(l -> l.size() > 0)
        .isPresent()) {
      return dataSourceMap;
    }
    for (DataSourceConfig dataSourceConfig : dataSourcesConfiguration.getDataSourceConfigs()) {
      String className = dataSourceConfig.getClassName();
      Map<String, Object> properties = dataSourceConfig.getProperties();
      try {
        LOG.info("Creating thirdeye datasource {} with properties '{}'", className, properties);
        Constructor<?> constructor = Class.forName(className).getConstructor();
        ThirdEyeDataSource thirdeyeDataSource = (ThirdEyeDataSource) constructor
            .newInstance();
        thirdeyeDataSource.init(new ThirdEyeDataSourceContext()
            .setProperties(properties)
            .setMetricConfigManager(metricConfigManager)
            .setDatasetConfigManager(datasetConfigManager)
        );
        // use class simple name as key, this enforces that there cannot be more than one data source of the same type
        String name = thirdeyeDataSource.getName();
        if (dataSourceMap.containsKey(name)) {
          throw new IllegalStateException("Data source " + name + " already exists.");
        }
        dataSourceMap.put(name, thirdeyeDataSource);
      } catch (Exception e) {
        LOG.error("Exception in creating thirdeye data source {}", className, e);
      }
    }
    return dataSourceMap;
  }
}
