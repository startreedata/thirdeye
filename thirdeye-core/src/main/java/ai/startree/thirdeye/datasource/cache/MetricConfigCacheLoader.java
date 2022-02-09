/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
