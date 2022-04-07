/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AlertTemplateManagerImpl extends AbstractManagerImpl<AlertTemplateDTO>
    implements AlertTemplateManager {

  @Inject
  public AlertTemplateManagerImpl(GenericPojoDao genericPojoDao) {
    super(AlertTemplateDTO.class, genericPojoDao);
  }
}
