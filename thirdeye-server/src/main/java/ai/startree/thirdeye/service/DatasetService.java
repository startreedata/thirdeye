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

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.google.common.collect.ImmutableMap;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DatasetService extends CrudService<DatasetApi, DatasetConfigDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("name", "dataset")
      .build();

  @Inject
  public DatasetService(final DatasetConfigManager datasetConfigManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, datasetConfigManager, API_TO_INDEX_FILTER_MAP);
  }

  @Override
  protected DatasetConfigDTO createDto(final ThirdEyeServerPrincipal principal,
      final DatasetApi api) {
    return toDto(api);
  }

  @Override
  protected DatasetConfigDTO toDto(final DatasetApi api) {
    return ApiBeanMapper.toDatasetConfigDto(api);
  }

  @Override
  protected DatasetApi toApi(final DatasetConfigDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
