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

import ai.startree.thirdeye.spi.api.EnumerationItemApi;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {IdMapper.class})
public interface EnumerationItemMapper {

  EnumerationItemMapper INSTANCE = Mappers.getMapper(EnumerationItemMapper.class);

  @Mapping(target = "alerts", qualifiedByName = "IdMapper")
  EnumerationItemDTO toDto(EnumerationItemApi api);

  @Mapping(target = "alerts", qualifiedByName = "IdMapper")
  EnumerationItemApi toApi(EnumerationItemDTO dto);
}
