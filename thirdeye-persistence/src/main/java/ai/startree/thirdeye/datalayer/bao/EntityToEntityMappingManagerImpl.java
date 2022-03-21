/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

@Singleton
public class EntityToEntityMappingManagerImpl extends
    AbstractManagerImpl<EntityToEntityMappingDTO> implements EntityToEntityMappingManager {

  @Inject
  public EntityToEntityMappingManagerImpl(GenericPojoDao genericPojoDao) {
    super(EntityToEntityMappingDTO.class, genericPojoDao);
  }

  @Override
  public List<EntityToEntityMappingDTO> findByFromURN(String fromURN) {
    return findByPredicate(Predicate.EQ("fromURN", fromURN));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByFromURNs(Set<String> fromURNs) {
    return findByPredicate(Predicate.IN("fromURN", fromURNs.toArray()));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByToURN(String toURN) {
    return findByPredicate(Predicate.EQ("toURN", toURN));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByToURNs(Set<String> toURNs) {
    return findByPredicate(Predicate.IN("toURN", toURNs.toArray()));
  }

  @Override
  public EntityToEntityMappingDTO findByFromAndToURN(String fromURN, String toURN) {
    EntityToEntityMappingDTO dto = null;
    Predicate predicate = Predicate
        .AND(Predicate.EQ("fromURN", fromURN), Predicate.EQ("toURN", toURN));
    List<EntityToEntityMappingDTO> findByPredicate = findByPredicate(predicate);
    if (CollectionUtils.isNotEmpty(findByPredicate)) {
      dto = findByPredicate.get(0);
    }
    return dto;
  }

  @Override
  public List<EntityToEntityMappingDTO> findByMappingType(String mappingType) {
    return findByPredicate(Predicate.EQ("mappingType", mappingType));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByFromURNAndMappingType(String fromURN,
      String mappingType) {
    return findByPredicate(
        Predicate.AND(Predicate.EQ("fromURN", fromURN), Predicate.EQ("mappingType", mappingType)));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByToURNAndMappingType(String toURN,
      String mappingType) {
    return findByPredicate(
        Predicate.AND(Predicate.EQ("toURN", toURN), Predicate.EQ("mappingType", mappingType)));
  }
}
