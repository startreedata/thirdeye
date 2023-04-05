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

package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATASET_NOT_FOUND;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.MetricApi;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.collect.ImmutableMap;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MetricService extends CrudService<MetricApi, MetricConfigDTO> {

  private final DatasetConfigManager datasetConfigManager;
  private final MetricConfigManager metricConfigManager;

  @Inject
  public MetricService(final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, metricConfigManager, ImmutableMap.of());
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;
  }

  @Override
  protected MetricConfigDTO createDto(final ThirdEyePrincipal principal, final MetricApi api) {
    final MetricConfigDTO dto = toDto(api);
    dto.setCreatedBy(principal.getName());

    return dto;
  }

  @Override
  protected void validate(final MetricApi api, final MetricConfigDTO existing) {
    super.validate(api, existing);

    ensureExists(api.getDataset(), "dataset");
    ensureExists(datasetConfigManager.findByDataset(api.getDataset().getName()),
        ERR_DATASET_NOT_FOUND, api.getDataset().getName());

    // For new Metric or existing metric with different name
    if (existing == null || !existing.getName().equals(api.getName())) {
      ensure(dtoManager.findByName(api.getName()).size() == 0, ERR_DUPLICATE_NAME, api.getName());
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

