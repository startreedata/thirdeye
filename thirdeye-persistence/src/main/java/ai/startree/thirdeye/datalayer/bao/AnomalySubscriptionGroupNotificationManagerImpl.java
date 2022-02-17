/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import com.google.inject.Inject;

public class AnomalySubscriptionGroupNotificationManagerImpl
    extends AbstractManagerImpl<AnomalySubscriptionGroupNotificationDTO>
    implements AnomalySubscriptionGroupNotificationManager {

  @Inject
  public AnomalySubscriptionGroupNotificationManagerImpl(GenericPojoDao genericPojoDao) {
    super(AnomalySubscriptionGroupNotificationDTO.class,
        genericPojoDao);
  }
}
