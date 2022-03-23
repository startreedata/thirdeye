/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.cache;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheLoader;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DimensionFiltersCacheLoader extends CacheLoader<String, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DimensionFiltersCacheLoader.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final DataSourceCache dataSourceCache;
  private final DatasetConfigManager datasetConfigDAO;

  public DimensionFiltersCacheLoader(DataSourceCache dataSourceCache,
      DatasetConfigManager datasetConfigDAO) {
    this.dataSourceCache = dataSourceCache;
    this.datasetConfigDAO = datasetConfigDAO;
  }

  /**
   * Fetched dimension filters for this dataset from the right data source
   * {@inheritDoc}
   *
   * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
   */
  @Override
  public String load(String dataset) throws Exception {
    LOGGER.debug("Loading from dimension filters cache {}", dataset);
    String dimensionFiltersJson = null;
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findByDataset(dataset);
    String dataSourceName = datasetConfig.getDataSource();
    try {
      ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceName);
      if (dataSource == null) {
        LOGGER.warn("datasource [{}] found null in queryCache", dataSourceName);
      } else {
        Map<String, List<String>> dimensionFilters = dataSource.getDimensionFilters(
            datasetConfig);
        dimensionFiltersJson = OBJECT_MAPPER.writeValueAsString(dimensionFilters);
      }
    } catch (Exception e) {
      LOGGER.error("Exception in getting dimension filters for {} from data source {}", dataset,
          dataSourceName, e);
    }
    return dimensionFiltersJson;
  }
}

