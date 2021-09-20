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

package org.apache.pinot.thirdeye.datasource.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.AutoOnboard;
import org.apache.pinot.thirdeye.spi.datasource.DataSourceUtils;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans mock configs and creates metrics for MockThirdEyeDataSource
 */
public class AutoOnboardMockDataSource extends AutoOnboard {

  private static final Logger LOG = LoggerFactory.getLogger(AutoOnboardMockDataSource.class);

  private final MetricConfigManager metricDAO;
  private final DatasetConfigManager datasetDAO;
  private final String dataSourceName;

  /**
   * Constructor for dependency injection
   *
   * @param meta meta data source config
   */
  public AutoOnboardMockDataSource(DataSourceMetaBean meta,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager) {
    super(meta);
    this.metricDAO = metricConfigManager;
    this.datasetDAO = datasetConfigManager;
    this.dataSourceName = MapUtils.getString(meta.getProperties(), "name",
        MockThirdEyeDataSource.class.getSimpleName());
  }

  @Override
  public void run() {
    DataSourceMetaBean meta = this.getMeta();

    List<DatasetConfigDTO> datasetConfigs = new ArrayList<>();
    List<MetricConfigDTO> metricConfigs = new ArrayList<>();

    Map<String, Object> datasets = (Map<String, Object>) meta.getProperties().get("datasets");
    List<String> sortedDatasets = new ArrayList<>(datasets.keySet());
    Collections.sort(sortedDatasets);

    for (String datasetName : sortedDatasets) {
      Map<String, Object> dataset = (Map<String, Object>) datasets.get(datasetName);

      List<String> sortedDimensions = new ArrayList<>(
          (Collection<String>) dataset.get("dimensions"));
      Collections.sort(sortedDimensions);

      Period granularity = ConfigUtils
          .parsePeriod(MapUtils.getString(dataset, "granularity", "1hour"));

      DatasetConfigDTO datasetConfig = new DatasetConfigDTO();
      datasetConfig.setDataset(datasetName);
      datasetConfig.setDataSource(this.dataSourceName);
      datasetConfig.setDimensions(sortedDimensions);
      datasetConfig.setTimezone(MapUtils.getString(dataset, "timezone", "America/Los_Angeles"));
      datasetConfig.setTimeDuration(DataSourceUtils.getTimeDuration(granularity));
      datasetConfig.setTimeUnit(DataSourceUtils.getTimeUnit(granularity));

      datasetConfigs.add(datasetConfig);

      List<String> sortedMetrics = new ArrayList<>(
          ((Map<String, Object>) dataset.get("metrics")).keySet());
      Collections.sort(sortedMetrics);

      for (String metricName : sortedMetrics) {
        MetricConfigDTO metricConfig = new MetricConfigDTO();
        metricConfig.setName(metricName);
        metricConfig.setDataset(datasetName);
        metricConfig.setAlias(String.format("%s::%s", datasetName, metricName));

        metricConfigs.add(metricConfig);
      }
    }

    LOG.info("Read {} datasets and {} metrics", datasetConfigs.size(), metricConfigs.size());

    // NOTE: save in order as mock datasource expects metric ids first
    for (MetricConfigDTO metricConfig : metricConfigs) {
      if (this.metricDAO.findByMetricAndDataset(metricConfig.getName(), metricConfig.getDataset())
          == null) {
        Long id = this.metricDAO.save(metricConfig);
        if (id != null) {
          LOG.info("Created metric '{}' with id {}", metricConfig.getAlias(), id);
        } else {
          LOG.warn("Could not create metric '{}'", metricConfig.getAlias());
        }
      }
    }

    for (DatasetConfigDTO datasetConfig : datasetConfigs) {
      if (this.datasetDAO.findByDataset(datasetConfig.getDataset()) == null) {
        Long id = this.datasetDAO.save(datasetConfig);
        if (id != null) {
          LOG.info("Created dataset '{}' with id {}", datasetConfig.getDataset(), id);
        } else {
          LOG.warn("Could not create dataset '{}'", datasetConfig.getDataset());
        }
      }
    }
  }

  @Override
  public void runAdhoc() {
    this.run();
  }
}
