/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SubscriptionGroupManagerImpl extends
    AbstractManagerImpl<SubscriptionGroupDTO> implements SubscriptionGroupManager {

  @Inject
  public SubscriptionGroupManagerImpl(GenericPojoDao genericPojoDao) {
    super(SubscriptionGroupDTO.class, genericPojoDao);
  }
}
