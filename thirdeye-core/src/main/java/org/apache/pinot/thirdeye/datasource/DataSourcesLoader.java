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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains helper classes to load/transform datasources from file
 */
@Singleton
public class DataSourcesLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourcesLoader.class);

  /**
   * TODO spyne This is super temporary and will be removed asap. PLEASE DO NOT USE.
   * This is a reverse map to support backward compatibility while loading data sources from
   * config
   */
  @Deprecated
  private static final ImmutableMap<String, String> CLASS_FACTORY_NAME_MAP =
      ImmutableMap.<String, String>builder()
          .put("org.apache.pinot.thirdeye.datasource.pinot.PinotThirdEyeDataSource", "pinot")
          .put("org.apache.pinot.thirdeye.datasource.sql.SqlThirdEyeDataSource", "sql")
          .put("org.apache.pinot.thirdeye.datasource.csv.CSVThirdEyeDataSource", "csv")
          .put("org.apache.pinot.thirdeye.datasource.mock.MockThirdEyeDataSource", "mock")
          .build();

  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final DataSourcesConfiguration dataSourcesConfiguration;
  private final Map<String, ThirdEyeDataSourceFactory> dataSourceFactoryMap = new HashMap<>();

  @Inject
  public DataSourcesLoader(
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final DataSourcesConfiguration dataSourcesConfiguration) {
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.dataSourcesConfiguration = dataSourcesConfiguration;
  }

  public void addThirdEyeDataSourceFactory(ThirdEyeDataSourceFactory f) {
    checkState(!dataSourceFactoryMap.containsKey(f.name()),
        "Duplicate ThirdEyeDataSourceFactory: " + f.name());

    dataSourceFactoryMap.put(f.name(), f);
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
          requireNonNull(CLASS_FACTORY_NAME_MAP.get(ds.getClassName())),
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
      final String factoryName,
      final Map<String, Object> properties) {
    try {
      checkArgument(dataSourceFactoryMap.containsKey(factoryName),
          "Data Source type not loaded: " + factoryName);

      LOG.info("Creating thirdeye datasource type {} with properties '{}'",
          factoryName,
          properties);

      return dataSourceFactoryMap
          .get(factoryName)
          .build(buildContext(properties));
    } catch (Exception e) {
      LOG.error("Exception in creating thirdeye data source type {}", factoryName, e);
    }
    return null;
  }

  private ThirdEyeDataSourceContext buildContext(final Map<String, Object> properties) {
    return new ThirdEyeDataSourceContext()
        .setProperties(properties)
        .setMetricConfigManager(metricConfigManager)
        .setDatasetConfigManager(datasetConfigManager);
  }
}
