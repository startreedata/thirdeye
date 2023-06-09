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
package ai.startree.thirdeye.datalayer.mapper;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.datalayer.entity.EnumerationItemIndex;
import ai.startree.thirdeye.datalayer.entity.MergedAnomalyResultIndex;
import ai.startree.thirdeye.datalayer.entity.RcaInvestigationIndex;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import java.sql.Timestamp;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface IndexMapper {

  IndexMapper INSTANCE = Mappers.getMapper(IndexMapper.class);

  @Mapping(source = "alert.id", target = "alertId")
  EnumerationItemIndex toIndexEntity(EnumerationItemDTO dto);

  @Mapping(source = "enumerationItem.id", target = "enumerationItemId")
  @Mapping(target = "dimensions", ignore = true)
  @Mapping(source = "anomalyLabels", target = "ignored", qualifiedByName = "labelsToIgnoredMapper")
  MergedAnomalyResultIndex toIndexEntity(AnomalyDTO dto);
  @Named("labelsToIgnoredMapper")
  static boolean labelsToIgnored(List<AnomalyLabelDTO> labels) {
    return labels != null && labels.stream().anyMatch(AnomalyLabelDTO::isIgnore);
  }

  @Mapping(source = "anomaly.id", target = "anomalyId")
  @Mapping(source = "anomaly.startTime", target = "anomalyRangeStart")
  @Mapping(source = "anomaly.endTime", target = "anomalyRangeEnd")
  @Mapping(source = "createdBy", target = "owner")
  @Mapping(source = "createTime", target = "created", qualifiedByName = "timeMapper")
  @Mapping(source = "updateTime", target = "updated", qualifiedByName = "timeMapper")
  RcaInvestigationIndex toIndexEntity(RcaInvestigationDTO dto);

  @Named("timeMapper")
  static Long timeMapper(final Timestamp ts) {
    return optional(ts).map(Timestamp::getTime).orElse(null);
  }
}
