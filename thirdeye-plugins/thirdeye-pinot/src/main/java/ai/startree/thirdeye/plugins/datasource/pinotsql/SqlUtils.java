/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource.pinotsql;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datasource.RelationalQuery;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.Weigher;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for generated PQL queries (pinot).
 */
public class SqlUtils {

  private static final Logger LOG = LoggerFactory.getLogger(SqlUtils.class);

  public static LoadingCache<RelationalQuery, ResultSet> buildResponseCache(
      CacheLoader cacheLoader) {
    Preconditions.checkNotNull(cacheLoader, "A cache loader is required.");

    // Initializes listener that prints expired entries in debuggin mode.
    RemovalListener<RelationalQuery, ThirdEyeResultSet> listener;
    if (LOG.isDebugEnabled()) {
      listener = notification -> LOG.debug("Expired {}", notification.getKey().getQuery());
    } else {
      listener = notification -> {
      };
    }

    // ResultSetGroup Cache. The size of this cache is limited by the total number of buckets in all ResultSetGroup.
    // We estimate that 1 bucket (including overhead) consumes 1KB and this cache is allowed to use up to 50% of max
    // heap space.
    long maxBucketNumber = getApproximateMaxBucketNumber(
        Constants.DEFAULT_HEAP_PERCENTAGE_FOR_RESULTSETGROUP_CACHE);
    LOG.debug("Max bucket number for {}'s cache is set to {}", cacheLoader,
        maxBucketNumber);

    return CacheBuilder.newBuilder()
        .removalListener(listener)
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .maximumWeight(maxBucketNumber)
        .weigher(
            (Weigher<RelationalQuery, ResultSet>) (relationalQuery, resultSet) -> {
              try {
                resultSet.last();
                return resultSet.getMetaData().getColumnCount() * (resultSet.getRow() < 0 ? 0 : resultSet.getRow());
              } catch (SQLException throwables) {
                throwables.printStackTrace();
              }
              return 0;
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
}
