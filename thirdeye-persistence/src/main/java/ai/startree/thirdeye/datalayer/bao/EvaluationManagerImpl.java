/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.EvaluationManager;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EvaluationManagerImpl extends AbstractManagerImpl<EvaluationDTO> implements
    EvaluationManager {

  @Inject
  public EvaluationManagerImpl(GenericPojoDao genericPojoDao) {
    super(EvaluationDTO.class, genericPojoDao);
  }
}
