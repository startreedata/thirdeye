/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.cache;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetMaxDataTimeCacheLoader extends CacheLoader<String, Long> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetMaxDataTimeCacheLoader.class);

  private final DataSourceCache dataSourceCache;
  private final DatasetConfigManager datasetConfigDAO;

  private final ExecutorService reloadExecutor = Executors.newSingleThreadExecutor();

  public DatasetMaxDataTimeCacheLoader(DataSourceCache dataSourceCache,
      DatasetConfigManager datasetConfigDAO) {
    this.dataSourceCache = dataSourceCache;
    this.datasetConfigDAO = datasetConfigDAO;
  }

  /**
   * Fetches the max date time in millis for this dataset from the right data source
   * {@inheritDoc}
   *
   * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
   */
  @Override
  public Long load(String dataset) throws Exception {
    LOGGER.debug("Loading maxDataTime cache {}", dataset);
    long maxTime = 0;
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findByDataset(dataset);
    String dataSourceName = datasetConfig.getDataSource();
    try {
      ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceName);
      if (dataSource == null) {
        LOGGER.warn("dataSource [{}] found null in the query cache", dataSourceName);
      } else {
        maxTime = dataSource.getMaxDataTime(datasetConfig);
      }
    } catch (Exception e) {
      LOGGER.error("Exception in getting max date time for {} from data source {}", dataset,
          dataSourceName, e);
    }
    if (maxTime <= 0) {
      maxTime = System.currentTimeMillis();
    }
    return maxTime;
  }

  @Override
  public ListenableFuture<Long> reload(final String dataset, Long preMaxDataTime) {
    ListenableFutureTask<Long> reloadTask = ListenableFutureTask.create(new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        return DatasetMaxDataTimeCacheLoader.this.load(dataset);
      }
    });
    reloadExecutor.execute(reloadTask);
    LOGGER.info("Passively refreshing max data time of collection: {}", dataset);
    return reloadTask;
  }
}
