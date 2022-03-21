/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DataSourceManagerImpl extends AbstractManagerImpl<DataSourceDTO>
    implements DataSourceManager {

  @Inject
  public DataSourceManagerImpl(GenericPojoDao genericPojoDao) {
    super(DataSourceDTO.class, genericPojoDao);
  }
}
