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

import static ai.startree.thirdeye.ResourceUtils.badRequest;
import static ai.startree.thirdeye.ResourceUtils.ensure;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DUPLICATE_NAME;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.datasource.DataSourceOnboarder;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DataSourceService extends CrudService<DataSourceApi, DataSourceDTO> {

  private final DataSourceCache dataSourceCache;
  private final DataSourceOnboarder dataSourceOnboarder;
  private final DatasetConfigManager datasetConfigDAO;
  private final AuthorizationManager authorizationManager;

  @Inject
  public DataSourceService(
      final DataSourceManager dataSourceManager,
      final DataSourceCache dataSourceCache,
      final DataSourceOnboarder dataSourceOnboarder,
      final AuthorizationManager authorizationManager,
      final DatasetConfigManager datasetConfigDAO) {
    super(authorizationManager, dataSourceManager, ImmutableMap.of());
    this.dataSourceCache = dataSourceCache;
    this.dataSourceOnboarder = dataSourceOnboarder;
    this.datasetConfigDAO = datasetConfigDAO;
    this.authorizationManager = authorizationManager;
  }

  @Override
  protected void validate(final ThirdEyePrincipal principal, final DataSourceApi api, @Nullable final DataSourceDTO existing) {
    super.validate(principal, api, existing);
    /* new entity creation or name change in existing entity */
    if (existing == null) {
      final DataSourceDTO sameNameSameNamespace = dtoManager.findUniqueByNameAndNamespace(api.getName(),
          optional(api.getAuth()).map(AuthorizationConfigurationApi::getNamespace)
              .orElse(authorizationManager.currentNamespace(principal))
      );
      ensure(sameNameSameNamespace == null, ERR_DUPLICATE_NAME, api.getName());
    } else if (!existing.getName().equals(api.getName())) {
      // see TE-2431 - need datasets to point to datasource by id instead of name - in the meantime, we forbid datasource name change
      throw badRequest(
          ThirdEyeStatus.ERR_DATASOURCE_VALIDATION_FAILED,
          api.getName(),
          String.format(
              "Changing the name of a datasource is not allowed. Please delete the datasource %s and create a new one with the new name.",
              existing.getName()));
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
    // deleting a datasource has cascading effects down to alerts
    // we only delete datasets because datasets maintain a reference to a datasource and not deleting those dataset will prevent from creating new ones with the same name because of the db uniqueness constraint (name,namespace) 
    // todo have datasets point to datasource by id - see https://startree.atlassian.net/browse/TE-2432 and then rewrite this query exploiting an index on the datasource id
    // todo eventually we should relax the constraint to (name, datasourceId, namespace) but the use cases for this are limited and this would require some changes in the UI
    final List<Long> datasetIdsToDelete = datasetConfigDAO
        .findByPredicate(Predicate.EQ("namespace", dto.namespace()))
        .stream()
        .filter(e -> e.getDataSource().equals(dto.getName()))
        .map(AbstractDTO::getId)
        .toList()
    ;
    datasetConfigDAO.deleteByIds(datasetIdsToDelete);
    dataSourceCache.removeDataSource(dto);
  }

  @Override
  protected void prepareUpdatedDto(
      final ThirdEyeServerPrincipal principal,
      final DataSourceDTO existing,
      final DataSourceDTO updated) {
    dataSourceCache.removeDataSource(existing);
  }

  public List<DatasetApi> getDatasets(final ThirdEyePrincipal principal, final long id) {
    final DataSourceDTO dataSourceDto = dtoManager.findById(id);
    checkArgument(dataSourceDto != null, "Could not find datasource with id %s", id);
    authorizationManager.ensureNamespace(principal, dataSourceDto);
    authorizationManager.ensureCanRead(principal, dataSourceDto);
    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceDto);
    return dataSource.getDatasets().stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList());
  }

  public DatasetApi onboardDataset(final ThirdEyePrincipal principal, final long dataSourceId, final String datasetName) {
    final DataSourceDTO dataSourceDto = dtoManager.findById(dataSourceId);
    checkArgument(dataSourceDto != null, "Could not find datasource with id %s", dataSourceId);
    authorizationManager.ensureCanCreate(principal, dataSourceDto);
    final DatasetConfigDTO datasetConfigDTO = dataSourceOnboarder.onboardDataset(dataSourceDto,
        datasetName);

    return ApiBeanMapper.toApi(datasetConfigDTO);
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

  public boolean validate(final ThirdEyePrincipal principal, final long id) {
    final DataSourceDTO dataSourceDto = dtoManager.findById(id);
    checkArgument(dataSourceDto != null, "Could not find datasource with id %s", id);
    authorizationManager.ensureCanRead(principal, dataSourceDto);
    return dataSourceCache.getDataSource(dataSourceDto).validate();
  }

  public DataSourceApi recommend(final ThirdEyePrincipal principal) {
    final String namespace = authorizationManager.currentNamespace(principal);

    // verify that user has read access to data sources in given namespace
    final DataSourceDTO sampleDataset = new DataSourceDTO();
    sampleDataset.setAuth(new AuthorizationConfigurationDTO().setNamespace(namespace));
    authorizationManager.ensureCanRead(principal, sampleDataset);

    return authorizationManager.generateDatasourceConnection(principal);
  }
}
