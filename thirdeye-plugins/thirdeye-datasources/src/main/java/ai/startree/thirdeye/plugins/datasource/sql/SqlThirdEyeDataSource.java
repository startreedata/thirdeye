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
package ai.startree.thirdeye.plugins.datasource.sql;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datasource.DataSourceRequest;
import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceContext;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlThirdEyeDataSource implements ThirdEyeDataSource {

  private static final Logger LOG = LoggerFactory.getLogger(SqlThirdEyeDataSource.class);

  private LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> sqlResponseCache;
  private String name;

  @Override
  public void init(final ThirdEyeDataSourceContext context) {
    Map<String, Object> properties = context.getDataSourceDTO().getProperties();

    /* TODO fix onboarding SqlThirdEyeDataSource */
    MetricConfigManager metricConfigManager = null;

    /* TODO fix onboarding SqlThirdEyeDataSource */
    final DatasetConfigManager datasetConfigManager = null;
    SqlResponseCacheLoader sqlResponseCacheLoader = new SqlResponseCacheLoader(properties,
        metricConfigManager,
        datasetConfigManager);
    sqlResponseCache = buildResponseCache(sqlResponseCacheLoader);
    name = MapUtils.getString(properties, "name", SqlThirdEyeDataSource.class.getSimpleName());
  }


  public static LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> buildResponseCache(
      CacheLoader cacheLoader) {
    Preconditions.checkNotNull(cacheLoader, "A cache loader is required.");

    // ResultSetGroup Cache. The size of this cache is limited by the total number of buckets in all ResultSetGroup.
    // We estimate that 1 bucket (including overhead) consumes 1KB and this cache is allowed to use up to 50% of max
    // heap space.
    long maxBucketNumber = getApproximateMaxBucketNumber(
        Constants.DEFAULT_HEAP_PERCENTAGE_FOR_RESULTSETGROUP_CACHE);
    LOG.debug("Max bucket number for {}'s cache is set to {}", cacheLoader,
        maxBucketNumber);

    return CacheBuilder.newBuilder()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .maximumWeight(maxBucketNumber)
        .weigher(
            (Weigher<RelationalQuery, ThirdEyeResultSetGroup>) (relationalQuery, resultSetGroup) -> {
              int resultSetCount = resultSetGroup.size();
              int weight = 0;
              for (int idx = 0; idx < resultSetCount; ++idx) {
                ThirdEyeResultSet resultSet = resultSetGroup.get(idx);
                weight += ((resultSet.getColumnCount() + resultSet.getGroupKeyLength()) * resultSet
                    .getRowCount());
              }
              return weight;
            })
        .build(cacheLoader);
  }

  private static long getApproximateMaxBucketNumber(int percentage) {
    long jvmMaxMemoryInBytes = Runtime.getRuntime().maxMemory();
    if (jvmMaxMemoryInBytes == Long.MAX_VALUE) { // Check upper bound
      jvmMaxMemoryInBytes = Constants.DEFAULT_UPPER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB
          * FileUtils.ONE_MB; // MB to Bytes
    } else { // Check lower bound
      long lowerBoundInBytes = Constants.DEFAULT_LOWER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB
          * FileUtils.ONE_MB; // MB to Bytes
      if (jvmMaxMemoryInBytes < lowerBoundInBytes) {
        jvmMaxMemoryInBytes = lowerBoundInBytes;
      }
    }
    return (jvmMaxMemoryInBytes / 102400) * percentage;
  }
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public DataTable fetchDataTable(final DataSourceRequest request) throws Exception {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the cached ResultSetGroup corresponding to the given Presto query.
   *
   * @param SQLQuery the query that is specifically constructed for Presto.
   * @return the corresponding ThirdEyeResultSet to the given Presto query.
   */
  private ThirdEyeResultSetGroup executeSQL(SqlQuery SQLQuery) throws Exception {
    ThirdEyeResultSetGroup thirdEyeResultSetGroup;
    try {
      thirdEyeResultSetGroup = sqlResponseCache.get(SQLQuery);
    } catch (Exception e) {
      throw e;
    }
    return thirdEyeResultSetGroup;
  }

  @Override
  public void clear() throws Exception {
    // left blank
  }

  @Override
  public void close() throws Exception {
    // left blank
  }
}
