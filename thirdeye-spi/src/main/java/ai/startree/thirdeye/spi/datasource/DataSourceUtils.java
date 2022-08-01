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
package ai.startree.thirdeye.spi.datasource;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.Weigher;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceUtils {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceUtils.class);

  private DataSourceUtils() {
  }

  /**
   * Guess time duration from period.
   *
   * @param granularity dataset granularity
   */
  public static int getTimeDuration(Period granularity) {
    if (granularity.getDays() > 0) {
      return granularity.getDays();
    }
    if (granularity.getHours() > 0) {
      return granularity.getHours();
    }
    if (granularity.getMinutes() > 0) {
      return granularity.getMinutes();
    }
    if (granularity.getSeconds() > 0) {
      return granularity.getSeconds();
    }
    return granularity.getMillis();
  }

  /**
   * Guess time unit from period.
   *
   * @param granularity dataset granularity
   */
  public static TimeUnit getTimeUnit(Period granularity) {
    if (granularity.getDays() > 0) {
      return TimeUnit.DAYS;
    }
    if (granularity.getHours() > 0) {
      return TimeUnit.HOURS;
    }
    if (granularity.getMinutes() > 0) {
      return TimeUnit.MINUTES;
    }
    if (granularity.getSeconds() > 0) {
      return TimeUnit.SECONDS;
    }
    return TimeUnit.MILLISECONDS;
  }

  /**
   * Returns the time spec of the timestamp in the specified dataset config. The timestamp time spec
   * is mainly used
   * for constructing the queries to backend database. For most use case, this method returns the
   * same time spec as
   * getTimeSpecFromDatasetConfig(); however, if the dataset is non-additive, then
   * getTimeSpecFromDatasetConfig
   * should be used unless the application is related to database queries.
   *
   * @param datasetConfig the given dataset config
   * @return the time spec of the timestamp in the specified dataset config.
   */
  public static TimeSpec getTimestampTimeSpecFromDatasetConfig(DatasetConfigDTO datasetConfig) {
    String timeFormat = SpiUtils.getTimeFormatString(datasetConfig);
    return new TimeSpec(datasetConfig.getTimeColumn(),
        new TimeGranularity(datasetConfig.getTimeDuration(), datasetConfig.getTimeUnit()),
        timeFormat);
  }

  public static LoadingCache<RelationalQuery, ThirdEyeResultSetGroup> buildResponseCache(
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
}
