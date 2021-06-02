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

import static com.google.common.base.Preconditions.checkState;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
  public Map<String, ThirdEyeDataSource> getDataSourceMapFromConfig() {
    final List<DataSourceConfig> dataSourceConfigs = dataSourcesConfiguration.getDataSourceConfigs();
    if (!optional(dataSourceConfigs)
        .filter(l -> l.size() > 0)
        .isPresent()) {
      return Collections.emptyMap();
    }
    return loadDataSources(dataSourceConfigs);
  }

  private Map<String, ThirdEyeDataSource> loadDataSources(
      final List<DataSourceConfig> dataSourceConfigs) {
    final Map<String, ThirdEyeDataSource> dataSourceMap = new HashMap<>();
    for (DataSourceConfig ds : dataSourceConfigs) {
      final ThirdEyeDataSource thirdEyeDataSource = loadDataSource(
          ds.getClassName(),
          ds.getProperties());

      if (thirdEyeDataSource != null) {
        final String name = thirdEyeDataSource.getName();
        checkState(!dataSourceMap.containsKey(name), "Data source " + name + " already exists.");
        dataSourceMap.put(name, thirdEyeDataSource);
      }
    }
    return dataSourceMap;
  }

  public ThirdEyeDataSource loadDataSource(
      final String classRef,
      final Map<String, Object> properties) {
    try {
      LOG.info("Loading thirdeye datasource {} with properties '{}'", classRef, properties);
      final Constructor<?> constructor = Class.forName(classRef).getConstructor();
      final ThirdEyeDataSource thirdeyeDataSource = (ThirdEyeDataSource) constructor
          .newInstance();
      thirdeyeDataSource.init(new ThirdEyeDataSourceContext()
          .setProperties(properties)
          .setMetricConfigManager(metricConfigManager)
          .setDatasetConfigManager(datasetConfigManager)
      );
      return thirdeyeDataSource;
      // use class simple name as key, this enforces that there cannot be more than one data source of the same type
    } catch (Exception e) {
      LOG.error("Exception in creating thirdeye data source {}", classRef, e);
    }
    return null;
  }
}
