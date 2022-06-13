/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.cache;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.google.common.cache.CacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetConfigCacheLoader extends CacheLoader<String, DatasetConfigDTO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetConfigCacheLoader.class);
  private final DatasetConfigManager datasetConfigDAO;

  public DatasetConfigCacheLoader(DatasetConfigManager datasetConfigDAO) {
    this.datasetConfigDAO = datasetConfigDAO;
  }

  @Override
  public DatasetConfigDTO load(String collection) throws Exception {
    LOGGER.debug("Loading DatasetConfigCache for {}", collection);
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findByDataset(collection);
    return datasetConfig;
  }
}
