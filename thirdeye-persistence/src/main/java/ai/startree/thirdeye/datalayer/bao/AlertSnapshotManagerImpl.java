/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.AlertSnapshotManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertSnapshotDTO;
import com.google.inject.Inject;

public class AlertSnapshotManagerImpl extends AbstractManagerImpl<AlertSnapshotDTO>
    implements AlertSnapshotManager {

  @Inject
  public AlertSnapshotManagerImpl(GenericPojoDao genericPojoDao) {
    super(AlertSnapshotDTO.class, genericPojoDao);
  }
}
