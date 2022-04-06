
/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.cache;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.DataSourcesLoader;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSourceCache {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceCache.class);

  private final DataSourceManager dataSourceManager;
  private final DataSourcesLoader dataSourcesLoader;
  private final ExecutorService executorService;

  private final Map<String, DataSourceWrapper> cache = new HashMap<>();

  private final Counter datasourceExceptionCounter;
  private final Counter datasourceDurationCounter;
  private final Counter datasourceCallCounter;

  @Inject
  public DataSourceCache(
      final DataSourceManager dataSourceManager,
      final DataSourcesLoader dataSourcesLoader,
      final MetricRegistry metricRegistry) {
    this.dataSourceManager = dataSourceManager;
    this.dataSourcesLoader = dataSourcesLoader;
    executorService = Executors.newCachedThreadPool();

    datasourceExceptionCounter = metricRegistry.counter("datasourceExceptionCounter");
    datasourceDurationCounter = metricRegistry.counter("datasourceDurationCounter");
    datasourceCallCounter = metricRegistry.counter("datasourceCallCounter");
  }

  public synchronized ThirdEyeDataSource getDataSource(final String name) {
    final Optional<DataSourceDTO> dataSource = findByName(name);

    // datasource absent in DB
    if(dataSource.isEmpty()) {
      // remove redundant cache if datasource was recently deleted
      removeDataSource(name);
      throw new ThirdEyeException(ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, name);
    }
    final DataSourceWrapper cachedEntry = cache.get(name);
    if (cachedEntry != null) {
      if(cachedEntry.getUpdateTime().equals(dataSource.get().getUpdateTime())) {
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
    Optional.ofNullable(cache.remove(name)).ifPresent(entry -> {
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

  public ThirdEyeResponse getQueryResult(final ThirdEyeRequest request) throws Exception {
    datasourceCallCounter.inc();
    final long tStart = System.nanoTime();
    try {
      final String dataSource = request.getDataSource();

      return getDataSource(dataSource).execute(request);
    } catch (final Exception e) {
      datasourceExceptionCounter.inc();
      throw e;
    } finally {
      datasourceDurationCounter.inc(System.nanoTime() - tStart);
    }
  }

  public Future<ThirdEyeResponse> getQueryResultAsync(final ThirdEyeRequest request) {
    return executorService.submit(() -> getQueryResult(request));
  }

  public Map<ThirdEyeRequest, Future<ThirdEyeResponse>> getQueryResultsAsync(
      final List<ThirdEyeRequest> requests) {
    final Map<ThirdEyeRequest, Future<ThirdEyeResponse>> responseFuturesMap = new LinkedHashMap<>();
    for (final ThirdEyeRequest request : requests) {
      responseFuturesMap.put(request, getQueryResultAsync(request));
    }
    return responseFuturesMap;
  }

  public void clear() throws Exception {
    for (final DataSourceWrapper dataSourceWrapper : cache.values()) {
      dataSourceWrapper.getDataSource().close();
    }
    cache.clear();
  }
}
