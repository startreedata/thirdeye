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
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.datasource.DataSourceOnboarder;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
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
    /* new entity creation or name change in existing entity */
    if (existing == null || !existing.getName().equals(api.getName())) {
      final List<DataSourceDTO> sameName = dtoManager.findByName(api.getName());
      final List<DataSourceDTO> sameNameSameNamespace = authorizationManager.filterByNamespace(principal,
          optional(api.getAuth()).map(AuthorizationConfigurationApi::getNamespace).orElse(null), sameName);
      ensure(sameNameSameNamespace.isEmpty(), ERR_DUPLICATE_NAME, api.getName());
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
    dataSourceCache.removeDataSource(dto);
  }

  @Override
  protected void prepareUpdatedDto(
      final ThirdEyeServerPrincipal principal,
      final DataSourceDTO existing,
      final DataSourceDTO updated) {
    dataSourceCache.removeDataSource(existing);
  }

  @Deprecated // use getDatasets by id
  public List<DatasetApi> getDatasets(final ThirdEyePrincipal principal, final String name) {
    final DataSourceDTO dataSourceDto = getDatasourceByName(
        principal, name);
    authorizationManager.ensureCanRead(principal, dataSourceDto);
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceDto);
    return dataSource.getDatasets().stream()
        .filter(d -> authorizationManager.canRead(principal, d))
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public List<DatasetApi> getDatasets(final ThirdEyePrincipal principal, final long id) {
    final DataSourceDTO dataSourceDto = dtoManager.findById(id);
    checkArgument(dataSourceDto != null, "Could not find datasource with id %s", id);
    authorizationManager.ensureCanRead(principal, dataSourceDto);
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceDto);
    return dataSource.getDatasets().stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  @Deprecated // useOnboardDataset by datasourceId instead
  public DatasetApi onboardDataset(final ThirdEyePrincipal principal, final String dataSourceName, final String datasetName) {
    final DataSourceDTO dataSourceDto = getDatasourceByName(principal, dataSourceName);
    authorizationManager.ensureCanCreate(principal, dataSourceDto);
    final DatasetConfigDTO datasetConfigDTO = dataSourceOnboarder.onboardDataset(dataSourceDto,
        datasetName);

    return ApiBeanMapper.toApi(datasetConfigDTO);
  }

  public DatasetApi onboardDataset(final ThirdEyePrincipal principal, final long dataSourceId, final String datasetName) {
    final DataSourceDTO dataSourceDto = dtoManager.findById(dataSourceId);
    checkArgument(dataSourceDto != null, "Could not find datasource with id %s", dataSourceId);
    authorizationManager.ensureCanCreate(principal, dataSourceDto);
    final DatasetConfigDTO datasetConfigDTO = dataSourceOnboarder.onboardDataset(dataSourceDto,
        datasetName);

    return ApiBeanMapper.toApi(datasetConfigDTO);
  }

  @Deprecated // use onboardAll by Id instead
  public List<DatasetApi> onboardAll(final ThirdEyePrincipal principal, final String name) {
    final DataSourceDTO dataSourceDto = getDatasourceByName(principal, name);
    authorizationManager.ensureCanCreate(principal, dataSourceDto);
    final List<DatasetConfigDTO> datasets = dataSourceOnboarder.onboardAll(dataSourceDto);

    return datasets.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public List<DatasetApi> onboardAll(final ThirdEyePrincipal principal, final long id) {
    final DataSourceDTO dataSourceDto = dtoManager.findById(id);
    // TODO CYRIL authz - doing such check leaks ids of other namespaces because the error is not the same when an id exists in another namespace or not - also happens in other function - can be improved later - most DAO operation should be performed with a namespace or list of namespace filter 
    checkArgument(dataSourceDto != null, "Could not find datasource with id %s", id);  
    // TODO CYRIL authz review - is "create" a good access level here? - rationale: if a user can create a datasource, then the user can onboard datasets   
    authorizationManager.ensureCanCreate(principal, dataSourceDto);
    final List<DatasetConfigDTO> datasets = dataSourceOnboarder.onboardAll(dataSourceDto);
    return datasets.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  @Deprecated // use offboardAll by id instead
  public List<DatasetApi> offboardAll(final ThirdEyePrincipal principal, final String name) {
    final DataSourceDTO dataSourceDto = getDatasourceByName(principal, name);
    authorizationManager.ensureCanDelete(principal, dataSourceDto);
    final List<DatasetConfigDTO> datasets = dataSourceOnboarder.offboardAll(dataSourceDto);

    return datasets.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public List<DatasetApi> offboardAll(final ThirdEyePrincipal principal, final long id) {
    final DataSourceDTO dataSourceDto = dtoManager.findById(id);
    checkArgument(dataSourceDto != null, "Could not find datasource with id %s", id);
    authorizationManager.ensureCanDelete(principal, dataSourceDto);
    final List<DatasetConfigDTO> datasets = dataSourceOnboarder.offboardAll(dataSourceDto);

    return datasets.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public void clearDataSourceCache(final ThirdEyePrincipal principal) {
    authorizationManager.ensureHasRootAccess(principal);
    dataSourceCache.clear();
  }

  @Deprecated // use validate by id
  public boolean validate(final ThirdEyePrincipal principal, final String name) {
    final DataSourceDTO dataSourceDto = getDatasourceByName(principal, name);
    authorizationManager.ensureCanRead(principal, dataSourceDto);
    return dataSourceCache.getDataSource(dataSourceDto).validate();
  }

  public boolean validate(final ThirdEyePrincipal principal, final long id) {
    final DataSourceDTO dataSourceDto = dtoManager.findById(id);
    checkArgument(dataSourceDto != null, "Could not find datasource with id %s", id);
    authorizationManager.ensureCanRead(principal, dataSourceDto);
    return dataSourceCache.getDataSource(dataSourceDto).validate();
  }
  
  @Deprecated // in most cases and API we should get the datasource by id 
  private DataSourceDTO getDatasourceByName(final ThirdEyePrincipal principal, final String name) {
    final List<DataSourceDTO> sameName = dtoManager.findByName(name);
    final List<DataSourceDTO> sameNameSameNamespace = authorizationManager
        .filterByNamespace(principal, null, sameName);
    checkArgument(!sameNameSameNamespace.isEmpty(), "Could not find datasource with name: %s", name);
    checkState(sameNameSameNamespace.size() == 1, "Found multiple datasources with name: %s", name);
    return sameNameSameNamespace.get(0);
  }
}
