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
package ai.startree.thirdeye.datalayer.mapper;

import ai.startree.thirdeye.datalayer.entity.RcaInvestigationIndex;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import org.modelmapper.PropertyMap;

/**
 * Custom mapping from DTO to Index.
 * See http://modelmapper.org/javadoc/ --> PropertyMap
 */
public class RcaInvestigationIndexMapper extends
    PropertyMap<RcaInvestigationDTO, RcaInvestigationIndex> {

  @Override
  protected void configure() {
    // make sure to read the doc - below is not usual java - it's using an embedded DSL
    map().setAnomalyId(source.getAnomaly().getId());
    map().setAnomalyRangeStart(source.getAnomaly().getStartTime());
    map().setAnomalyRangeEnd(source.getAnomaly().getEndTime());
    map().setOwner(source.getCreatedBy());
    map().setCreated(source.getCreateTime().getTime());
    map().setUpdated(source.getUpdateTime().getTime());
  }
}
