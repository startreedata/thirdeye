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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.core.DataSourceOnboarder;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataSourceService extends CrudService<DataSourceApi, DataSourceDTO> {

  private final DataSourceCache dataSourceCache;
  private final DataSourceOnboarder dataSourceOnboarder;

  @Inject
  public DataSourceService(
      final DataSourceManager dataSourceManager,
      final DataSourceCache dataSourceCache,
      final DataSourceOnboarder dataSourceOnboarder,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, dataSourceManager, ImmutableMap.of());
    this.dataSourceCache = dataSourceCache;
    this.dataSourceOnboarder = dataSourceOnboarder;
  }

  @Override
  protected void validate(final ThirdEyePrincipal principal, final DataSourceApi api, @Nullable final DataSourceDTO existing) {
    super.validate(principal, api, existing);
    // FIXME cyril authz - name match should be namespace aware
    /* new entity creation or name change in existing entity */
    if (existing == null || !existing.getName().equals(api.getName())) {
      ensure(dtoManager.findByName(api.getName()).size() == 0, ERR_DUPLICATE_NAME, api.getName());
    }
  }

  @Override
  protected DataSourceDTO toDto(final DataSourceApi api) {
    return ApiBeanMapper.toDataSourceDto(api);
  }

  @Override
  protected DataSourceApi toApi(final DataSourceDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @Override
  protected void deleteDto(final DataSourceDTO dto) {
    super.deleteDto(dto);
    dataSourceCache.removeDataSource(dto.getName());
  }

  @Override
  protected void prepareUpdatedDto(
      final ThirdEyeServerPrincipal principal,
      final DataSourceDTO existing,
      final DataSourceDTO updated) {
    dataSourceCache.removeDataSource(existing.getName());
  }

  public List<DatasetApi> getDatasets(final ThirdEyePrincipal principal, final String name) {
    // fixme add datasource authz check - also need to have one datasourceCache per namespace or namespace aware datasource cache key to avoid name collisions
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(name);
    return dataSource.getDatasets().stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public DatasetApi onboardDataset(final ThirdEyePrincipal principal, final String dataSourceName, final String datasetName) {
    // fixme add authz check
    final DatasetConfigDTO datasetConfigDTO = dataSourceOnboarder.onboardDataset(dataSourceName,
        datasetName);

    return ApiBeanMapper.toApi(datasetConfigDTO);
  }

  public List<DatasetApi> onboardAll(final ThirdEyePrincipal principal, final String name) {
    // // FIXME cyril add principal check authz
    final List<DatasetConfigDTO> datasets = dataSourceOnboarder.onboardAll(name);

    return datasets.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public List<DatasetApi> offboardAll(final ThirdEyePrincipal principal, final String name) {
    // FIXME cyril add principal check authz
    final List<DatasetConfigDTO> datasets = dataSourceOnboarder.offboardAll(name);

    return datasets.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public void clearDataSourceCache(final ThirdEyePrincipal principal) {
    // // FIXME cyril  ensure root access - next have one cache per namespace? authz
    dataSourceCache.clear();
  }

  public boolean validate(final ThirdEyePrincipal principal, final String name) {
    // // FIXME cyril  ensure access  authz
    return dataSourceCache.getDataSource(name).validate();
  }
}
