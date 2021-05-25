package org.apache.pinot.thirdeye.datasource;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.Weigher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.pinot.spi.data.DateTimeFieldSpec;
import org.apache.pinot.thirdeye.datasource.pinot.resultset.ThirdEyeResultSetGroup;
import org.apache.pinot.thirdeye.spi.Constants;
import org.apache.pinot.thirdeye.spi.common.time.TimeGranularity;
import org.apache.pinot.thirdeye.spi.common.time.TimeSpec;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.pinot.resultset.ThirdEyeResultSet;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Partial;
import org.joda.time.Period;
import org.joda.time.PeriodType;
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
   * Returns partial to zero out date fields based on period type
   *
   * @return partial
   */
  public static Partial makeOrigin(PeriodType type) {
    List<DateTimeFieldType> fields = new ArrayList<>();

    if (PeriodType.millis().equals(type)) {
      // left blank

    } else if (PeriodType.seconds().equals(type)) {
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.minutes().equals(type)) {
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.hours().equals(type)) {
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.days().equals(type)) {
      fields.add(DateTimeFieldType.hourOfDay());
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else if (PeriodType.months().equals(type)) {
      fields.add(DateTimeFieldType.dayOfMonth());
      fields.add(DateTimeFieldType.hourOfDay());
      fields.add(DateTimeFieldType.minuteOfHour());
      fields.add(DateTimeFieldType.secondOfMinute());
      fields.add(DateTimeFieldType.millisOfSecond());
    } else {
      throw new IllegalArgumentException(String.format("Unsupported PeriodType '%s'", type));
    }

    int[] zeros = new int[fields.size()];
    Arrays.fill(zeros, 0);

    // workaround for dayOfMonth > 0 constraint
    if (PeriodType.months().equals(type)) {
      zeros[0] = 1;
    }

    return new Partial(fields.toArray(new DateTimeFieldType[fields.size()]), zeros);
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
    String timeFormat = getTimeFormatString(datasetConfig);
    return new TimeSpec(datasetConfig.getTimeColumn(),
        new TimeGranularity(datasetConfig.getTimeDuration(), datasetConfig.getTimeUnit()),
        timeFormat);
  }

  public static String getTimeFormatString(DatasetConfigDTO datasetConfig) {
    String timeFormat = datasetConfig.getTimeFormat();
    if (timeFormat.startsWith(DateTimeFieldSpec.TimeFormat.SIMPLE_DATE_FORMAT.toString())) {
      timeFormat = getSDFPatternFromTimeFormat(timeFormat);
    }
    return timeFormat;
  }

  private static String getSDFPatternFromTimeFormat(String timeFormat) {
    String pattern = timeFormat;
    String[] tokens = timeFormat.split(":", 2);
    if (tokens.length == 2) {
      pattern = tokens[1];
    }
    return pattern;
  }

  public static DateTimeZone getDateTimeZone(final DatasetConfigDTO datasetConfig) {
    final String timezone = datasetConfig != null
        ? datasetConfig.getTimezone()
        : TimeSpec.DEFAULT_TIMEZONE;
    return DateTimeZone.forID(timezone);
  }

  public static MetricConfigDTO getMetricConfigFromId(Long metricId,
      final MetricConfigManager metricConfigManager) {
    MetricConfigDTO metricConfig = null;
    if (metricId != null) {
      metricConfig = metricConfigManager.findById(metricId);
    }
    return metricConfig;
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
    LOG.debug("Max bucket number for {}'s cache is set to {}", cacheLoader.toString(),
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
