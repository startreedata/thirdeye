/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.OverrideConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class OverrideConfigManagerImpl extends AbstractManagerImpl<OverrideConfigDTO> implements
    OverrideConfigManager {

  @Inject
  public OverrideConfigManagerImpl(GenericPojoDao genericPojoDao) {
    super(OverrideConfigDTO.class, genericPojoDao);
  }

  @Override
  public List<OverrideConfigDTO> findAllConflictByTargetType(String entityTypeName,
      long windowStart, long windowEnd) {
    Predicate predicate =
        Predicate.AND(Predicate.LE("startTime", windowEnd), Predicate.GE("endTime", windowStart),
            Predicate.EQ("targetEntity", entityTypeName));

    return findByPredicate(predicate);
  }
}
