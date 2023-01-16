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
package ai.startree.thirdeye.plugins.datasource.pinot;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.macro.SqlExpressionBuilder;
import ai.startree.thirdeye.spi.datasource.macro.SqlLanguage;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotThirdEyeDataSource implements ThirdEyeDataSource {

  public static final String HTTP_SCHEME = "http";
  public static final String HTTPS_SCHEME = "https";
  private static final Logger LOG = LoggerFactory.getLogger(PinotThirdEyeDataSource.class);

  private final String name;
  private final SqlExpressionBuilder sqlExpressionBuilder;
  private final SqlLanguage sqlLanguage;
  private final PinotDatasetOnboarder datasetOnboarder;
  private final LoadingCache<PinotQuery, ThirdEyeResultSetGroup> queryCache;
  private final PinotThirdEyeDataSourceConfig config;
  private final PinotConnectionManager connectionManager;

  /* Use case: Log Query Cache stats few min */
  private long queryCacheTs = 0;

  @Inject
  public PinotThirdEyeDataSource(
      final ThirdEyeDataSourceContext context,
      final SqlExpressionBuilder sqlExpressionBuilder,
      final PinotSqlLanguage sqlLanguage,
      final PinotDatasetOnboarder datasetOnboarder,
      final PinotConnectionManager connectionManager,
      final PinotQueryExecutor queryExecutor,
      final PinotThirdEyeDataSourceConfig config) {
    this.sqlExpressionBuilder = sqlExpressionBuilder;
    this.sqlLanguage = sqlLanguage;
    this.datasetOnboarder = datasetOnboarder;

    name = context.getDataSourceDTO().getName();
    this.connectionManager = connectionManager;

    /* Uses LoadingCache to cache queries */
    queryCache = requireNonNull(buildQueryCache(queryExecutor),
        String.format("%s doesn't connect to Pinot or cache is not initialized.", getName()));
    this.config = config;
  }

  public static LoadingCache<PinotQuery, ThirdEyeResultSetGroup> buildQueryCache(
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
  public ThirdEyeResultSetGroup executeSQL(final PinotQuery pinotQuery) throws ExecutionException {
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
      LOG.error("Failed to execute PQL: {}", pinotQuery.getQuery());
      LOG.error("queryCache.stats: {}", queryCache.stats());
      throw e;
    }
  }

  @Override
  public DataTable fetchDataTable(final DataSourceRequest request) throws Exception {
    try {
      // Use pinot SQL.
      final ThirdEyeResultSet thirdEyeResultSet = executeSQL(new PinotQuery(
          request.getQuery(),
          request.getTable(),
          true)).get(0);
      return new ThirdEyeResultSetDataTable(thirdEyeResultSet);
    } catch (final ExecutionException e) {
      throw e;
    }
  }

  @Override
  public boolean validate() {
    try {
      return validate0();
    } catch (final ExecutionException | IOException | ArrayIndexOutOfBoundsException e) {
      LOG.error("Exception while performing pinot datasource validation.", e);
    }
    return false;
  }

  private boolean validate0() throws IOException, ExecutionException {
    final PinotHealthCheckConfiguration healthCheck = config.getHealthCheck();
    if (healthCheck == null || !healthCheck.isEnabled()) {
      return true;
    }

    String query = optional(healthCheck.getQuery())
        .filter(sql -> !sql.isBlank())
        .orElse(null);

    if (query == null) {
      query = healthCheckQuery();
    }
    if (query == null) {
      /* No tables. partially validated with REST. can't validate with query. return true */
      return true;
    }

    final PinotQuery pinotQuery = new PinotQuery(query, null, true);

    /* Disable caching for validate queries */
    queryCache.refresh(pinotQuery);
    final ThirdEyeResultSetGroup result = executeSQL(pinotQuery);
    return result.size() > 0 && result.get(0).getRowCount() > 0;
  }

  private String healthCheckQuery() throws IOException {
    // Table name required to execute query against pinot broker.
    final ImmutableList<String> allTables = datasetOnboarder.getAllTables();
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
      return datasetOnboarder.onboardAll(name);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public DatasetConfigDTO getDataset(final String datasetName) {
    try {
      return datasetOnboarder.onboardTable(datasetName, name);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    connectionManager.close();
    datasetOnboarder.close();
  }

  @Override
  public SqlLanguage getSqlLanguage() {
    return sqlLanguage;
  }

  @Override
  public SqlExpressionBuilder getSqlExpressionBuilder() {
    return sqlExpressionBuilder;
  }
}
