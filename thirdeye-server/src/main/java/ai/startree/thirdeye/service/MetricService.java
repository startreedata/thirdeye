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
package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.ResourceUtils.ensure;
import static ai.startree.thirdeye.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATASET_NOT_FOUND;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_ENTITY;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.collect.ImmutableMap;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricService extends CrudService<MetricApi, MetricConfigDTO> {

  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricDao;

  @Inject
  public MetricService(final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, metricConfigManager, ImmutableMap.of());
    this.datasetConfigManager = datasetConfigManager;
    this.metricDao = metricConfigManager;
  }

  @Override
  protected void validate(final ThirdEyePrincipal principal, final MetricApi api, final MetricConfigDTO existing) {
    super.validate(principal, api, existing);

    ensureExists(api.getDataset(), "dataset");
    final DatasetConfigDTO datasetDto = datasetConfigManager.findUniqueByNameAndNamespace(api.getDataset().getName(),
        optional(api.getAuth()).map(AuthorizationConfigurationApi::getNamespace)
            .orElse(authorizationManager.currentNamespace(principal))
    );
    ensure(datasetDto != null, ERR_DATASET_NOT_FOUND, api.getDataset().getName());

    // For new Metric or existing metric with different name
    if (existing == null || !existing.getName().equals(api.getName())) {
      final MetricConfigDTO metricInDbWithSameName = metricDao.findBy(api.getName(), api.getDataset().getName(), datasetDto.namespace());
      ensure(metricInDbWithSameName == null,
          ERR_DUPLICATE_ENTITY,
          String.format("Metric with name: %s and dataset: %s already exists.",
              api.getName(),
              api.getDataset().getName()));
    }
  }

  @Override
  protected MetricConfigDTO toDto(final MetricApi api) {
    return ApiBeanMapper.toMetricConfigDto(api);
  }

  @Override
  protected MetricApi toApi(final MetricConfigDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}

