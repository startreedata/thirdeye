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

package ai.startree.thirdeye.datasource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
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
  private final Map<String, ThirdEyeDataSourceFactory> dataSourceFactoryMap = new HashMap<>();

  @Inject
  public DataSourcesLoader(
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager) {
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
  }

  public void addThirdEyeDataSourceFactory(ThirdEyeDataSourceFactory f) {
    checkState(!dataSourceFactoryMap.containsKey(f.name()),
        "Duplicate ThirdEyeDataSourceFactory: " + f.name());

    dataSourceFactoryMap.put(f.name(), f);
  }

  public ThirdEyeDataSource loadDataSource(DataSourceDTO dataSource) {
    final String factoryName = dataSource.getType();
    try {
      checkArgument(dataSourceFactoryMap.containsKey(factoryName),
          "Data Source type not loaded: " + factoryName);

      LOG.info("Creating thirdeye datasource type {} with properties '{}'",
          factoryName,
          dataSource.getProperties());

      return dataSourceFactoryMap
          .get(factoryName)
          .build(buildContext(dataSource));
    } catch (Exception e) {
      LOG.error("Exception in creating thirdeye data source type {}", factoryName, e);
    }
    return null;
  }

  private ThirdEyeDataSourceContext buildContext(final DataSourceDTO dataSource) {
    return new ThirdEyeDataSourceContext()
        .setDataSourceDTO(dataSource)
        .setMetricConfigManager(metricConfigManager)
        .setDatasetConfigManager(datasetConfigManager);
  }
}
