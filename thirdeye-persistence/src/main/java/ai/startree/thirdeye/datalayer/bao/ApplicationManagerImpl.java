/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.ApplicationManager;
import ai.startree.thirdeye.spi.datalayer.dto.ApplicationDTO;
import com.google.inject.Inject;
import java.util.List;

public class ApplicationManagerImpl extends AbstractManagerImpl<ApplicationDTO>
    implements ApplicationManager {

  @Inject
  public ApplicationManagerImpl(GenericPojoDao genericPojoDao) {
    super(ApplicationDTO.class, genericPojoDao);
  }

  public List<ApplicationDTO> findByName(String name) {
    Predicate predicate = Predicate.EQ("application", name);
    return genericPojoDao.get(predicate, ApplicationDTO.class);
  }
}
