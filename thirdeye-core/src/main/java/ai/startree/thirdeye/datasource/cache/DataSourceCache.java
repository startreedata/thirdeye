/*
 * Copyright 2022 StarTree Inc
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

/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.cache;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSourceCache {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceCache.class);

  private final DataSourceManager dataSourceManager;
  private final DataSourcesLoader dataSourcesLoader;

  private final Map<String, DataSourceWrapper> cache = new HashMap<>();

  @Inject
  public DataSourceCache(
      final DataSourceManager dataSourceManager,
      final DataSourcesLoader dataSourcesLoader,
      final MetricRegistry metricRegistry) {
    this.dataSourceManager = dataSourceManager;
    this.dataSourcesLoader = dataSourcesLoader;
  }

  public synchronized ThirdEyeDataSource getDataSource(final String name) {
    final Optional<DataSourceDTO> dataSource = findByName(name);

    // datasource absent in DB
    if (dataSource.isEmpty()) {
      // remove redundant cache if datasource was recently deleted
      removeDataSource(name);
      throw new ThirdEyeException(ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, name);
    }
    final DataSourceWrapper cachedEntry = cache.get(name);
    if (cachedEntry != null) {
      if (cachedEntry.getUpdateTime().equals(dataSource.get().getUpdateTime())) {
        // cache hit
        return cachedEntry.getDataSource();
      }
    }
    // cache miss
    return loadDataSource(dataSource.get());
  }

  private Optional<DataSourceDTO> findByName(final String name) {
    final List<DataSourceDTO> results =
        dataSourceManager.findByPredicate(Predicate.EQ("name", name));
    checkState(results.size() <= 1, "Multiple data sources found with name: " + name);

    return results.stream().findFirst();
  }

  public void removeDataSource(final String name) {
    optional(cache.remove(name)).ifPresent(entry -> {
      try {
        entry.getDataSource().close();
      } catch (Exception e) {
        LOG.error("Datasource {} was not flushed gracefully.", entry.getDataSource().getName());
      }
    });
  }

  private ThirdEyeDataSource loadDataSource(final DataSourceDTO dataSource) {
    requireNonNull(dataSource);
    final String dsName = dataSource.getName();
    final ThirdEyeDataSource thirdEyeDataSource = dataSourcesLoader.loadDataSource(dataSource);
    requireNonNull(thirdEyeDataSource, "Failed to construct a data source object! " + dsName);
    // remove outdated cached datasource
    removeDataSource(dsName);
    cache.put(dsName, new DataSourceWrapper(thirdEyeDataSource, dataSource.getUpdateTime()));
    return thirdEyeDataSource;
  }

  public void clear() throws Exception {
    for (final DataSourceWrapper dataSourceWrapper : cache.values()) {
      dataSourceWrapper.getDataSource().close();
    }
    cache.clear();
  }
}
