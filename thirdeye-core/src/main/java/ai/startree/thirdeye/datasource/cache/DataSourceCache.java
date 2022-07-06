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
import ai.startree.thirdeye.spi.util.Pair;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSourceCache {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceCache.class);

  private final DataSourceManager dataSourceManager;
  private final DataSourcesLoader dataSourcesLoader;
  private final MetricRegistry metricRegistry;

  private final Map<String, Pair<DataSourceWrapper, Timestamp>> cache = new HashMap<>();

  @Inject
  public DataSourceCache(
      final DataSourceManager dataSourceManager,
      final DataSourcesLoader dataSourcesLoader,
      final MetricRegistry metricRegistry) {
    this.dataSourceManager = dataSourceManager;
    this.dataSourcesLoader = dataSourcesLoader;
    this.metricRegistry = metricRegistry;

    metricRegistry.register("healthyDatasourceCount", new CachedGauge<Integer>(1, TimeUnit.MINUTES) {
      @Override
      protected Integer loadValue() {
        return getHealthyDatasourceCount();
      }
    });
    metricRegistry.register("loadedDatasourceCount", (Gauge<Integer>) cache::size);
  }

  private Integer getHealthyDatasourceCount() {
    return Math.toIntExact(dataSourceManager.findAll().stream()
        .map(dto -> getDataSource(dto.getName()))
        .filter(ThirdEyeDataSource::validate)
        .count());
  }

  public synchronized ThirdEyeDataSource getDataSource(final String name) {
    final Optional<DataSourceDTO> dataSource = findByName(name);

    // datasource absent in DB
    if (dataSource.isEmpty()) {
      // remove redundant cache if datasource was recently deleted
      removeDataSource(name);
      throw new ThirdEyeException(ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, name);
    }
    final Pair<DataSourceWrapper, Timestamp> cachedEntry = cache.get(name);
    final DataSourceDTO dataSourceDTO = dataSource.get();
    if (cachedEntry != null) {
      final Timestamp updateTimeCached = cachedEntry.getSecond();
      if (updateTimeCached.equals(dataSourceDTO.getUpdateTime())) {
        return cachedEntry.getFirst(); // cache hit
      }
    }

    // cache miss
    return loadDataSource(dataSourceDTO);
  }

  private Optional<DataSourceDTO> findByName(final String name) {
    final List<DataSourceDTO> results =
        dataSourceManager.findByPredicate(Predicate.EQ("name", name));
    checkState(results.size() <= 1, "Multiple data sources found with name: " + name);

    return results.stream().findFirst();
  }

  private ThirdEyeDataSource loadDataSource(final DataSourceDTO dataSource) {
    requireNonNull(dataSource);
    final String dataSourceName = dataSource.getName();
    final DataSourceWrapper wrapped = wrap(
        requireNonNull(dataSourcesLoader.loadDataSource(dataSource),
            "Failed to construct a data source object! " + dataSourceName));

    // remove outdated cached datasource
    removeDataSource(dataSourceName);
    cache.put(dataSourceName, new Pair<>(wrapped, dataSource.getUpdateTime()));
    return wrapped;
  }

  private DataSourceWrapper wrap(final ThirdEyeDataSource thirdEyeDataSource) {
    return new DataSourceWrapper(thirdEyeDataSource, metricRegistry);
  }

  public void removeDataSource(final String name) {
    optional(cache.remove(name))
        .map(Pair::getFirst)
        .ifPresent(this::close);
  }

  public void clear() {
    cache.values().stream()
        .map(Pair::getFirst)
        .forEach(this::close);
    cache.clear();
  }

  private void close(final ThirdEyeDataSource dataSource) {
    try {
      dataSource.close();
    } catch (Exception e) {
      LOG.error("Datasource {} was not flushed gracefully.", dataSource.getName());
    }
  }
}
