/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.detectionpipeline.persistence;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedDatasetConfigManager extends DelegateDatasetConfigManager {

  /**
   * Simple cache that does not have an expiry mechanism. The cache is renewed on every
   * restart of the detection pipeline. This is to ensure that the cache is always up to date
   * and is consistent during the execution of the pipeline. Meaning, if a dataset is modified
   * midway during execution, the pipeline will still run with the old dataset config.
   */
  private final Map<CacheKey, DatasetConfigDTO> datasetNameCache;

  public CachedDatasetConfigManager(final DatasetConfigManager delegate) {
    super(delegate);
    datasetNameCache = new ConcurrentHashMap<>();
  }

  @Override
  public DatasetConfigDTO findByDatasetAndNamespace(final String name, final String namespace) {
    return 
        datasetNameCache.computeIfAbsent(new CacheKey(name, namespace), (k) -> 
        CachedDatasetConfigManager.super.findByDatasetAndNamespace(k.name(), k.namespace()));
  }
  
  private record CacheKey(String name, String namespace){}
}
