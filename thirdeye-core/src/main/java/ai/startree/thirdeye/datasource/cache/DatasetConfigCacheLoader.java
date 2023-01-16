/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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
