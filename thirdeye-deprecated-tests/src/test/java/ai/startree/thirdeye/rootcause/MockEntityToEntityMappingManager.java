/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause;

import ai.startree.thirdeye.spi.datalayer.bao.EntityToEntityMappingManager;
import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MockEntityToEntityMappingManager extends
    AbstractMockManager<EntityToEntityMappingDTO> implements EntityToEntityMappingManager {

  private final Collection<EntityToEntityMappingDTO> entities;

  public MockEntityToEntityMappingManager(Collection<EntityToEntityMappingDTO> entities) {
    this.entities = entities;
  }

  @Override
  public List<EntityToEntityMappingDTO> findByName(String name) {
    return findByPredicate(ai.startree.thirdeye.spi.datalayer.Predicate.EQ("name", name));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByFromURN(final String fromURN) {
    return new ArrayList<>(
        Collections2.filter(this.entities, new Predicate<EntityToEntityMappingDTO>() {
          @Override
          public boolean apply(EntityToEntityMappingDTO dto) {
            return dto.getFromURN().equals(fromURN);
          }
        }));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByFromURNs(Set<String> fromURNs) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<EntityToEntityMappingDTO> findByToURN(final String toURN) {
    return new ArrayList<>(
        Collections2.filter(this.entities, new Predicate<EntityToEntityMappingDTO>() {
          @Override
          public boolean apply(EntityToEntityMappingDTO dto) {
            return dto.getToURN().equals(toURN);
          }
        }));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByToURNs(Set<String> toURNs) {
    throw new AssertionError("not implemented");
  }

  @Override
  public EntityToEntityMappingDTO findByFromAndToURN(String fromURN, String toURN) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<EntityToEntityMappingDTO> findByMappingType(final String mappingType) {
    return new ArrayList<>(
        Collections2.filter(this.entities, new Predicate<EntityToEntityMappingDTO>() {
          @Override
          public boolean apply(EntityToEntityMappingDTO dto) {
            return dto.getMappingType().equals(mappingType);
          }
        }));
  }

  @Override
  public List<EntityToEntityMappingDTO> findByFromURNAndMappingType(String fromURN,
      String mappingType) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<EntityToEntityMappingDTO> findByToURNAndMappingType(String toURN,
      String mappingType) {
    throw new AssertionError("not implemented");
  }

  @Override
  public int deleteRecordsOlderThanDays(int days) {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<EntityToEntityMappingDTO> findAll() {
    return new ArrayList<>(this.entities);
  }
}
