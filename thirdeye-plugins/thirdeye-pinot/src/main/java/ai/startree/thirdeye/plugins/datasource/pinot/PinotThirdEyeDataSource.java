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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.plugins.datasource.pinot.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.plugins.datasource.pinot.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.api.DemoDatasetApi;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotThirdEyeDataSource implements ThirdEyeDataSource {

  public static final String HTTP_SCHEME = "http";
  public static final String HTTPS_SCHEME = "https";
  private static final Logger LOG = LoggerFactory.getLogger(PinotThirdEyeDataSource.class);
  public static final String PINOT_DEMO_PAGEVIEWS = "pinot-demo-pageviews";

  private final String name;
  private final DataSourceDTO dataSourceDTO;
  private final SqlExpressionBuilder sqlExpressionBuilder;
  private final SqlLanguage sqlLanguage;
  private final PinotDatasetReader datasetReader;
  private final LoadingCache<PinotQuery, ThirdEyeResultSetGroup> queryCache;
  private final PinotThirdEyeDataSourceConfig config;
  private final Runnable queryExecutorCloser;

  /* Use case: Log Query Cache stats few min */
  private long queryCacheTs = 0;

  @Inject
  public PinotThirdEyeDataSource(
      final ThirdEyeDataSourceContext context,
      final PinotDatasetReader datasetReader,
      final PinotQueryExecutor queryExecutor,
      final PinotThirdEyeDataSourceConfig config) {
    this.sqlExpressionBuilder = new PinotSqlExpressionBuilder();
    this.sqlLanguage = new PinotSqlLanguage();
    this.datasetReader = datasetReader;

    this.dataSourceDTO = context.getDataSourceDTO();
    this.name = context.getDataSourceDTO().getName();

    /* Uses LoadingCache to cache queries */
    this.queryCache = buildQueryCache(queryExecutor);
    GuavaCacheMetrics.monitor(Metrics.globalRegistry, queryCache, "thirdeye_cache_pinot_query",
        List.of(Tag.of("datasource_name", name),
            Tag.of("namespace", optional(dataSourceDTO.namespace()).orElse("null"))));
    
    // keep reference to the queryExecutor to close it at the end
    this.queryExecutorCloser = queryExecutor::close;
    this.config = config;
  }

  private static LoadingCache<PinotQuery, ThirdEyeResultSetGroup> buildQueryCache(
      final CacheLoader<PinotQuery, ThirdEyeResultSetGroup> cacheLoader) {
    Preconditions.checkNotNull(cacheLoader, "A cache loader is required.");

    // ResultSetGroup Cache. The size of this cache is limited by the total number of buckets in all ResultSetGroup.
    // We estimate that 1 bucket (including overhead) consumes 1KB and this cache is allowed to use up to 50% of max
    // heap space.
    final long maxBucketNumber = getApproximateMaxBucketNumber(
        Constants.DEFAULT_HEAP_PERCENTAGE_FOR_RESULTSETGROUP_CACHE);
    LOG.debug("Max bucket number for {}'s cache is set to {}", cacheLoader,
        maxBucketNumber);

    return CacheBuilder.newBuilder()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .maximumWeight(maxBucketNumber)
        .weigher(PinotThirdEyeDataSource::cacheWeigher)
        .recordStats()
        .build(cacheLoader);
  }

  private static int cacheWeigher(final RelationalQuery relationalQuery,
      final ThirdEyeResultSetGroup resultSetGroup) {
    final int resultSetCount = resultSetGroup.size();
    int weight = 0;
    for (int idx = 0; idx < resultSetCount; ++idx) {
      final ThirdEyeResultSet resultSet = resultSetGroup.get(idx);
      weight += ((resultSet.getColumnCount() + resultSet.getGroupKeyLength()) * resultSet
          .getRowCount());
    }
    return weight;
  }

  private static long getApproximateMaxBucketNumber(final int percentage) {
    long jvmMaxMemoryInBytes = Runtime.getRuntime().maxMemory();
    if (jvmMaxMemoryInBytes == Long.MAX_VALUE) { // Check upper bound
      jvmMaxMemoryInBytes = Constants.DEFAULT_UPPER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB
          * FileUtils.ONE_MB; // MB to Bytes
    } else { // Check lower bound
      final long lowerBoundInBytes =
          Constants.DEFAULT_LOWER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB
              * FileUtils.ONE_MB; // MB to Bytes
      if (jvmMaxMemoryInBytes < lowerBoundInBytes) {
        jvmMaxMemoryInBytes = lowerBoundInBytes;
      }
    }
    return (jvmMaxMemoryInBytes / 102400) * percentage;
  }

  @Override
  public void init(final ThirdEyeDataSourceContext context) {
    /* everything is now done in the constructor */
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns the cached ResultSetGroup corresponding to the given Pinot query.
   *
   * @param pinotQuery the query that is specifically constructed for Pinot.
   * @return the corresponding ResultSetGroup to the given Pinot query.
   * @throws ExecutionException is thrown if failed to connect to Pinot or gets results from
   *     Pinot.
   */
  private ThirdEyeResultSetGroup executeSQL(final PinotQuery pinotQuery) throws ExecutionException,
      ThirdEyeException {
    try {
      final ThirdEyeResultSetGroup thirdEyeResultSetGroup = queryCache.get(pinotQuery);
      final long current = System.currentTimeMillis();

      /* Log query stats with min interval of x minutes */
      if (current - queryCacheTs > Duration.ofMinutes(5).toMillis()) {
        LOG.info("queryCache.stats: {}", queryCache.stats());
        queryCacheTs = current;
      }
      return thirdEyeResultSetGroup;
    } catch (final ExecutionException e) {
      LOG.error("Failed to execute SQL: {} with options {}", pinotQuery.getQuery(),
          pinotQuery.getOptions(), e);
      LOG.error("queryCache.stats: {}", queryCache.stats());
      throw e;
    } catch (UncheckedExecutionException e) {
      LOG.error("Failed to execute SQL: {} with options {}", pinotQuery.getQuery(),
          pinotQuery.getOptions(), e);
      LOG.error("queryCache.stats: {}", queryCache.stats());
      Throwable cause = e.getCause();
      if (cause instanceof ThirdEyeException) {
        throw (ThirdEyeException) cause;
      } else {
        throw new ExecutionException(e.getMessage(), e);
      }
    }
  }

  @Override
  public DataTable fetchDataTable(final DataSourceRequest request) throws Exception {
    final Map<String, String> options = new HashMap<>(dataSourceDTO.getDefaultQueryOptions());
    options.putAll(request.getOptions());
    final ThirdEyeResultSetGroup thirdEyeResultSetGroup = executeSQL(new PinotQuery(
        request.getQuery(),
        request.getTable(),
        options));
    if (thirdEyeResultSetGroup.size() < 1) {
      throw new RuntimeException("Query returned no result. Table is empty? Original query: %s".formatted(request.getQuery()));
    }
    final ThirdEyeResultSet thirdEyeResultSet = thirdEyeResultSetGroup.get(0);
    return new ThirdEyeResultSetDataTable(thirdEyeResultSet);
  }

  @Override
  public boolean validate() {
    try {
      return validate0();
    } catch (final ExecutionException | IOException | ArrayIndexOutOfBoundsException | ThirdEyeException e) {
      LOG.error("Exception while performing pinot datasource validation.", e);
    }
    return false;
  }

  // todo cyril healthcheck should be abstracted by the controller
  private boolean validate0() throws IOException, ExecutionException, ThirdEyeException {
    final PinotHealthCheckConfiguration healthCheck = config.getHealthCheck();
    if (healthCheck == null || !healthCheck.isEnabled()) {
      return true;
    }

    final @Nullable String query = optional(healthCheck.getQuery())
        .filter(sql -> !sql.isBlank())
        .orElse(healthCheckQuery());

    if (query == null) {
      /* No tables. partially validated with REST. can't validate with query. return true */
      return true;
    }

    final PinotQuery pinotQuery = new PinotQuery(query, null, dataSourceDTO.getDefaultQueryOptions());

    /* Disable caching for validate queries */
    queryCache.refresh(pinotQuery);
    final ThirdEyeResultSetGroup result = executeSQL(pinotQuery);
    return result.size() > 0 && result.get(0).getRowCount() > 0;
  }

  private @Nullable String healthCheckQuery() throws IOException {
    // Table name required to execute query against pinot broker.
    final List<String> allTables = datasetReader.getAllTableNames();
    if (allTables.isEmpty()) {
      /* Can't proceed if there are no tables but a successful response is returned as positive */
      return null;
    }

    final String table = allTables.get(0);
    return String.format("SELECT 1 FROM %s LIMIT 1", table);
  }

  @Override
  public List<DatasetConfigDTO> getDatasets() {
    try {
      return datasetReader.getAll(name);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void prepareDatasetForOnboarding(final String datasetName) {
    try {
      datasetReader.prepareDatasetForOnboarding(datasetName);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DatasetConfigDTO getDataset(final String datasetName) {
    try {
      return datasetReader.getTable(datasetName, name);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    queryExecutorCloser.run();
    datasetReader.close();
  }

  @Override
  public SqlLanguage getSqlLanguage() {
    return sqlLanguage;
  }

  @Override
  public SqlExpressionBuilder getSqlExpressionBuilder() {
    return sqlExpressionBuilder;
  }


  @Override
  public @NonNull List<DemoDatasetApi> availableDemoDatasets() {
    return List.of(
        new DemoDatasetApi()
            .setId(PINOT_DEMO_PAGEVIEWS)
            .setName("eCommerce Website Pageviews")
            .setDescription("8 months of e-commerce data at daily granularity.")
    );
  }

  @Override
  public @NonNull String createDemoDataset(final @NonNull String identifier) {
    // a map of schema, config will be used for simplicity
    // TO IMPLEMENT
    return null;
  }
}
