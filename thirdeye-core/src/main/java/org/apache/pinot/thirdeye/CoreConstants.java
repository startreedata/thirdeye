package org.apache.pinot.thirdeye;

import java.util.concurrent.TimeUnit;

public interface CoreConstants {

  String TWO_DECIMALS_FORMAT = "#,###.##";
  String MAX_DECIMALS_FORMAT = "#,###.#####";
  String DECIMALS_FORMAT_TOKEN = "#";
  String PROP_DETECTOR_COMPONENT_NAME_DELIMETER = ",";

  int DEFAULT_HEAP_PERCENTAGE_FOR_RESULTSETGROUP_CACHE = 50;
  int DEFAULT_LOWER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB = 100;
  int DEFAULT_UPPER_BOUND_OF_RESULTSETGROUP_CACHE_SIZE_IN_MB = 8192;

  // How much data to prefetch to warm up the cache
  long DEFAULT_CACHING_PERIOD_LOOKBACK = -1;
  long CACHING_PERIOD_LOOKBACK_DAILY = TimeUnit.DAYS.toMillis(90);
  long CACHING_PERIOD_LOOKBACK_HOURLY = TimeUnit.DAYS.toMillis(60);

  // disable minute level cache warm up
  long CACHING_PERIOD_LOOKBACK_MINUTELY = -1;
  long DETECTION_TASK_MAX_LOOKBACK_WINDOW = TimeUnit.DAYS.toMillis(7);
}
