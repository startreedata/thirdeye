
package org.apache.pinot.thirdeye.datasource.cache;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.util.ResourceUtils.badRequest;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.pinot.thirdeye.datasource.DataSourcesLoader;
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

  private final Map<DataSourceCacheKey, ThirdEyeDataSource> cache = new HashMap<>();

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
      throw badRequest(ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, name);
    }

    final ThirdEyeDataSource cachedThirdEyeDataSource = cache.get(new DataSourceCacheKey(name, dataSource.get().getUpdateTime()));
    if (cachedThirdEyeDataSource != null) {
      return cachedThirdEyeDataSource;
    }

    // remove outdated cached datasource if any
    removeDataSource(name);
    final ThirdEyeDataSource thirdEyeDataSource = dataSourcesLoader.loadDataSource(dataSource.get());
    requireNonNull(thirdEyeDataSource, "Failed to construct a data source object! " + name);
    cache.put(new DataSourceCacheKey(name, new Timestamp(new Date().getTime())), thirdEyeDataSource);
    return thirdEyeDataSource;
  }

  private Optional<DataSourceDTO> findByName(final String name) {
    final List<DataSourceDTO> results =
        dataSourceManager.findByPredicate(Predicate.EQ("name", name));
    checkState(results.size() <= 1, "Multiple data sources found with name: " + name);

    return results.stream().findFirst();
  }

  public void removeDataSource(final String name) {
    cache.remove(new DataSourceCacheKey(name, new Timestamp(0)));
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

  private class DataSourceCacheKey {
    private String name;
    private Timestamp loadTime;

    public DataSourceCacheKey(final String name, final Timestamp loadTime) {
      this.name = name;
      this.loadTime = loadTime;
    }

    public String getName() {
      return name;
    }

    public Timestamp getLoadTime() {
      return loadTime;
    }

    @Override
    public boolean equals(final Object o) {
      final DataSourceCacheKey that = (DataSourceCacheKey) o;
      // "this" is equal to "that" if "this" is loaded after "that" and have same names.
      // Usually here "this" is a cached datasource entry and "that" is a DB entry with updateTime as loadTime.
      // The implication being the datasource which is cached after the updateTime of that datasource is a valid datasource.
      return Objects.equal(getName(), that.getName())
        && getLoadTime().after(that.getLoadTime());
    }
  }
}

