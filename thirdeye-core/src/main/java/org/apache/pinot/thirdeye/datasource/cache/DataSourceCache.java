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

package org.apache.pinot.thirdeye.datasource.cache;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

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
import org.apache.pinot.thirdeye.datasource.DataSourcesLoader;
import org.apache.pinot.thirdeye.spi.ThirdEyeException;
import org.apache.pinot.thirdeye.spi.ThirdEyeStatus;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DataSourceManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceDTO;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataSourceCache {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceCache.class);

  private final DataSourceManager dataSourceManager;
  private final DataSourcesLoader dataSourcesLoader;
  private final ExecutorService executorService;

  private final Map<String, ThirdEyeDataSource> cache = new HashMap<>();

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
    final ThirdEyeDataSource cachedThirdEyeDataSource = cache.get(name);
    if (cachedThirdEyeDataSource != null) {
      return cachedThirdEyeDataSource;
    }

    final Optional<DataSourceDTO> dataSource = findByName(name);
    if (dataSource.isPresent()) {
      final ThirdEyeDataSource thirdEyeDataSource = dataSourcesLoader.loadDataSource(dataSource.get());

      requireNonNull(thirdEyeDataSource, "Failed to construct a data source object! " + name);
      cache.put(name, thirdEyeDataSource);
      return thirdEyeDataSource;
    }
    throw new ThirdEyeException(ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, name);
  }

  private Optional<DataSourceDTO> findByName(final String name) {
    final List<DataSourceDTO> results =
        dataSourceManager.findByPredicate(Predicate.EQ("name", name));
    checkState(results.size() <= 1, "Multiple data sources found with name: " + name);

    return results.stream().findFirst();
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
    for (final ThirdEyeDataSource thirdEyeDataSource : cache.values()) {
      thirdEyeDataSource.close();
    }
    cache.clear();
  }
}
