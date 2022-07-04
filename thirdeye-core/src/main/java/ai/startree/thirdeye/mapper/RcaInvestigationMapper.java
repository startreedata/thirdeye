/*
 * Copyright 2022 StarTree Inc
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

import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import ai.startree.thirdeye.spi.api.UserApi;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import java.sql.Timestamp;
import java.util.Date;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AnomalyMapper.class})
public interface RcaInvestigationMapper {

  RcaInvestigationMapper INSTANCE = Mappers.getMapper(RcaInvestigationMapper.class);

  @Mappings({
      @Mapping(target = "createTime", source = "created"),
      @Mapping(target = "updateTime", source = "updated"),
  })
  RcaInvestigationDTO toDto(RcaInvestigationApi api);

  @Mappings({
      @Mapping(target = "created", source = "createTime"),
      @Mapping(target = "updated", source = "updateTime"),
  })
  RcaInvestigationApi toApi(RcaInvestigationDTO dto);

  // mappers below are used by mapstruct -- see https://github.com/mapstruct/mapstruct/issues/1824
  // todo cyril share this with other mappers?
  @SuppressWarnings("unused")
  default Timestamp mapDateToTimestamp(Date date) {
    if (date != null) {
      return new Timestamp(date.getTime());
    }
    return null;
  }

  @SuppressWarnings("unused")
  default Date mapTimestampToDate(Timestamp timestamp) {
    if (timestamp != null) {
      return new Date(timestamp.getTime());
    }
    return null;
  }

  @SuppressWarnings("unused")
  default String mapUserApiToOwner(UserApi userApi) {
    if (userApi != null) {
      return userApi.getPrincipal();
    }
    return null;
  }

  @SuppressWarnings("unused")
  default UserApi mapOwnerToUserApi(String owner) {
    if (owner != null) {
      return new UserApi().setPrincipal(owner);
    }
    return null;
  }
}
