/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.RootcauseTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.RootcauseTemplateDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class RootcauseTemplateManagerImpl extends
    AbstractManagerImpl<RootcauseTemplateDTO> implements
    RootcauseTemplateManager {

  @Inject
  public RootcauseTemplateManagerImpl(GenericPojoDao genericPojoDao) {
    super(RootcauseTemplateDTO.class, genericPojoDao);
  }

  @Override
  public List<RootcauseTemplateDTO> findByMetricId(long metricId) {
    Predicate predicate = Predicate.EQ("metricId", metricId);
    return findByPredicate(predicate);
  }

  @Override
  public Long saveOrUpdate(RootcauseTemplateDTO rootcauseTemplateDTO) {
    Predicate predicate = Predicate.EQ("name", rootcauseTemplateDTO.getName());
    List<RootcauseTemplateDTO> list = findByPredicate(predicate);
    if (!list.isEmpty()) {
      rootcauseTemplateDTO.setId(list.get(0).getId());
      super.update(rootcauseTemplateDTO);
      return rootcauseTemplateDTO.getId();
    } else {
      return super.save(rootcauseTemplateDTO);
    }
  }
}
