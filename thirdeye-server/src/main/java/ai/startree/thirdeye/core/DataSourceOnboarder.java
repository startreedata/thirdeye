/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.core;

import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSourceOnboarder {

  private static final Logger log = LoggerFactory.getLogger(DataSourceOnboarder.class);

  private final DataSourceCache dataSourceCache;
  private final DataSourceManager dataSourceManager;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;

  @Inject
  public DataSourceOnboarder(final DataSourceCache dataSourceCache,
      final DataSourceManager dataSourceManager,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    this.dataSourceCache = dataSourceCache;
    this.dataSourceManager = dataSourceManager;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
  }

  public List<DatasetConfigDTO> onboardAll(final String name) {
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(name);
    ensureExists(dataSource, ThirdEyeStatus.ERR_DATASOURCE_NOT_LOADED, name);

    final List<DatasetConfigDTO> existingDatasets = datasetConfigManager.findAll()
        .stream()
        .filter(dataset -> name.equals(dataset.getDataSource()))
        .collect(Collectors.toList());

    final List<DatasetConfigDTO> allDatasets = dataSource.getDatasets();

    final Set<String> existingDatasetNames = existingDatasets
        .stream()
        .map(DatasetConfigDTO::getDataset)
        .collect(Collectors.toSet());

    final List<DatasetConfigDTO> datasetsToBeAdded = allDatasets.stream()
        .filter(ds -> !existingDatasetNames.contains(ds.getDataset()))
        .collect(Collectors.toList());

    final List<DatasetConfigDTO> addedDatasets = datasetsToBeAdded.stream()
        .map(this::persist)
        .collect(Collectors.toList());

    log.info(String.format("Onboarded %d datasets from %s", addedDatasets.size(), name));
    return addedDatasets;
  }

  public DatasetConfigDTO onboardDataset(final String dataSourceName, final String datasetName) {
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceName);
    ensureExists(dataSource, ThirdEyeStatus.ERR_DATASOURCE_NOT_LOADED, dataSourceName);

    final DatasetConfigDTO newDataset = dataSource.getDataset(datasetName);
    ensureExists(newDataset, ThirdEyeStatus.ERR_DATASET_NOT_FOUND, datasetName);

    final DatasetConfigDTO datasetConfigDTO = persist(newDataset);
    ensureExists(datasetConfigDTO, ThirdEyeStatus.ERR_DATASET_NOT_FOUND, datasetName);

    return datasetConfigDTO;
  }

  private DatasetConfigDTO persist(final DatasetConfigDTO datasetConfigDTO) {
    final List<MetricConfigDTO> metrics = datasetConfigDTO.getMetrics();
    datasetConfigDTO.setMetrics(null);
    datasetConfigManager.save(datasetConfigDTO);

    metrics.forEach(metricConfigManager::save);
    return datasetConfigDTO;
  }

  public List<DatasetConfigDTO> offboardAll(final String name) {
    ensure(dataSourceManager.findByName(name).size() == 1, "exactly 1 data source expected.");

    final List<DatasetConfigDTO> datasets = datasetConfigManager.findAll()
        .stream()
        .filter(dto -> dto.getDataSource().equals(name))
        .collect(Collectors.toList());

    final Set<String> filtered = datasets.stream()
        .map(DatasetConfigDTO::getDataset)
        .collect(Collectors.toSet());

    metricConfigManager.findAll()
        .stream()
        .filter(dto -> filtered.contains(dto.getDataset()))
        .forEach(metricConfigManager::delete);

    datasets.forEach(datasetConfigManager::delete);
    return datasets;
  }
}
