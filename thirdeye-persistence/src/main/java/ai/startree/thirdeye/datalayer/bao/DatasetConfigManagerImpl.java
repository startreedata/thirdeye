/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class DatasetConfigManagerImpl extends AbstractManagerImpl<DatasetConfigDTO>
    implements DatasetConfigManager {

  @Inject
  public DatasetConfigManagerImpl(GenericPojoDao genericPojoDao) {
    super(DatasetConfigDTO.class, genericPojoDao);
  }

  @Override
  public DatasetConfigDTO findByDataset(String dataset) {
    Predicate predicate = Predicate.EQ("dataset", dataset);
    List<DatasetConfigDTO> list = findByPredicate(predicate);
    if (list.size() == 1) {
      return list.iterator().next();
    }
    return null;
  }

  @Override
  public List<DatasetConfigDTO> findActive() {
    Predicate activePredicate = Predicate.EQ("active", true);
    return findByPredicate(activePredicate);
  }

  @Override
  public void updateLastRefreshTime(String dataset, long refreshTime, long eventTime) {
    DatasetConfigDTO datasetConfigDTO = findByDataset(dataset);
    datasetConfigDTO.setLastRefreshTime(refreshTime);
    datasetConfigDTO.setLastRefreshEventTime(eventTime);
    update(datasetConfigDTO);
  }
}
