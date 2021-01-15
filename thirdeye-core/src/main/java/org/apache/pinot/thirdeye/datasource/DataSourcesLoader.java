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

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains helper classes to load/transform datasources from file
 */
public class DataSourcesLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourcesLoader.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  /**
   * Returns datasources config from yml file
   *
   * @param dataSourcesUrl URL to data sources config
   * @return DataSources
   */
  public DataSources fromDataSourcesUrl(URL dataSourcesUrl) {
    requireNonNull(dataSourcesUrl, "dataSourcesUrl is null!");
    try {
      return requireNonNull(OBJECT_MAPPER.readValue(dataSourcesUrl, DataSources.class),
          "dataSources is null");
    } catch (IOException e) {
      LOG.error("Exception in reading data sources file {}", dataSourcesUrl, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns datasource name to datasource map
   */
  public Map<String, ThirdEyeDataSource> getDataSourceMap(DataSources dataSources) {
    Map<String, ThirdEyeDataSource> dataSourceMap = new HashMap<>();
    if (!optional(dataSources.getDataSourceConfigs()).filter(l -> l.size() > 0).isPresent()) {
      return dataSourceMap;
    }
    for (DataSourceConfig dataSourceConfig : dataSources.getDataSourceConfigs()) {
      String className = dataSourceConfig.getClassName();
      Map<String, Object> properties = dataSourceConfig.getProperties();
      try {
        LOG.info("Creating thirdeye datasource {} with properties '{}'", className, properties);
        Constructor<?> constructor = Class.forName(className).getConstructor(Map.class);
        ThirdEyeDataSource thirdeyeDataSource = (ThirdEyeDataSource) constructor
            .newInstance(properties);
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
