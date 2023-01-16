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
package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
@Named("IdMapper")
public interface IdMapper {

  IdMapper INSTANCE = Mappers.getMapper(IdMapper.class);

  private static <ApiT extends ThirdEyeCrudApi<ApiT>, DtoT extends AbstractDTO> DtoT populateId(
      final ThirdEyeCrudApi<ApiT> api,
      final DtoT dto) {
    dto.setId(api.getId());
    return dto;
  }

  default AlertDTO map(AlertApi api) {
    return populateId(api, new AlertDTO());
  }

  default AlertApi map(AlertDTO o) {
    return new AlertApi().setId(o.getId());
  }

  default EnumerationItemDTO map(EnumerationItemApi api) {
    return populateId(api, new EnumerationItemDTO());
  }

  default EnumerationItemApi map(EnumerationItemDTO o) {
    return new EnumerationItemApi().setId(o.getId());
  }
}
