/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.datasource;

import static ai.startree.thirdeye.spi.ThirdEyeException.checkThirdEye;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note:
 * DataSourceOnboarder is not responsible for performing authz
 * DataSourceOnboarder assumes the authz of the datasourceDto is already performed by consumers
 * of this class if necessary.
 * Passed datasourceDto should have an id.
 */
@Singleton
public class DataSourceOnboarder {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceOnboarder.class);

  private final DataSourceCache dataSourceCache;
  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;

  @Inject
  public DataSourceOnboarder(final DataSourceCache dataSourceCache,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    this.dataSourceCache = dataSourceCache;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
  }

  public List<DatasetConfigDTO> onboardAll(final DataSourceDTO dataSourceDto) {
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceDto);
    checkThirdEye(dataSource != null, ThirdEyeStatus.ERR_DATASOURCE_NOT_LOADED, dataSourceDto.getName());

    // TODO CYRIL authz perf - findAll by namespace directly instead of filtering in app
    final Set<String> alreadyOnboardedDatasets = datasetConfigManager.findAll()
        .stream()
        .filter(dataset -> Objects.equals(dataSourceDto.getName(), dataset.getDataSource())
            && Objects.equals(dataSourceDto.namespace(), dataset.namespace()))
        .map(DatasetConfigDTO::getDataset)
        .collect(Collectors.toSet());

    final List<DatasetConfigDTO> datasetsToOnboard = dataSource.getDatasets()
        .stream()
        .filter(d -> !alreadyOnboardedDatasets.contains(d.getDataset()))
        .toList();

    final List<DatasetConfigDTO> addedDatasets = datasetsToOnboard.stream()
        .map(datasetConfigDTO -> persist(datasetConfigDTO, dataSourceDto.getAuth()))
        .collect(Collectors.toList());

    LOG.info("Onboarded {} datasets: {} from datasource {}", addedDatasets.size(), addedDatasets,
        dataSourceDto.getName());
    return addedDatasets;
  }

  public DatasetConfigDTO onboardDataset(final DataSourceDTO dataSourceDto,
      final String datasetName) {
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceDto);
    checkThirdEye(dataSource != null, ThirdEyeStatus.ERR_DATASOURCE_NOT_LOADED, dataSourceDto.getName());
    final DatasetConfigDTO newDataset = dataSource.getDataset(datasetName);
    checkThirdEye(newDataset != null, ThirdEyeStatus.ERR_DATASET_NOT_FOUND, datasetName);
    final DatasetConfigDTO datasetConfigDTO = persist(newDataset, dataSourceDto.getAuth());
    checkThirdEye(datasetConfigDTO != null, ThirdEyeStatus.ERR_DATASET_NOT_FOUND, datasetName);

    return datasetConfigDTO;
  }

  public List<DatasetConfigDTO> offboardAll(final DataSourceDTO dataSourceDto) {
    // TODO CYRIL authz perf - findAll by namespace directly instead of filtering in app
    final List<DatasetConfigDTO> datasets = datasetConfigManager.findAll()
        .stream()
        .filter(dto -> dto.getDataSource().equals(dataSourceDto.getName()))
        .filter(dto -> Objects.equals(dto.namespace(), dataSourceDto.namespace()))
        .collect(Collectors.toList());

    final Set<String> filtered = datasets.stream()
        .map(DatasetConfigDTO::getDataset)
        .collect(Collectors.toSet());

    // TODO CYRIL authz perf - findAll by namespace directly instead of filtering in app
    metricConfigManager.findAll()
        .stream()
        .filter(dto -> filtered.contains(dto.getDataset()))
        .filter(dto -> Objects.equals(dto.namespace(), dataSourceDto.namespace()))
        .forEach(metricConfigManager::delete);

    datasets.forEach(datasetConfigManager::delete);
    return datasets;
  }

  /**
   * the provided auth should come from the datasource. It is injected in all entities created by
   * this method.
   */
  private DatasetConfigDTO persist(final DatasetConfigDTO datasetConfigDTO,
      final AuthorizationConfigurationDTO auth) {
    final List<MetricConfigDTO> metrics = datasetConfigDTO.getMetrics();
    datasetConfigDTO.setAuth(auth);
    datasetConfigDTO.setMetrics(null);
    final Long datasetId = datasetConfigManager.save(datasetConfigDTO);
    checkState(datasetId != null, "Failed creating dataset %s", datasetConfigDTO.getDataset());

    // onboard metrics with the same namespace as the dataset
    final DatasetConfigDTO datasetConfigMinimalInfo = new DatasetConfigDTO();
    datasetConfigMinimalInfo.setId(datasetId);
    datasetConfigMinimalInfo.setDataset(datasetConfigDTO.getDataset());
    metrics.stream()
        .peek(m -> m.setAuth(auth))
        .peek(m -> m.setDatasetConfig(datasetConfigMinimalInfo))
        .forEach(metricConfigManager::save);
    return datasetConfigDTO;
  }
}
