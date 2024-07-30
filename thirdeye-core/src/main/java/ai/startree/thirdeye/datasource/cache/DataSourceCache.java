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
/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.cache;

import static ai.startree.thirdeye.spi.Constants.METRICS_CACHE_TIMEOUT;
import static ai.startree.thirdeye.spi.util.ExecutorUtils.threadsNamed;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.Collections.emptyList;

import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note:
 * About authz: DataSourceCache is used in multiple places and defined in core.
 * So we assume the authz of the datasourceDto is already performed by consumers of this class
 * Passed datasourceDto should have an id.
 */
@Singleton
public class DataSourceCache {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceCache.class);

  private final DataSourceManager dataSourceManager;
  private final DataSourcesLoader dataSourcesLoader;

  // fixme cyril - use a guava evicting cache based on time/usage
  private final Map<Long, CachedDataSourceEntry> cache = new HashMap<>();

  private final ExecutorService executorService = new ThreadPoolExecutor(0, 10,
      60L,
      TimeUnit.SECONDS,
      new SynchronousQueue<>(),
      threadsNamed("DataSourceCache-%d"));

  @Inject
  public DataSourceCache(
      final DataSourceManager dataSourceManager,
      final DataSourcesLoader dataSourcesLoader) {
    this.dataSourceManager = dataSourceManager;
    this.dataSourcesLoader = dataSourcesLoader;

    Gauge.builder("thirdeye_healthy_datasources",
            memoizeWithExpiration(this::getHealthyDatasourceCount, METRICS_CACHE_TIMEOUT.toMinutes(),
                TimeUnit.MINUTES))
        .register(Metrics.globalRegistry);
    Metrics.gaugeMapSize("thirdeye_cached_datasources", emptyList(), cache);
  }

  // TODO CYRIL authz refacto - move this DataSourceCache should not have access to DataSourceManager - update architectureTest
  private Integer getHealthyDatasourceCount() {
    return Math.toIntExact(dataSourceManager.findAll().stream()
        .map(this::getDataSource)
        .filter(this::validateWithTimeout)
        .count());
  }

  private boolean validateWithTimeout(final ThirdEyeDataSource ds) {
    final Future<Boolean> future = executorService.submit(ds::validate);
    try {
      return future.get(2, TimeUnit.SECONDS);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    } catch (final TimeoutException e) {
      // Cancel the task as it has timed out
      future.cancel(true);
      return false;
    } catch (final Exception e) {
      return false;
    }
  }

  public synchronized ThirdEyeDataSource getDataSource(final @NonNull DataSourceDTO dataSourceDto) {
    final CachedDataSourceEntry cachedEntry = cache.get(Objects.requireNonNull(dataSourceDto.getId()));
    if (cachedEntry != null) {
      final Timestamp updateTimeCached = cachedEntry.timestamp();
      if (updateTimeCached.equals(dataSourceDto.getUpdateTime())) {
        return cachedEntry.dataSource(); // cache hit
      }
    }

    // cache miss
    return loadDataSource(dataSourceDto);
  }

  private ThirdEyeDataSource loadDataSource(final @NonNull DataSourceDTO dataSourceDto) {
    final ThirdEyeDataSource dataSource = dataSourcesLoader.loadDataSource(dataSourceDto);
    checkState(dataSource != null,
        "Failed to construct a data source object for datasource %s", dataSourceDto);
    final MeteredDataSource meteredDataSource = new MeteredDataSource(dataSource);

    // remove outdated cached datasource
    removeDataSource(dataSourceDto);
    cache.put(Objects.requireNonNull(dataSourceDto.getId()),
        new CachedDataSourceEntry(meteredDataSource, dataSourceDto.getUpdateTime()));
    return meteredDataSource;
  }

  public void removeDataSource(final DataSourceDTO dataSourceDTO) {
    optional(cache.remove(Objects.requireNonNull(dataSourceDTO.getId())))
        .map(CachedDataSourceEntry::dataSource)
        .ifPresent(this::close);
  }

  public void clear() {
    // TODO CYRIL authz validate design - for the moment clear is performed across all namespaces
    cache.values().stream()
        .map(CachedDataSourceEntry::dataSource)
        .forEach(this::close);
    cache.clear();
  }

  private void close(final ThirdEyeDataSource dataSource) {
    try {
      dataSource.close();
    } catch (final Exception e) {
      LOG.error("Datasource {} was not flushed gracefully.", dataSource.getName());
    }
  }

  private record CachedDataSourceEntry(MeteredDataSource dataSource, Timestamp timestamp) {}
}
