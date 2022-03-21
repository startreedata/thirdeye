/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.cache;

import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.cache.CacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricConfigCacheLoader extends CacheLoader<MetricDataset, MetricConfigDTO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricConfigCacheLoader.class);
  private final MetricConfigManager metricConfigDAO;

  public MetricConfigCacheLoader(MetricConfigManager metricConfigDAO) {
    this.metricConfigDAO = metricConfigDAO;
  }

  @Override
  public MetricConfigDTO load(MetricDataset metricDataset) {
    LOGGER.debug("Loading MetricConfigCache for metric {} of {}", metricDataset.getMetricName(),
        metricDataset.getDataset());
    MetricConfigDTO metricConfig = metricConfigDAO
        .findByMetricAndDataset(metricDataset.getMetricName(),
            metricDataset.getDataset());
    return metricConfig;
  }
}
