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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OPERATION_UNSUPPORTED;
import static ai.startree.thirdeye.util.ResourceUtils.badRequest;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.datalayer.core.EnumerationItemDeleter;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.datalayer.bao.EnumerationItemManager;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EnumerationItemService extends CrudService<EnumerationItemApi, EnumerationItemDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("alert.id", "alertId")
      .build();

  private final EnumerationItemDeleter enumerationItemDeleter;

  @Inject
  public EnumerationItemService(final AuthorizationManager authorizationManager,
      final EnumerationItemManager enumerationItemManager,
      final EnumerationItemDeleter enumerationItemDeleter) {
    super(authorizationManager, enumerationItemManager, API_TO_INDEX_FILTER_MAP);
    this.enumerationItemDeleter = enumerationItemDeleter;
  }

  @Override
  protected EnumerationItemDTO createDto(final ThirdEyePrincipal principal,
      final EnumerationItemApi api) {
    final EnumerationItemDTO dto = ApiBeanMapper.toEnumerationItemDTO(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected EnumerationItemDTO toDto(final EnumerationItemApi api) {
    throw badRequest(ERR_OPERATION_UNSUPPORTED,
        "Enumeration Items are immutable. You can regenerate from the alert.");
  }

  @Override
  protected EnumerationItemApi toApi(final EnumerationItemDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @Override
  protected void deleteDto(final EnumerationItemDTO dto) {
    enumerationItemDeleter.delete(dto);
  }
}
