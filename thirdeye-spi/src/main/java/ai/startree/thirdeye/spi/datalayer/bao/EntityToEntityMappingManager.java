/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import java.util.List;
import java.util.Set;

public interface EntityToEntityMappingManager extends AbstractManager<EntityToEntityMappingDTO> {

  List<EntityToEntityMappingDTO> findByFromURN(String fromURN);

  List<EntityToEntityMappingDTO> findByFromURNs(Set<String> fromURN);

  List<EntityToEntityMappingDTO> findByToURN(String toURN);

  List<EntityToEntityMappingDTO> findByToURNs(Set<String> toURN);

  EntityToEntityMappingDTO findByFromAndToURN(String fromURN, String toURN);

  List<EntityToEntityMappingDTO> findByMappingType(String mappingType);

  List<EntityToEntityMappingDTO> findByFromURNAndMappingType(String fromURN, String mappingType);

  List<EntityToEntityMappingDTO> findByToURNAndMappingType(String toURN, String mappingType);
}
